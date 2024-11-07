package io.github.ryunen344.suburi.navigation

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.compose.rememberNavController

typealias OnHandleDeepLink = (Intent?) -> Intent?

class TypeSafeDeepLinkNavHostController(
    context: Context,
    private val onHandleDeepLink: OnHandleDeepLink = { it },
) : NavHostController(context) {
    override fun handleDeepLink(intent: Intent?): Boolean {
        val handled = onHandleDeepLink(intent)
        return super.handleDeepLink(handled)
    }
}

@Suppress("RestrictedApi")
@Composable
fun rememberTypeSafeDeepLinkNavController(
    onHandleDeepLink: OnHandleDeepLink = { it },
    vararg navigators: Navigator<out NavDestination>,
): NavHostController {
    val context = LocalContext.current
    val currentOnHandleDeepLink by rememberUpdatedState(onHandleDeepLink)
    val composeNavigators = rememberNavController(*navigators).navigatorProvider.navigators
    return rememberSaveable(
        inputs = navigators,
        saver = typeSafeDeepLinkNavControllerSaver(context, currentOnHandleDeepLink, composeNavigators)
    ) {
        createTypeSafeDeepLinkNavController(context, currentOnHandleDeepLink, composeNavigators)
    }
}

private fun createTypeSafeDeepLinkNavController(
    context: Context,
    onHandleDeepLink: OnHandleDeepLink,
    composeNavigators: Map<String, Navigator<out NavDestination>>,
) = TypeSafeDeepLinkNavHostController(context, onHandleDeepLink).apply {
    composeNavigators.forEach(navigatorProvider::addNavigator)
}

private fun typeSafeDeepLinkNavControllerSaver(
    context: Context,
    onHandleDeepLink: OnHandleDeepLink,
    composeNavigators: Map<String, Navigator<out NavDestination>>,
): Saver<NavHostController, *> =
    Saver(
        save = { it.saveState() },
        restore = { createTypeSafeDeepLinkNavController(context, onHandleDeepLink, composeNavigators).apply { restoreState(it) } }
    )
