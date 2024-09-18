import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.PI
import kotlin.math.sin

@Composable
@Preview
fun App() {
    var task by remember { mutableStateOf(1) }

    MaterialTheme {
        when (task) {
            1 -> Task1 { task = 2 }
            2 -> Task2 { task = 1 }
        }
    }
}

@Composable
fun Task1(onNextTaskClick: () -> Unit) {
    var squares by remember { mutableStateOf(50) }
    val colors = MaterialTheme.colors
    Row {
        Row(
            modifier = Modifier.width(250.dp).fillMaxHeight()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Number of squares: $squares")
                Slider(
                    value = squares.toFloat(),
                    valueRange = 1f..100f,
                    onValueChange = { squares = it.toInt() },
                )
                Button(onClick = onNextTaskClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Next task")
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            var curSize = size.width.coerceAtMost(size.height) / 1.5f
            var start = Offset(
                x = (size.width - curSize) / 2, y = (size.height - curSize) / 2
            )
            val ratio = 0.08f
            for (i in 0 until squares) {
                drawRect(
                    color = colors.primary, topLeft = start, size = Size(curSize, curSize), style = Stroke(2f)
                )
                start = split(start, Offset(start.x + curSize, start.y + curSize), ratio)
                curSize *= (1 - ratio)
            }

        }
    }
}

fun DrawScope.drawTriangle(p1: Offset, p2: Offset, p3: Offset, color: Color) {
    Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        lineTo(p1.x, p1.y)
        close()
    }.let { drawPath(it, color, style = Stroke(2f)) }
}

fun Float.rad(): Float {
    return this * (PI.toFloat() / 180f)
}

fun split(a: Offset, b: Offset, ratio: Float): Offset {
    return Offset(
        x = a.x + (b.x - a.x) * ratio, y = a.y + (b.y - a.y) * ratio
    )
}

@Composable
fun Task2(onPrevTaskClick: () -> Unit) {
    var depth by remember { mutableStateOf(5) }
    var sizeScaling by remember { mutableStateOf(0.5f) }
    var rotation by remember { mutableStateOf(180f) }
    val colors = MaterialTheme.colors
    Row {
        Row(
            modifier = Modifier.width(250.dp).fillMaxHeight()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Num of generations: $depth")
                Slider(
                    value = depth.toFloat(),
                    valueRange = 1f..10f,
                    onValueChange = { depth = it.toInt() },
                )
                Text("Size scaling: $sizeScaling")
                Slider(
                    value = sizeScaling,
                    valueRange = 0f..10f,
                    onValueChange = { sizeScaling = it },
                )
                Text("Rotation: $rotation")
                Slider(
                    value = rotation,
                    valueRange = 0f..360f,
                    onValueChange = { rotation = it },
                )
                Button(
                    onClick = onPrevTaskClick,
                ) {
                    Text("Previous task")
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            val (width, height) = size

            withTransform({
                scale(sizeScaling, sizeScaling)
                rotate(rotation, Offset(width / 2, height / 2))
            }) {
                val triangles = mutableListOf<Triple<Offset, Offset, Offset>>().apply {
                    val curSize = width.coerceAtMost(height) / 1.5f
                    val p1 = Offset(
                        x = (width - curSize) / 2, y = (height - curSize) / 2
                    )
                    val p2 = Offset(
                        x = p1.x + curSize, y = p1.y
                    )
                    val p3 = Offset(
                        x = p1.x + curSize / 2, y = p1.y + curSize * sin(60.0f.rad())
                    )

                    add(Triple(p1, p2, p3))
                }

                var curDepth = 0
                while (triangles.isNotEmpty() && curDepth < depth) {
                    for (i in 0 until triangles.size) {
                        val (p1, p2, p3) = triangles.removeFirst()
                        drawTriangle(p1, p2, p3, colors.primary)

                        val p12 = split(p1, p2, 0.5f)
                        val p23 = split(p2, p3, 0.5f)
                        val p31 = split(p3, p1, 0.5f)

                        triangles.addLast(Triple(p1, p12, p31))
                        triangles.addLast(Triple(p12, p2, p23))
                        triangles.addLast(Triple(p31, p23, p3))
                    }

                    curDepth++
                }


            }

        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
