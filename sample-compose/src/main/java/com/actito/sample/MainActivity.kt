package com.actito.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.actito.Actito
import com.actito.assets.models.ActitoAsset
import com.actito.geo.ktx.INTENT_ACTION_BEACON_NOTIFICATION_OPENED
import com.actito.iam.ActitoInAppMessaging
import com.actito.iam.ktx.inAppMessaging
import com.actito.iam.models.ActitoInAppMessage
import com.actito.models.ActitoNotification
import com.actito.push.ktx.push
import com.actito.push.ui.ActitoPushUI
import com.actito.push.ui.ktx.pushUI
import com.actito.sample.core.SampleNotifier
import com.actito.sample.core.SampleSnackBarController
import com.actito.sample.core.SampleSnackbarVisuals
import com.actito.sample.ui.assets.AssetsScreen
import com.actito.sample.ui.assets.details.AssetDetailsScreen
import com.actito.sample.ui.beacons.BeaconsScreen
import com.actito.sample.ui.device.DeviceScreen
import com.actito.sample.ui.events.EventsScreen
import com.actito.sample.ui.home.HomeScreen
import com.actito.sample.ui.inbox.InboxScreen
import com.actito.sample.ui.tags.TagsScreen
import com.actito.sample.ui.theme.ActitoSampleTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
private data object RouteHome : NavKey

@Serializable
private data object RouteDevice : NavKey

@Serializable
private data object RouteInbox : NavKey

@Serializable
private data object RouteTags : NavKey

@Serializable
private data object RouteBeacons : NavKey

@Serializable
private data object RouteEvents : NavKey

@Serializable
private data object RouteAssets : NavKey

@Serializable
private data class RouteAssetDetails(val asset: @Contextual ActitoAsset) : NavKey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val backStack = rememberNavBackStack(RouteHome)
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                SampleSnackBarController.snackbarEvents.collect { event ->
                    snackbarHostState.showSnackbar(
                        visuals = SampleSnackbarVisuals(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            duration = event.duration,
                            type = event.type,
                            withDismissAction = true,
                        ),
                    )
                }
            }

            ActitoSampleTheme {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<RouteHome> {
                            HomeScreen(
                                snackbarHostState = snackbarHostState,
                                onNavigateToDevice = {
                                    backStack.add(RouteDevice)
                                },
                                onNavigateToInbox = {
                                    backStack.add(RouteInbox)
                                },
                                onNavigateToTags = {
                                    backStack.add(RouteTags)
                                },
                                onNavigateToBeacons = {
                                    backStack.add(RouteBeacons)
                                },
                                onNavigateToAssets = {
                                    backStack.add(RouteAssets)
                                },
                                onNavigateToEvents = {
                                    backStack.add(RouteEvents)
                                },
                            )
                        }

                        entry<RouteDevice> {
                            DeviceScreen(
                                snackbarHostState = snackbarHostState,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }

                        entry<RouteInbox> {
                            InboxScreen(
                                snackbarHostState = snackbarHostState,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }

                        entry<RouteTags> {
                            TagsScreen(
                                snackbarHostState = snackbarHostState,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }

                        entry<RouteBeacons> {
                            BeaconsScreen(
                                snackbarHostState = snackbarHostState,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }

                        entry<RouteAssets> {
                            AssetsScreen(
                                snackbarHostState = snackbarHostState,
                                onNavigateBack = { backStack.removeLastOrNull() },
                                onNavigateToAssetDetails = { asset -> backStack.add(RouteAssetDetails(asset)) },
                            )
                        }

                        entry<RouteEvents> {
                            EventsScreen(
                                snackbarHostState = snackbarHostState,
                                onNavigateBack = { backStack.removeLastOrNull() },
                            )
                        }

                        entry<RouteAssetDetails> { key ->
                            AssetDetailsScreen(
                                snackbarHostState = snackbarHostState,
                                onNavigateBack = { backStack.removeLastOrNull() },
                                asset = key.asset,
                            )
                        }
                    },

                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { -it })
                    },
                    popTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { it })
                    },
                    predictivePopTransitionSpec = {
                        slideInHorizontally(initialOffsetX = { -it }) togetherWith
                            slideOutHorizontally(targetOffsetX = { it })
                    },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                )
            }
        }

        if (intent != null) handleIntent(intent)

        Actito.pushUI().addLifecycleListener(notificationLifecycleListener)
        Actito.inAppMessaging().addLifecycleListener(messageLifecycleListener)
    }

    override fun onDestroy() {
        super.onDestroy()

        Actito.pushUI().removeLifecycleListener(notificationLifecycleListener)
        Actito.inAppMessaging().removeLifecycleListener(messageLifecycleListener)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Actito.push().handleTrampolineIntent(intent)) return
        if (Actito.handleTestDeviceIntent(intent)) return
        if (Actito.handleDynamicLinkIntent(this, intent)) return

        Actito.push().parseNotificationOpenedIntent(intent)?.also { result ->
            Actito.pushUI().presentNotification(this, result.notification)
            return
        }

        Actito.push().parseNotificationActionOpenedIntent(intent)?.also { result ->
            Actito.pushUI().presentAction(this, result.notification, result.action)
            return
        }

        when (intent.action) {
            Actito.INTENT_ACTION_BEACON_NOTIFICATION_OPENED -> {
                lifecycleScope.launch {
                    SampleNotifier.emitInfo("Beacon notification opened.")
                }

                return
            }
        }

        val uri = intent.data ?: return

        lifecycleScope.launch {
            SampleNotifier.emitInfo("Received deep link.\n\nURI: $uri")
        }
    }

    private val messageLifecycleListener = object : ActitoInAppMessaging.MessageLifecycleListener {
        override fun onMessagePresented(message: ActitoInAppMessage) {
            lifecycleScope.launch {
                SampleNotifier.emitInfo("IAM presented.\n\nName: ${message.name}")
            }
        }

        override fun onMessageFinishedPresenting(message: ActitoInAppMessage) {
            lifecycleScope.launch {
                SampleNotifier.emitInfo("IAM finished presenting.\n\nName: ${message.name}")
            }
        }

        override fun onMessageFailedToPresent(message: ActitoInAppMessage) {
            lifecycleScope.launch {
                SampleNotifier.emitError("IAM failed to present.\n\nName: ${message.name}")
            }
        }

        override fun onActionExecuted(message: ActitoInAppMessage, action: ActitoInAppMessage.Action) {
            lifecycleScope.launch {
                SampleNotifier.emitInfo(
                    "IAM action executed.\n\n" + "Message name: ${message.name}.\n\nAction label: ${action.label}",
                )
            }
        }

        override fun onActionFailedToExecute(
            message: ActitoInAppMessage,
            action: ActitoInAppMessage.Action,
            error: Exception?,
        ) {
            lifecycleScope.launch {
                SampleNotifier.emitError(
                    "IAM action failed to execute.\n\n" +
                        "Message name: ${message.name}.\n\nAction label: ${action.label}",
                )
            }
        }
    }

    private val notificationLifecycleListener = object : ActitoPushUI.NotificationLifecycleListener {
        override fun onNotificationWillPresent(notification: ActitoNotification) {
            lifecycleScope.launch {
                SampleNotifier.emitInfo("Notification will present.\n\nID: ${notification.id}")
            }
        }

        override fun onNotificationPresented(notification: ActitoNotification) {
            lifecycleScope.launch {
                SampleNotifier.emitInfo("Notification presented.\n\nID: ${notification.id}")
            }
        }

        override fun onNotificationFinishedPresenting(notification: ActitoNotification) {
            lifecycleScope.launch {
                SampleNotifier.emitInfo("Notification finished presenting.\n\nID: ${notification.id}")
            }
        }

        override fun onNotificationFailedToPresent(notification: ActitoNotification) {
            lifecycleScope.launch {
                SampleNotifier.emitError("Notification failed to present.\n\nID: ${notification.id}")
            }
        }

        override fun onNotificationUrlClicked(notification: ActitoNotification, uri: Uri) {
            lifecycleScope.launch {
                SampleNotifier.emitInfo("Notification URL clicked.\n\nURL: $uri")
            }
        }

        override fun onActionWillExecute(notification: ActitoNotification, action: ActitoNotification.Action) {
            lifecycleScope.launch {
                SampleNotifier.emitInfo(
                    "Action will execute.\n\nNotification ID: ${notification.id}.\n\nAction label ID: ${action.label}.",
                )
            }
        }

        override fun onActionExecuted(notification: ActitoNotification, action: ActitoNotification.Action) {
            lifecycleScope.launch {
                SampleNotifier.emitInfo(
                    "" +
                        "Action executed.\n\nNotification ID: ${notification.id}.\n\nAction label ID: ${action.label}.",
                )
            }
        }

        override fun onActionFailedToExecute(
            notification: ActitoNotification,
            action: ActitoNotification.Action,
            error: Exception?,
        ) {
            lifecycleScope.launch {
                SampleNotifier.emitError(
                    "Action failed to execute.\n\n" +
                        "Notification ID: ${notification.id}.\n\nAction label ID: ${action.label}.",
                )
            }
        }

        override fun onCustomActionReceived(
            notification: ActitoNotification,
            action: ActitoNotification.Action,
            uri: Uri,
        ) {
            lifecycleScope.launch {
                SampleNotifier.emitInfo(
                    "Custom Action received.\n\nNotification ID: ${notification.id}.\n\n" +
                        "Action label ID: ${action.label}.",
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ActitoSampleTheme {
        Greeting("Android")
    }
}
