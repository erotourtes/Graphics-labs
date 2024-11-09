import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.skia.Matrix33
import kotlin.math.atan

@Composable
fun Form(
    objectPosition: MutableState<Offset>,
    objectRotation: MutableState<Float>,
    objectScale: MutableState<Float>,
    isMirror: MutableState<Boolean>,
    canvasSize: Size,
    modifier: Modifier
) {
    var position by objectPosition;
    var rotation by objectRotation;
    var scale by objectScale;
    var mirror by isMirror;

    Column(modifier = modifier) {
        Text("Move X ${position.x}")
        Slider(
            value = position.x,
            onValueChange = { position = position.copy(x = it) },
            valueRange = 0f..canvasSize.width,
        )

        Text("Move Y ${position.y}")
        Slider(
            value = position.y,
            onValueChange = { position = position.copy(y = it) },
            valueRange = 0f..canvasSize.height
        )

        Text("Rotate $rotation")
        Slider(
            value = rotation,
            onValueChange = { rotation = it },
            valueRange = 0f..360f
        )

        Text("Scale $scale")
        Slider(
            value = scale,
            onValueChange = { scale = it },
            valueRange = 0f..5f,
        )

        Text("Mirror $mirror")
        Checkbox(
            checked = mirror,
            onCheckedChange = { mirror = it }
        )
    }
}


/**
 *  x    | a b | p
 *  y  * | c d | q
 *       | - - - -
 *  1    | m n | s
 *
 * if multiplying (x, y, 1) * matrix33 = (x', y', 1)
 * x' = (ax + by + p) / (mx + ny + s)
 * y' = (cx + dy + q) / (mx + ny + s)
 */
fun Offset.applyMatrix(matrix33: Matrix33): Offset {
    val (x, y) = this
    val m = matrix33.mat

//    val x1 = m[0] * x + m[3] * y + m[6]
//    val y1 = m[1] * x + m[4] * y + m[7]
//    val h = m[2] * x + m[5] * y + m[8]
    val x1 = m[0] * x + m[1] * y + m[2]
    val y1 = m[3] * x + m[4] * y + m[5]
    val h = m[6] * x + m[7] * y + m[8]

    return Offset(x1 / h, y1 / h)
}

fun List<Matrix33>.applyMatrices(): Matrix33 {
    return this.reduce { acc, matrix -> acc.makeConcat(matrix) }
}

@Composable
@Preview
fun App() {
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val objectPosition = remember { mutableStateOf(Offset.Zero) }
    val objectRotation = remember { mutableStateOf(0f) }
    val objectScale = remember { mutableStateOf(1f) }
    val isMirror = remember { mutableStateOf(false) }

    LaunchedEffect(canvasSize) {
        objectPosition.value = Offset(canvasSize.width / 2, canvasSize.height / 2)
    }

    val scale by objectScale
    val rotation by objectRotation
    val pos by objectPosition
    val mirror by isMirror

    val mirrorMatrix = listOf(
        Matrix33.makeRotate(45f),
        Matrix33.makeScale(1f, -1f),
        Matrix33.makeRotate(-45f),
    )

    val posMatrix =
        listOf(
            if (mirror) mirrorMatrix.applyMatrices() else Matrix33.makeScale(1f),
            Matrix33.makeTranslate(pos.x, pos.y),
            Matrix33.makeScale(scale),
            Matrix33.makeRotate(rotation),
            Matrix33.makeTranslate(-75f, -50f)
        ).applyMatrices()

    val figurePos = remember {
        listOf(
            Offset(0f, 0f),
            Offset(0f, 100f),
            Offset(150f, 100f),
            Offset(150f, 50f),
            Offset(50f, 50f),
            Offset(50f, 0f),
        )
    }

    val path = figurePos.map { it.applyMatrix(posMatrix) }.let {
        Path().apply {
            it.forEachIndexed { index, offset ->
                if (index == 0) moveTo(offset.x, offset.y)
                else lineTo(offset.x, offset.y)
            }
            close()
        }
    }


    MaterialTheme {
        Row {
            Form(
                objectPosition = objectPosition,
                objectRotation = objectRotation,
                objectScale = objectScale,
                isMirror = isMirror,
                canvasSize = canvasSize,
                modifier = Modifier.weight(1f).fillMaxSize()
            )
            Canvas(
                modifier = Modifier
                    .weight(5f).fillMaxSize().clipToBounds()
                    .onSizeChanged { canvasSize = it.toSize() }) {

                drawIntoCanvas { canvas ->
                    canvas.withSave {
                        // Translate
//                        canvas.translate(size.width / 2, size.height / 2)

                        val paint = Paint()
                        paint.strokeWidth = 2f
                        paint.style = PaintingStyle.Stroke
                        canvas.drawPath(path, paint)
                        canvas.drawLine(Offset(0f, 0f), Offset(size.width, size.width), Paint())
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
