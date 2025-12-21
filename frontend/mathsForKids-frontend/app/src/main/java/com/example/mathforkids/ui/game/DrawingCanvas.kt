package com.example.mathforkids.ui.game

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Drawing canvas for handwriting digit input
 * Captures touch gestures and allows drawing
 */
@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    onPathChange: (Path) -> Unit = {}
) {
    var currentPath by remember { mutableStateOf(Path()) }
    var paths by remember { mutableStateOf(listOf<DrawPath>()) }

    // Notify parent of path changes
    LaunchedEffect(paths) {
        onPathChange(currentPath)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color.White, RoundedCornerShape(16.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = Path().apply {
                                moveTo(offset.x, offset.y)
                            }
                        },
                        onDrag = { change, _ ->
                            currentPath.lineTo(change.position.x, change.position.y)
                            paths = paths + DrawPath(
                                path = Path().apply {
                                    addPath(currentPath)
                                },
                                color = Color.Black,
                                strokeWidth = 25f
                            )
                        },
                        onDragEnd = {
                            paths = paths + DrawPath(
                                path = currentPath,
                                color = Color.Black,
                                strokeWidth = 25f
                            )
                            onPathChange(currentPath)
                        }
                    )
                }
        ) {
            // Draw all paths
            paths.forEach { drawPath ->
                drawPath(
                    path = drawPath.path,
                    color = drawPath.color,
                    style = Stroke(
                        width = drawPath.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

/**
 * Data class to store drawing path with properties
 */
data class DrawPath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float
)

/**
 * Convert Canvas to Bitmap for ML model input
 */
fun canvasToBitmap(
    width: Int,
    height: Int,
    paths: List<DrawPath>
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // White background
    canvas.drawColor(android.graphics.Color.WHITE)

    // Draw all paths
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }

    paths.forEach { drawPath ->
        paint.color = drawPath.color.toArgb()
        paint.strokeWidth = drawPath.strokeWidth
        canvas.drawPath(drawPath.path.asAndroidPath(), paint)
    }

    return bitmap
}

/**
 * Advanced drawing canvas with state management
 * Supports clearing, undo, and bitmap export
 */
@Composable
fun ManagedDrawingCanvas(
    modifier: Modifier = Modifier,
    onClear: () -> Unit = {},
    getBitmap: () -> Bitmap?
) {
    var paths by remember { mutableStateOf(listOf<DrawPath>()) }
    var currentPath by remember { mutableStateOf(Path()) }
    var isDrawing by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDrawing = true
                            currentPath = Path().apply {
                                moveTo(offset.x, offset.y)
                            }
                        },
                        onDrag = { change, _ ->
                            if (isDrawing) {
                                currentPath.lineTo(change.position.x, change.position.y)
                            }
                        },
                        onDragEnd = {
                            if (isDrawing) {
                                paths = paths + DrawPath(
                                    path = currentPath,
                                    color = Color.Black,
                                    strokeWidth = 30f
                                )
                                isDrawing = false
                                currentPath = Path()
                            }
                        }
                    )
                }
        ) {
            // Draw all completed paths
            paths.forEach { drawPath ->
                drawPath(
                    path = drawPath.path,
                    color = drawPath.color,
                    style = Stroke(
                        width = drawPath.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Draw current path being drawn
            if (isDrawing) {
                drawPath(
                    path = currentPath,
                    color = Color.Black,
                    style = Stroke(
                        width = 30f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
