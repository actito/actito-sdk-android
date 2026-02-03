package com.actito.sample

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
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.actito.assets.models.ActitoAsset
import com.actito.sample.core.SampleSnackBar
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
                SampleSnackBar.snackbarEvents.collect { event ->
                    snackbarHostState.showSnackbar(
                        visuals = SampleSnackbarVisuals(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            duration = event.duration,
                            type = event.type,
                        ),
                    )
                }
            }

            ActitoSampleTheme {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            is RouteHome -> NavEntry(key) {
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

                            is RouteDevice -> NavEntry(key) {
                                DeviceScreen(
                                    snackbarHostState = snackbarHostState,
                                    onNavigateBack = { backStack.removeLastOrNull() },
                                )
                            }

                            is RouteInbox -> NavEntry(key) {
                                InboxScreen(
                                    snackbarHostState = snackbarHostState,
                                    onNavigateBack = { backStack.removeLastOrNull() },
                                )
                            }

                            is RouteTags -> NavEntry(key) {
                                TagsScreen(
                                    snackbarHostState = snackbarHostState,
                                    onNavigateBack = { backStack.removeLastOrNull() },
                                )
                            }

                            is RouteBeacons -> NavEntry(key) {
                                BeaconsScreen(
                                    snackbarHostState = snackbarHostState,
                                    onNavigateBack = { backStack.removeLastOrNull() },
                                )
                            }

                            is RouteAssets -> NavEntry(key) {
                                AssetsScreen(
                                    snackbarHostState = snackbarHostState,
                                    onNavigateBack = { backStack.removeLastOrNull() },
                                    onNavigateToAssetDetails = { asset -> backStack.add(RouteAssetDetails(asset)) },
                                )
                            }

                            is RouteEvents -> NavEntry(key) {
                                EventsScreen(
                                    snackbarHostState = snackbarHostState,
                                    onNavigateBack = { backStack.removeLastOrNull() },
                                )
                            }

                            is RouteAssetDetails -> NavEntry(key) {
                                AssetDetailsScreen(
                                    snackbarHostState = snackbarHostState,
                                    onNavigateBack = { backStack.removeLastOrNull() },
                                    asset = key.asset,
                                )
                            }

                            else -> {
                                error("Unknown route: $key")
                            }
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
