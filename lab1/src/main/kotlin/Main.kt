import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.random.Random

val Color.Companion.BG: Color
    get() = Color(0x276bffd3)

val Color.Companion.FG: Color
    get() = Color(0xF83eb5ad)

val icon = System.getProperty("icon") ?: "icon.png"

@Composable
@Preview
fun App(exitApplication: () -> Unit) {
    var draw by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.BG)
            .padding(16.dp)
            .clickable { draw = !draw },
    ) {
        CanvasDrawable(draw)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "Lab 1",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.FG
            )
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                "By IM-21 student".forEach {
                    Text(
                        text = it.toString(),
                        fontSize = Random.nextInt(16, 32).sp,
                        color = Color(Random.nextInt(50, 70), Random.nextInt(127, 181), Random.nextInt(86, 181)),
                        letterSpacing = Random.nextInt(1, 5).sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Text(text = "Max Siryk", fontSize = 24.sp, color = Color.FG)
            OutlinedButton(
                onClick = exitApplication,
                colors = buttonColors(
                    contentColor = Color.FG,
                    backgroundColor = Color.BG
                ),
                modifier = Modifier.align(Alignment.End)
            ) { Text("Click Me") }
            Image(painter = painterResource(icon), contentDescription = "Icon")
        }
    }
}

@Composable
fun CanvasDrawable(
    draw: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (draw) {
            // Draw 'M'
            drawLine(
                color = Color.FG,
                start = Offset(50f, 50f),
                end = Offset(50f, 150f),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.FG,
                start = Offset(50f, 50f),
                end = Offset(100f, 100f),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.FG,
                start = Offset(100f, 100f),
                end = Offset(150f, 50f),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.FG,
                start = Offset(150f, 50f),
                end = Offset(150f, 150f),
                strokeWidth = 4f
            )

            // Draw 'A'
            drawLine(
                color = Color.FG,
                start = Offset(200f, 150f),
                end = Offset(250f, 50f),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.FG,
                start = Offset(250f, 50f),
                end = Offset(300f, 150f),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.FG,
                start = Offset(225f, 100f),
                end = Offset(275f, 100f),
                strokeWidth = 4f
            )

            // Draw 'X'
            drawLine(
                color = Color.FG,
                start = Offset(350f, 50f),
                end = Offset(400f, 150f),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.FG,
                start = Offset(400f, 50f),
                end = Offset(350f, 150f),
                strokeWidth = 4f
            )
        }
    }
}

fun main() = application {
    val icon = useResource(icon, ::loadImageBitmap)
    Window(
        onCloseRequest = ::exitApplication,
        icon = BitmapPainter(icon),
        title = "Lab 1: Max Siryk IM-21"
    ) {
        App(::exitApplication)
    }
}
