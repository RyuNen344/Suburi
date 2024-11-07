package io.github.ryunen344.suburi.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import dagger.hilt.android.AndroidEntryPoint
import io.github.ryunen344.suburi.navigation.rememberTypeSafeDeepLinkNavController
import io.github.ryunen344.suburi.ui.screen.Routes
import io.github.ryunen344.suburi.ui.screen.Structure
import io.github.ryunen344.suburi.ui.screen.WrappedUuid
import io.github.ryunen344.suburi.ui.screen.routes
import io.github.ryunen344.suburi.ui.screen.structure.StructureScreen
import io.github.ryunen344.suburi.ui.screen.toRoutes
import io.github.ryunen344.suburi.ui.screen.top.TopScreen
import io.github.ryunen344.suburi.ui.screen.uuid.UuidScreen
import io.github.ryunen344.suburi.ui.theme.SuburiTheme
import timber.log.Timber
import java.util.UUID

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Timber.wtf("onCreate ${this.hashCode()}")
        setContent {
            SuburiTheme {
                val navController = rememberTypeSafeDeepLinkNavController(
                    onHandleDeepLink = {
                        val handled = if (it == null || it.data == null) {
                            it
                        } else {
                            val origin = it.data
                            // handle origin
                            Intent(it).also {
                                it.data = Uri.parse("https://www.example.com/uuid/47277417-a40f-43ac-9d27-009835c3e356")
                            }
                        }
                        Timber.wtf("handleDeepLink $this original:${it?.data}, handled:${handled?.data}")
                        handled
                    }
                )

                DisposableEffect(navController) {
                    val listener = NavController.OnDestinationChangedListener { _, destination, arguments ->
                        Timber.tag("OnDestinationChangedListener").wtf("destination:{$destination}, arguments:{$arguments}")
                    }

                    navController.addOnDestinationChangedListener(listener)
                    onDispose {
                        navController.removeOnDestinationChangedListener(listener)
                    }
                }

                val onNewIntentListener = remember(navController) { Consumer<Intent>(navController::handleDeepLink) }
                DisposableEffect(this) {
                    addOnNewIntentListener(onNewIntentListener)
                    onDispose {
                        removeOnNewIntentListener(onNewIntentListener)
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Routes.Top::class,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    routes<Routes.Top> {
                        TopScreen(
                            onClickUuid = {
                                navController.navigate(Routes.Uuid(WrappedUuid(UUID.randomUUID())))
                            },
                            onClickStructure = {
                                navController.navigate(Routes.Structures(Structure.random()))
                            },
                        )
                    }
                    routes<Routes.Uuid> {
                        UuidScreen(uuid = it.toRoutes<Routes.Uuid>().uuid)
                    }
                    routes<Routes.Structures> {
                        StructureScreen(structure = it.toRoutes<Routes.Structures>().structure)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        Timber.wtf("onNewIntent $intent")
        super.onNewIntent(intent)
    }
}
