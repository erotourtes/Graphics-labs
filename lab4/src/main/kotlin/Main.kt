import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import kotlin.random.Random

// TODO: compose `by` vs `=`; isAnimating with `=` is not working?

data class Obstacle(
    val position: Offset,
    val size: Size,
) {

}

@Composable
@Preview
fun PositionInput(value: Offset = Offset.Zero, onUpdate: (Offset) -> Unit) {
    var x by remember { mutableStateOf(value.x.toString()) }
    var y by remember { mutableStateOf(value.y.toString()) }

    Column {
        Text("Image Position:")
        TextField(value = x, onValueChange = { x = it }, isError = x.toFloatOrNull() == null, label = { Text("X:") })
        TextField(value = y, onValueChange = { y = it }, isError = y.toFloatOrNull() == null, label = { Text("Y:") })
        Button(onClick = {
            onUpdate(Offset(x.toFloatOrNull() ?: 0f, y.toFloatOrNull() ?: 0f))
        }) { Text("Update") }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    val image = remember {
        useResource("image.png") { loadImageBitmap(it) }
    }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var imagePosition by remember {
        mutableStateOf(Offset(-(image.width / 2).toFloat(), -(image.height / 2).toFloat()))
    }
    val textMeasurer = rememberTextMeasurer()
    var cameraPosition by remember { mutableStateOf(Offset.Zero) }
    LaunchedEffect(Unit) {
        val x = imagePosition.x - canvasSize.width / 2
        val y =  imagePosition.y - canvasSize.height / 2
        cameraPosition = Offset(x, y)
    }

    var keyboardEvents by remember { mutableStateOf(false) }
    val keyboardSpeed = 10

    var followMouse by remember { mutableStateOf(false) }

    var isAnimating by remember { mutableStateOf(false) }
    var velocity by remember { mutableStateOf(Offset(3f, 5f)) }
    var obstacles by remember { mutableStateOf<List<Obstacle>>(emptyList()) }
    val obstacleSize = 3
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            obstacles = List(obstacleSize) {
                val posX = (cameraPosition.x..cameraPosition.x + canvasSize.width).random()
                val posY = (cameraPosition.y..cameraPosition.y + canvasSize.height).random()
                val width = (0f..canvasSize.width * 0.1f).random()
                val height = (0f..canvasSize.height * 0.1f).random()

                Obstacle(
                    position = Offset(posX, posY),
                    size = Size(width, height),
                )
            }
        }

        while (isAnimating) {
            delay((1f / 60 * 1000).toLong())

            // Update position based on velocity
            imagePosition = imagePosition.copy(
                x = imagePosition.x + velocity.x, y = imagePosition.y + velocity.y
            )

            val leftBound = cameraPosition.x
            val rightBound = canvasSize.width + cameraPosition.x
            val topBound = cameraPosition.y
            val bottomBound = canvasSize.height + cameraPosition.y

            val vector = obstacles.collision(
                imagePosition,
                Size(image.width.toFloat(), image.height.toFloat())
            )
            velocity = velocity.copy(x = velocity.x * vector.x, y = velocity.y * vector.y)

            if (imagePosition.x < leftBound || imagePosition.x + image.width > rightBound) {
                velocity = velocity.copy(x = -velocity.x)
            }
            if (imagePosition.y < topBound || imagePosition.y + image.height > bottomBound) {
                velocity = velocity.copy(y = -velocity.y)
            }
        }
    }


    MaterialTheme {
        Row {
            Column(modifier = Modifier.weight(1f)) {
                PositionInput(imagePosition, onUpdate = { imagePosition = it })
                Button(onClick = {
                    val imagePos = imagePosition
                    val size = canvasSize
                    val x = image.width / 2f + imagePos.x - size.width / 2
                    val y = image.height / 2f + imagePos.y - size.height / 2
                    cameraPosition = Offset(x, y)
                }) {
                    Text("Center the image")
                }
                Button(onClick = { isAnimating = !isAnimating }) {
                    Text(if (isAnimating) "Stop Moving" else "Start moving")
                }
                Row {
                    Text("Keyboard events")
                    Checkbox(
                        checked = keyboardEvents,
                        onCheckedChange = { keyboardEvents = it },
                    )
                }
                Row {
                    Text("Follow mouse")
                    Checkbox(
                        checked = followMouse,
                        onCheckedChange = { followMouse = it },
                    )
                }
            }
            Canvas(modifier = Modifier.weight(5f).fillMaxSize().clipToBounds()
                .onSizeChanged { canvasSize = it.toSize() }.then(if (followMouse) {
                    Modifier.onPointerEvent(PointerEventType.Move, PointerEventPass.Main) { event ->
                        if (!followMouse) return@onPointerEvent
                        val mousePosition = event.awtEventOrNull ?: return@onPointerEvent
                        // TODO: does it take mouse position without applied transformation?
                        // TODO: if yes then, what is a better approach handling transformations?
                        val x = mousePosition.x.toFloat() + cameraPosition.x - image.width / 2
                        val y = mousePosition.y.toFloat() + cameraPosition.y - image.height / 2
                        imagePosition = Offset(x, y)
                    }
                } else Modifier)
                // TODO: use focus requester
                .focusable(enabled = true).clickable {}.onKeyEvent { event ->
                    if (!keyboardEvents) return@onKeyEvent false

                    when (event.key) {
                        Key.H -> imagePosition =
                            imagePosition.copy(x = imagePosition.x - keyboardSpeed)

                        Key.L -> imagePosition =
                            imagePosition.copy(x = imagePosition.x + keyboardSpeed)

                        Key.K -> imagePosition =
                            imagePosition.copy(y = imagePosition.y - keyboardSpeed)

                        Key.J -> imagePosition =
                            imagePosition.copy(y = imagePosition.y + keyboardSpeed)
                    }

                    false
                }) {
                drawIntoCanvas { canvas ->
                    canvas.withSave {
                        canvas.translate(-cameraPosition.x, -cameraPosition.y)
                        canvas.drawImage(image, topLeftOffset = imagePosition, paint = Paint())
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "Pos (${imagePosition.x};${imagePosition.y})",
                            topLeft = imagePosition,
                            style = TextStyle(fontSize = 50.sp, color = Color.Yellow)
                        )

                        obstacles.forEach {
                            drawRect(
                                topLeft = it.position,
                                size = it.size,
                                color = Color.Magenta,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun ClosedRange<Float>.random(): Float {
    return start + (endInclusive - start) * Random.nextFloat()
}

private fun List<Obstacle>.collision(pos: Offset, size: Size): Offset {
    for (obstacle in this) {
        val collidedHorizontally = obstacle.position.x + obstacle.size.width > pos.x &&
                obstacle.position.x < pos.x + size.width
        val collidedVertically = obstacle.position.y + obstacle.size.height > pos.y &&
                obstacle.position.y < pos.y + size.height

        if (collidedHorizontally && collidedVertically) {
            // Calculate overlap on each side
            val overlapX = minOf(
                pos.x + size.width - obstacle.position.x,
                obstacle.position.x + obstacle.size.width - pos.x
            )
            val overlapY = minOf(
                pos.y + size.height - obstacle.position.y,
                obstacle.position.y + obstacle.size.height - pos.y
            )

            return if (overlapX < overlapY) {
                // Horizontal collision, reflect X
                Offset(-1f, 1f)
            } else {
                // Vertical collision, reflect Y
                Offset(1f, -1f)
            }
        }
    }

    return Offset(1f, 1f)
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
