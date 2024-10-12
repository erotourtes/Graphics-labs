import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.sin

val Float.rad get() = (this * Math.PI / 180).toFloat()

fun DrawScope.drawKochSnowflake(
    t1: Offset,
    t2: Offset,
    t3: Offset,
    color: Color,
    depth: Int
) {
    val path = Path()

    fun drawKochSide(start: Offset, end: Offset, opposite: Offset, depth: Int) {
        if (depth == 0) {
            path.lineTo(end.x, end.y)
        } else {
            val p1 = (end + start * 2f) / 3f
            val p3 = (end * 2f + start) / 3f
            val p2 = ((start + end) / 2f * 4f - opposite) / 3f

            val startOpposite = (opposite + start * 2f) / 3f
            val endOpposite = (opposite + end * 2f) / 3f

            drawKochSide(start, p1, startOpposite, depth - 1)
            drawKochSide(p1, p2, p3, depth - 1)
            drawKochSide(p2, p3, p1, depth - 1)
            drawKochSide(p3, end, endOpposite, depth - 1)
        }
    }

    path.moveTo(t1.x, t1.y)
    drawKochSide(t1, t2, t3, depth)
    drawKochSide(t2, t3, t1, depth)
    drawKochSide(t3, t1, t2, depth)

    drawPath(path, color, style = Stroke(width = 2f))
}

@Composable
@Preview
fun App() {
    var depth by remember { mutableStateOf(1) }
    var featureSize by remember { mutableStateOf(100) }

    MaterialTheme {
        Row {
            Column(modifier = Modifier.weight(1f)) {
                Slider(onValueChange = { depth = it.toInt() }, value = depth.toFloat(), valueRange = 1f..10f)
                Text("Depth: $depth")

                Slider(
                    onValueChange = { featureSize = it.toInt() },
                    value = featureSize.toFloat(),
                    valueRange = 100f..300f
                )
                Text("Size: $featureSize")
            }
            Canvas(modifier = Modifier.weight(5f).fillMaxSize().background(Color.LightGray)) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val size = featureSize.toFloat()
                val left = Offset(0f, 0f)
                val right = Offset(size, 0f)
                val top = Offset(size / 2, size * sin(60f.rad))
                translate(-size / 2, size * sin(60f.rad) * 2 / 3) {
                    scale(1f, -1f) {
                        translate(centerX, centerY) {
                            drawKochSnowflake(left, right, top, Color.Red, depth)
                        }
                    }
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
