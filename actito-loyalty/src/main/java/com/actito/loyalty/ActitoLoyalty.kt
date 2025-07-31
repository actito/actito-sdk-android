package com.actito.loyalty

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import com.actito.Actito
import com.actito.ActitoApplicationUnavailableException
import com.actito.ActitoCallback
import com.actito.ActitoDeviceUnavailableException
import com.actito.ActitoNotReadyException
import com.actito.ActitoServiceUnavailableException
import com.actito.internal.network.request.ActitoRequest
import com.actito.ktx.device
import com.actito.loyalty.internal.logger
import com.actito.loyalty.internal.network.push.FetchPassResponse
import com.actito.loyalty.internal.network.push.FetchPassbookTemplateResponse
import com.actito.loyalty.internal.network.push.FetchSaveLinksResponse
import com.actito.loyalty.ktx.INTENT_EXTRA_PASSBOOK
import com.actito.loyalty.models.ActitoPass
import com.actito.models.ActitoApplication
import com.actito.models.ActitoNotification
import com.actito.utilities.coroutines.toCallbackFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public object ActitoLoyalty {

    // region Intent actions

    public const val INTENT_ACTION_PASSBOOK_OPENED: String = "com.actito.intent.action.PassbookOpened"

    // endregion

    // region Intent extras

    public const val INTENT_EXTRA_PASSBOOK: String = "com.actito.intent.extra.Passbook"

    // endregion

    // region Actito Loyalty

    /**
     * Specifies the class responsible for displaying a Passbook-style activity.
     *
     * This property must be set to a class that extends [PassbookActivity], which is used to present
     * pass details to the user. When invoking the `present` method, this class will be used to handle
     * the UI and interaction for displaying the pass.
     */
    @JvmStatic
    public var passbookActivity: Class<out PassbookActivity> = PassbookActivity::class.java

    /**
     * Fetches a pass by its serial number.
     *
     * @param serial The serial number of the pass to be fetched.
     * @return The fetched [ActitoPass] corresponding to the given serial number.
     *
     * @see [ActitoPass]
     */
    public suspend fun fetchPassBySerial(serial: String): ActitoPass = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val pass = ActitoRequest.Builder()
            .get("/pass/forserial/$serial")
            .responseDecodable(FetchPassResponse::class)
            .pass

        enhancePass(pass)
    }

    /**
     * Fetches a pass by its serial number with a callback.
     *
     * @param serial The serial number of the pass to be fetched.
     * @param callback A callback that will be invoked with the result of the fetch operation.
     *                 This will provide either the [ActitoPass] on success
     *                 or an error on failure.
     */
    @JvmStatic
    public fun fetchPassBySerial(serial: String, callback: ActitoCallback<ActitoPass>): Unit =
        toCallbackFunction(::fetchPassBySerial)(serial, callback::onSuccess, callback::onFailure)

    /**
     * Fetches a pass by its barcode.
     *
     * @param barcode The barcode of the pass to be fetched.
     * @return The fetched [ActitoPass] corresponding to the given barcode.
     *
     * @see [ActitoPass]
     */
    public suspend fun fetchPassByBarcode(barcode: String): ActitoPass = withContext(Dispatchers.IO) {
        checkPrerequisites()

        val pass = ActitoRequest.Builder()
            .get("/pass/forbarcode/$barcode")
            .responseDecodable(FetchPassResponse::class)
            .pass

        enhancePass(pass)
    }

    /**
     * Fetches a pass by its barcode with a callback.
     *
     * @param barcode The barcode of the pass to be fetched.
     * @param callback A callback that will be invoked with the result of the fetch operation.
     *                 This will provide either the [ActitoPass] on success
     *                 or an error on failure.
     */
    @JvmStatic
    public fun fetchPassByBarcode(barcode: String, callback: ActitoCallback<ActitoPass>): Unit =
        toCallbackFunction(::fetchPassByBarcode)(barcode, callback::onSuccess, callback::onFailure)

    /**
     * Presents a pass to the user in a Passbook-style activity.
     *
     * This method uses the activity specified in [passbookActivity] to present the given pass to the user.
     *
     * @param activity The [Activity] context from which the pass presentation will be launched.
     * @param pass The [ActitoPass] to be presented to the user.
     */
    @JvmStatic
    public fun present(activity: Activity, pass: ActitoPass) {
        present(activity, pass, null)
    }

    // endregion

    // region Actito Loyalty Integration

    public fun handlePassPresentation(
        activity: Activity,
        notification: ActitoNotification,
        callback: ActitoCallback<Unit>,
    ) {
        val serial = extractPassSerial(notification) ?: run {
            logger.warning("Unable to extract the pass' serial from the notification.")

            val error = IllegalArgumentException("Unable to extract the pass' serial from the notification.")
            callback.onFailure(error)

            return
        }

        fetchPassBySerial(
            serial,
            object : ActitoCallback<ActitoPass> {
                override fun onSuccess(result: ActitoPass) {
                    present(activity, result, callback)
                }

                override fun onFailure(e: Exception) {
                    logger.error("Failed to fetch the pass with serial '$serial'.", e)
                    callback.onFailure(e)
                }
            },
        )
    }

    // endregion

    @Throws
    private fun checkPrerequisites() {
        if (!Actito.isReady) {
            logger.warning("Actito is not ready yet.")
            throw ActitoNotReadyException()
        }

        if (Actito.device().currentDevice == null) {
            logger.warning("Actito device is not yet available.")
            throw ActitoDeviceUnavailableException()
        }

        val application = Actito.application ?: run {
            logger.warning("Actito application is not yet available.")
            throw ActitoApplicationUnavailableException()
        }

        if (application.services[ActitoApplication.ServiceKeys.PASSBOOK] != true) {
            logger.warning("Actito passes functionality is not enabled.")
            throw ActitoServiceUnavailableException(service = ActitoApplication.ServiceKeys.PASSBOOK)
        }
    }

    private fun extractPassSerial(notification: ActitoNotification): String? {
        if (notification.type != ActitoNotification.TYPE_PASSBOOK) return null

        val content = notification.content
            .firstOrNull { it.type == ActitoNotification.Content.TYPE_PK_PASS }
            ?: return null

        val url = content.data as? String ?: return null

        val parts = url.split("/")
        if (parts.isEmpty()) return null

        return parts.last()
    }

    private suspend fun enhancePass(pass: FetchPassResponse.Pass): ActitoPass = withContext(Dispatchers.IO) {
        val passType = when (pass.version) {
            1 -> pass.passbook?.let { fetchPassType(it) }
            else -> null
        }

        val googlePaySaveLink = when (pass.version) {
            2 -> fetchGooglePaySaveLink(pass.serial)
            else -> null
        }

        ActitoPass(
            id = pass._id,
            type = passType,
            version = pass.version,
            passbook = pass.passbook,
            template = pass.template,
            serial = pass.serial,
            barcode = pass.barcode,
            redeem = pass.redeem,
            redeemHistory = pass.redeemHistory,
            limit = pass.limit,
            token = pass.token,
            data = pass.data ?: emptyMap(),
            date = pass.date,
            googlePaySaveLink = googlePaySaveLink,
        )
    }

    private suspend fun fetchPassType(passbook: String): ActitoPass.PassType = withContext(Dispatchers.IO) {
        ActitoRequest.Builder()
            .get("/passbook/$passbook")
            .responseDecodable(FetchPassbookTemplateResponse::class)
            .passbook
            .passStyle
    }

    private suspend fun fetchGooglePaySaveLink(serial: String): String? = withContext(Dispatchers.IO) {
        ActitoRequest.Builder()
            .get("/pass/savelinks/$serial")
            .responseDecodable(FetchSaveLinksResponse::class)
            .saveLinks
            ?.googlePay
    }

    private fun present(activity: Activity, pass: ActitoPass, callback: ActitoCallback<Unit>?) {
        when (pass.version) {
            1 -> {
                activity.startActivity(
                    Intent(activity, passbookActivity)
                        .putExtra(Actito.INTENT_EXTRA_PASSBOOK, pass),
                )

                callback?.onSuccess(Unit)
            }
            2 -> {
                val url = pass.googlePaySaveLink ?: run {
                    logger.warning("Cannot present the pass without a Google Pay link.")

                    val error = IllegalArgumentException("Cannot present the pass without a Google Pay link.")
                    callback?.onFailure(error)

                    return
                }

                try {
                    val intent = Intent().setAction(Intent.ACTION_VIEW)
                        .setData(url.toUri())
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    activity.startActivity(intent)
                    callback?.onSuccess(Unit)
                } catch (e: ActivityNotFoundException) {
                    logger.error("Failed to present the pass.", e)
                    callback?.onFailure(e)
                }
            }
            else -> {
                logger.error("Unsupported pass version: ${pass.version}")

                val error = IllegalArgumentException("Unsupported pass version: ${pass.version}")
                callback?.onFailure(error)
            }
        }
    }
}
