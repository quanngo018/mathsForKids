package com.example.mathforkids.ui.game

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathforkids.model.MnistClassifier
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Writing practice game for kids
 * Uses MNIST model to recognize handwritten digits
 */
@Composable
fun WritingPracticeGameScreen(
    level: Int,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
    var selectedDigit by remember { mutableStateOf<Int?>(null) }
    
    if (selectedDigit == null) {
        // Show digit selection screen
        DigitSelectionScreen(
            onDigitSelected = { digit -> selectedDigit = digit },
            onBack = onBack
        )
    } else {
        // Show writing practice for selected digit
        WritingPracticeScreen(
            targetDigit = selectedDigit!!,
            onCorrect = onCorrect,
            onIncorrect = onIncorrect,
            onBack = onBack,
            onNewDigit = { selectedDigit = null },
            onNextDigit = { 
                selectedDigit = if (selectedDigit!! < 9) selectedDigit!! + 1 else 0
            }
        )
    }
}

/**
 * Digit selection screen (0-9)
 */
@Composable
fun DigitSelectionScreen(
    onDigitSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("← Quay lại")
                }
                
                Text(
                    text = "Chọn số muốn luyện viết",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(100.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Grid of digit buttons (3x3 + 1)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1: 0-2
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (digit in 0..2) {
                        DigitButton(
                            digit = digit,
                            onClick = { onDigitSelected(digit) }
                        )
                    }
                }
                
                // Row 2: 3-5
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (digit in 3..5) {
                        DigitButton(
                            digit = digit,
                            onClick = { onDigitSelected(digit) }
                        )
                    }
                }
                
                // Row 3: 6-8
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for (digit in 6..8) {
                        DigitButton(
                            digit = digit,
                            onClick = { onDigitSelected(digit) }
                        )
                    }
                }
                
                // Row 4: 9 (centered)
                DigitButton(
                    digit = 9,
                    onClick = { onDigitSelected(9) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Nhấn vào số để bắt đầu luyện viết",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Individual digit button
 */
@Composable
fun DigitButton(
    digit: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Actual writing practice screen for a specific digit
 */
@Composable
fun WritingPracticeScreen(
    targetDigit: Int,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit,
    onNewDigit: () -> Unit,
    onNextDigit: () -> Unit
) {
    val context = LocalContext.current
    val classifier = remember { MnistClassifier(context) }
    
    // Drawing state
    var paths by remember(targetDigit) { mutableStateOf(listOf<DrawPath>()) }
    var currentPath by remember(targetDigit) { mutableStateOf(Path()) }
    var isDrawing by remember(targetDigit) { mutableStateOf(false) }
    
    // Feedback state
    var showFeedback by remember(targetDigit) { mutableStateOf(false) }
    var isCorrectAnswer by remember(targetDigit) { mutableStateOf(false) }
    var predictedDigit by remember(targetDigit) { mutableStateOf(-1) }
    var confidence by remember(targetDigit) { mutableStateOf(0f) }
    
    // Cleanup classifier on dispose
    DisposableEffect(Unit) {
        onDispose {
            classifier.close()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            GameHeader(
                title = "Bé tập viết ✏️",
                onBack = onBack
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Target digit - simple display without card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Hãy viết số: ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF424242)
                )
                Text(
                    text = targetDigit.toString(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(targetDigit) {
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
                                            strokeWidth = 40f
                                        )
                                        isDrawing = false
                                        currentPath = Path()
                                    }
                                }
                            )
                        }
                ) {
                    // Draw completed paths
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
                    
                    // Draw current path
                    if (isDrawing) {
                        drawPath(
                            path = currentPath,
                            color = Color.Black,
                            style = Stroke(
                                width = 40f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Clear button
                Button(
                    onClick = {
                        paths = emptyList()
                        currentPath = Path()
                        showFeedback = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Text(
                        "🗑️ Xóa",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Check button
                Button(
                    onClick = {
                        if (paths.isNotEmpty()) {
                            // Convert drawing to bitmap
                            val bitmap = pathsToBitmap(paths, 280, 280)
                            
                            // Classify digit
                            val (predicted, conf) = classifier.classify(bitmap)
                            predictedDigit = predicted
                            confidence = conf
                            
                            // Check if correct
                            isCorrectAnswer = predicted == targetDigit
                            showFeedback = true
                            
                            // Trigger callback
                            if (isCorrectAnswer) {
                                onCorrect()
                            } else {
                                onIncorrect()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    enabled = paths.isNotEmpty(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Text(
                        "✓ Kiểm tra",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            // Feedback
            AnimatedVisibility(
                visible = showFeedback,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrectAnswer) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isCorrectAnswer) "🎉 Đúng rồi!" else "😊 Thử lại nhé!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrectAnswer) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )

                        
                        if (isCorrectAnswer) {
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Row 1: Vẽ tiếp and Vẽ lại
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                                ) {
                                    // Button to continue to next digit
                                    Button(
                                        onClick = onNextDigit,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "Vẽ tiếp ➡️",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    // Button to try same digit again
                                    Button(
                                        onClick = {
                                            paths = emptyList()
                                            showFeedback = false
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2196F3)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "Vẽ lại 🔄",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    // Button to go back
                                    Button(
                                        onClick = onNewDigit,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFF9800)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "⬅️ Quay lại",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // Show 2 buttons for incorrect answer: "Quay lại" and "Vẽ lại"
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Button to go back to digit selection
                                Button(
                                    onClick = onNewDigit,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF9800)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "⬅️ Quay lại",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // Button to try again
                                Button(
                                    onClick = {
                                        paths = emptyList()
                                        showFeedback = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "Vẽ lại 🔄",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convert drawing paths to bitmap for MNIST model
 */
private fun pathsToBitmap(paths: List<DrawPath>, width: Int, height: Int): Bitmap {
    if (paths.isEmpty()) {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            android.graphics.Canvas(this).drawColor(android.graphics.Color.WHITE)
        }
    }
    
    // Find bounds of all paths
    var minX = Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var maxY = Float.MIN_VALUE
    
    paths.forEach { drawPath ->
        val bounds = android.graphics.RectF()
        drawPath.path.asAndroidPath().computeBounds(bounds, true)
        minX = minOf(minX, bounds.left)
        minY = minOf(minY, bounds.top)
        maxX = maxOf(maxX, bounds.right)
        maxY = maxOf(maxY, bounds.bottom)
    }
    
    val pathWidth = maxX - minX
    val pathHeight = maxY - minY
    
    // Calculate scale to fit in bitmap with padding
    val padding = 20f
    val availableWidth = width - 2 * padding
    val availableHeight = height - 2 * padding
    val scale = minOf(availableWidth / pathWidth, availableHeight / pathHeight)
    
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    // White background
    canvas.drawColor(android.graphics.Color.WHITE)
    
    // Calculate translation to center the drawing
    val translateX = padding + (availableWidth - pathWidth * scale) / 2 - minX * scale
    val translateY = padding + (availableHeight - pathHeight * scale) / 2 - minY * scale
    
    // Apply transformation
    canvas.translate(translateX, translateY)
    canvas.scale(scale, scale)
    
    // Draw all paths
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
        color = android.graphics.Color.BLACK
    }
    
    paths.forEach { drawPath ->
        paint.strokeWidth = drawPath.strokeWidth
        canvas.drawPath(drawPath.path.asAndroidPath(), paint)
    }
    
    return bitmap
}
