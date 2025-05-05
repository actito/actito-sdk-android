package com.actito.loyalty

import android.app.Activity
import com.actito.ActitoCallback
import com.actito.loyalty.models.ActitoPass

public interface ActitoLoyalty {

    /**
     * Specifies the class responsible for displaying a Passbook-style activity.
     *
     * This property must be set to a class that extends [PassbookActivity], which is used to present
     * pass details to the user. When invoking the `present` method, this class will be used to handle
     * the UI and interaction for displaying the pass.
     */
    public var passbookActivity: Class<out PassbookActivity>

    /**
     * Fetches a pass by its serial number.
     *
     * @param serial The serial number of the pass to be fetched.
     * @return The fetched [ActitoPass] corresponding to the given serial number.
     *
     * @see [ActitoPass]
     */
    public suspend fun fetchPassBySerial(serial: String): ActitoPass

    /**
     * Fetches a pass by its serial number with a callback.
     *
     * @param serial The serial number of the pass to be fetched.
     * @param callback A callback that will be invoked with the result of the fetch operation.
     *                 This will provide either the [ActitoPass] on success
     *                 or an error on failure.
     */
    public fun fetchPassBySerial(serial: String, callback: ActitoCallback<ActitoPass>)

    /**
     * Fetches a pass by its barcode.
     *
     * @param barcode The barcode of the pass to be fetched.
     * @return The fetched [ActitoPass] corresponding to the given barcode.
     *
     * @see [ActitoPass]
     */
    public suspend fun fetchPassByBarcode(barcode: String): ActitoPass

    /**
     * Fetches a pass by its barcode with a callback.
     *
     * @param barcode The barcode of the pass to be fetched.
     * @param callback A callback that will be invoked with the result of the fetch operation.
     *                 This will provide either the [ActitoPass] on success
     *                 or an error on failure.
     */
    public fun fetchPassByBarcode(barcode: String, callback: ActitoCallback<ActitoPass>)

    /**
     * Presents a pass to the user in a Passbook-style activity.
     *
     * This method uses the activity specified in [passbookActivity] to present the given pass to the user.
     *
     * @param activity The [Activity] context from which the pass presentation will be launched.
     * @param pass The [ActitoPass] to be presented to the user.
     */
    public fun present(activity: Activity, pass: ActitoPass)
}
