package io.github.ryunen344.suburi.ui.screen.top

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.ryunen344.suburi.ui.theme.SuburiTheme

@Composable
fun TopScreen(onClickUuid: () -> Unit, onClickStructure: () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            Greeting(
                name = "Android",
            )
            Greeting(
                name = "😀",
            )
            Greeting(
                name = "😭",
            )
            Button(onClickUuid) {
                Text("navigate uuid")
            }
            Button(onClickStructure) {
                Text("navigate structure")
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
private fun GreetingPreview() {
    SuburiTheme {
        Greeting("Android")
    }
}
