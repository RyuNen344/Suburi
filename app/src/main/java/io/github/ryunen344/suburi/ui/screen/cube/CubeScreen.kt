package io.github.ryunen344.suburi.ui.screen.cube

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import io.github.ryunen344.suburi.ui.theme.SuburiTheme
import kotlin.math.absoluteValue

private const val CAMERA_DISTANCE = 300f
private const val STROKE_POINT = 15f
private val RotationMatrix = Matrix()
private val IdentityVertices = arrayOf(
    // rear
    intArrayOf(-1, 1, -1),
    intArrayOf(1, 1, -1),
    intArrayOf(1, -1, -1),
    intArrayOf(-1, -1, -1),
    // front
    intArrayOf(-1, 1, 1),
    intArrayOf(1, 1, 1),
    intArrayOf(1, -1, 1),
    intArrayOf(-1, -1, 1),
)

// index of vertices
private val Lines = arrayOf(
    // rear
    intArrayOf(0, 1, Color.Red.toArgb()),
    intArrayOf(1, 2, Color.Red.toArgb()),
    intArrayOf(2, 3, Color.Red.toArgb()),
    intArrayOf(3, 0, Color.Red.toArgb()),
    // front
    intArrayOf(4, 5, Color.Blue.toArgb()),
    intArrayOf(5, 6, Color.Blue.toArgb()),
    intArrayOf(6, 7, Color.Blue.toArgb()),
    intArrayOf(7, 4, Color.Blue.toArgb()),
    // depth
    intArrayOf(0, 4, Color.Green.toArgb()),
    intArrayOf(1, 5, Color.Green.toArgb()),
    intArrayOf(2, 6, Color.Green.toArgb()),
    intArrayOf(3, 7, Color.Green.toArgb()),
)

private val Colors = arrayOf(
    Color.Red,
    Color.Green,
    Color.Blue,
)

@Composable
internal fun CubeScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            var valueChangeFinished by remember { mutableStateOf(false) }
            val durationMillisSlider = remember {
                SliderState(
                    value = 10000f,
                    steps = 9,
                    onValueChangeFinished = { valueChangeFinished = true },
                    valueRange = 1000f..10000f,
                )
            }

            Slider(
                state = durationMillisSlider,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            BoxWithConstraints(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val angle = remember { Animatable(0f) }
                LaunchedEffect(valueChangeFinished) {
                    valueChangeFinished = false
                    val current = angle.value
                    angle.animateTo(
                        targetValue = 720f + current,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = durationMillisSlider.value.toInt(),
                                easing = LinearEasing,
                            ),
                        ),
                    )
                }

                CachedCanvas(
                    modifier = Modifier.size(min(this.maxWidth, this.maxHeight) / 2f),
                ) {
                    val width = (this.size.width / 4f).absoluteValue
                    val vertices = vertices(width, angle.value)
                    val lines = lines(vertices)

                    onDrawBehind {
                        translate(
                            left = center.x,
                            top = center.y,
                        ) {
                            drawPoints(
                                points = vertices,
                                pointMode = PointMode.Points,
                                color = Color.Red,
                                strokeWidth = STROKE_POINT,
                            )

                            lines.forEach { (start, end, color) ->
                                drawLine(
                                    color = color,
                                    start = start,
                                    end = end,
                                    strokeWidth = STROKE_POINT,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun vertices(
    width: Float,
    angle: Float,
): List<Offset> {
    val matrix = RotationMatrix.apply {
        reset()
        rotateX(angle)
        rotateY(angle / 2)
        rotateZ(-angle)
    }

    return IdentityVertices.map { vertex ->
        val x = vertex[0] * width
        val y = vertex[1] * width
        val z = vertex[2] * width
        val rotated = matrix.map(x, y, z)

        val scale = CAMERA_DISTANCE / (CAMERA_DISTANCE + rotated[2])
        val screenX = rotated[0] * scale
        val screenY = rotated[1] * scale

        Offset(screenX, screenY)
    }
}

private fun lines(
    vertices: List<Offset>,
): List<Triple<Offset, Offset, Color>> {
    require(vertices.size == IdentityVertices.size) { "vertices size must be ${IdentityVertices.size}" }
    return Lines.mapIndexed { index, line ->
        Triple(vertices[line[0]], vertices[line[1]], Colors[index / 4])
    }
}

@Suppress("MagicNumber")
private fun Matrix.map(x: Float, y: Float, z: Float): FloatArray {
    if (values.size < 16) return floatArrayOf(x, y, z)

    val v00 = values[Matrix.ScaleX]
    val v01 = values[Matrix.SkewY]
    val v02 = values[2]
    val v10 = values[Matrix.SkewX]
    val v11 = values[Matrix.ScaleY]
    val v12 = values[6]
    val v20 = values[8]
    val v21 = values[9]
    val v22 = values[Matrix.ScaleZ]

    val tx = v00 * x + v10 * y + v20 * z
    val ty = v01 * x + v11 * y + v21 * z
    val tz = v02 * x + v12 * y + v22 * z

    return floatArrayOf(tx, ty, tz)
}

@Preview
@Composable
private fun CubeScreenPreview() {
    SuburiTheme {
        CubeScreen()
    }
}
