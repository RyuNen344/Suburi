package io.github.ryunen344.suburi.ui

import android.content.Intent
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.ryunen344.suburi.ui.screen.Routes
import io.github.ryunen344.suburi.ui.screen.Structure
import io.github.ryunen344.suburi.ui.screen.WrappedUuid
import io.github.ryunen344.suburi.ui.screen.routes
import io.github.ryunen344.suburi.ui.screen.structure.StructureScreen
import io.github.ryunen344.suburi.ui.screen.toRoutes
import io.github.ryunen344.suburi.ui.screen.top.TopScreen
import io.github.ryunen344.suburi.ui.screen.uuid.UuidScreen
import io.github.ryunen344.suburi.ui.theme.SuburiTheme
import java.util.UUID

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuburiTheme {
                val navController = rememberNavController()
                val onNewIntentListener = remember { Consumer<Intent>(navController::handleDeepLink) }
                DisposableEffect(onNewIntentListener) {
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
}
