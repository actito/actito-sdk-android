package com.actito.iam.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.CallSuper
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.actito.Actito
import com.actito.iam.internal.caching.ActitoImageCache
import com.actito.iam.internal.logger
import com.actito.iam.ktx.INTENT_EXTRA_IN_APP_MESSAGE
import com.actito.iam.ktx.inAppMessagingImplementation
import com.actito.iam.ktx.logInAppMessageActionClicked
import com.actito.iam.ktx.logInAppMessageViewed
import com.actito.iam.models.ActitoInAppMessage
import com.actito.ktx.events
import com.actito.utilities.parcel.parcelable
import com.actito.utilities.threading.onMainThread
import kotlinx.coroutines.launch

public abstract class InAppMessagingBaseFragment : Fragment() {
    protected lateinit var message: ActitoInAppMessage

    protected abstract val animatedView: View
    protected abstract val enterAnimation: Int
    protected abstract val exitAnimation: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        message = savedInstanceState?.parcelable(Actito.INTENT_EXTRA_IN_APP_MESSAGE)
            ?: arguments?.parcelable(Actito.INTENT_EXTRA_IN_APP_MESSAGE)
            ?: throw IllegalStateException("Cannot create the UI without the associated in-app message.")
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Prevent tracking the event during configuration changes.
        if (savedInstanceState != null) return

        logger.debug("Tracking in-app message viewed event.")
        lifecycleScope.launch {
            try {
                Actito.events().logInAppMessageViewed(message)
            } catch (e: Exception) {
                logger.error("Failed to log in-app message viewed event.", e)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(Actito.INTENT_EXTRA_IN_APP_MESSAGE, message)
    }

    protected fun animate(transition: Transition, onAnimationFinished: () -> Unit = {}) {
        val animation = when (transition) {
            Transition.ENTER -> AnimationUtils.loadAnimation(requireContext(), enterAnimation)
            Transition.EXIT -> AnimationUtils.loadAnimation(requireContext(), exitAnimation)
        }

        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // no-op
            }

            override fun onAnimationEnd(animation: Animation?) {
                onAnimationFinished()
            }

            override fun onAnimationRepeat(animation: Animation?) {
                // no-op
            }
        })

        animatedView.clearAnimation()
        animatedView.animation = animation
    }

    protected fun dismiss(animated: Boolean = true) {
        if (animated) {
            animate(
                transition = Transition.EXIT,
                onAnimationFinished = {
                    activity?.finish()
                    ActitoImageCache.clear()
                },
            )

            return
        }

        activity?.finish()
        ActitoImageCache.clear()
    }

    protected fun handleActionClicked(actionType: ActitoInAppMessage.ActionType) {
        val action = when (actionType) {
            ActitoInAppMessage.ActionType.PRIMARY -> message.primaryAction
            ActitoInAppMessage.ActionType.SECONDARY -> message.secondaryAction
        }

        if (action == null) {
            logger.debug("There is no '${actionType.rawValue}' action to process.")
            dismiss()

            return
        }

        if (action.url.isNullOrBlank()) {
            logger.debug("There is no URL for '${actionType.rawValue}' action.")
            dismiss()

            return
        }

        lifecycleScope.launch {
            try {
                Actito.events().logInAppMessageActionClicked(message, actionType)
            } catch (e: Exception) {
                logger.error("Failed to log in-app message action.", e)
            }

            val uri = action.url.toUri()

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(requireContext().packageName)
            }

            if (intent.resolveActivity(requireContext().packageManager) == null) {
                intent.setPackage(null)
            }

            try {
                startActivity(intent)
                logger.info("In-app message action '${actionType.rawValue}' successfully processed.")

                onMainThread {
                    Actito.inAppMessagingImplementation().lifecycleListeners.forEach {
                        it.get()?.onActionExecuted(message, action)
                    }
                }

                dismiss(animated = false)
            } catch (e: Exception) {
                logger.warning("Could not find an activity capable of opening the URL.", e)

                onMainThread {
                    Actito.inAppMessagingImplementation().lifecycleListeners.forEach {
                        it.get()?.onActionFailedToExecute(message, action, e)
                    }
                }

                dismiss()
            }
        }
    }

    protected enum class Transition {
        ENTER,
        EXIT,
    }
}
