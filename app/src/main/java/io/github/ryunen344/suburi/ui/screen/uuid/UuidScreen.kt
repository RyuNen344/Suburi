package io.github.ryunen344.suburi.ui.screen.uuid

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.ryunen344.suburi.ui.screen.WrappedUuid

@Composable
internal fun UuidScreen(
    uuid: WrappedUuid,
    viewModel: UuidViewModel = hiltViewModel(),
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            Text(text = "uuid $uuid")

            Button(viewModel::hoge) {
                Text("print uuid")
            }
        }
    }
}
