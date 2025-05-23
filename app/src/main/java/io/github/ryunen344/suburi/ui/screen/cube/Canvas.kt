package io.github.ryunen344.suburi.ui.screen.cube

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Suppress("ModifierWithoutDefault", "LambdaParameterEventTrailing")
@Composable
internal fun CachedCanvas(modifier: Modifier, onBuildDrawCache: CacheDrawScope.() -> DrawResult) =
    Spacer(modifier = modifier.drawWithCache(onBuildDrawCache))

@Suppress("ModifierWithoutDefault", "LambdaParameterEventTrailing")
@Composable
internal fun CachedCanvas(modifier: Modifier, contentDescription: String, onBuildDrawCache: CacheDrawScope.() -> DrawResult) =
    Spacer(
        modifier = modifier
            .drawWithCache(onBuildDrawCache)
            .semantics { this.contentDescription = contentDescription },
    )
