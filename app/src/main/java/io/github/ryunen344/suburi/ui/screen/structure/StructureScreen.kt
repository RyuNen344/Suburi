package io.github.ryunen344.suburi.ui.screen.structure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ryunen344.suburi.ui.screen.Structure

@Composable
internal fun StructureScreen(
    structure: Structure,
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            Text(text = "structure $structure")
        }
    }
}
