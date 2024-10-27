package io.github.ryunen344.suburi.ui.screen.structure

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ryunen344.suburi.ui.screen.Routes
import io.github.ryunen344.suburi.ui.screen.toRoutes
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class StructureViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    fun hoge() {
        runCatching {
            savedStateHandle.toRoutes<Routes.Structures>().structure
        }.onSuccess {
            Timber.d("$it")
        }.onFailure(Timber::e)
    }
}
