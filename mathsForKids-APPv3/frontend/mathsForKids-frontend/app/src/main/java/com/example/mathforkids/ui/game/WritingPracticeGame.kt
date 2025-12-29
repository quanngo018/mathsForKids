package com.example.mathforkids.ui.game

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathforkids.model.MnistClassifier
import com.example.mathforkids.util.rememberSoundHelper
import com.example.mathforkids.util.rememberTTSHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- LỚP DỮ LIỆU ĐƯỜNG VẼ (Thêm vào đây để chắc chắn không bị thiếu) ---


/**
 * Game tập viết số sử dụng AI (TensorFlow Lite)
 */
@Composable
fun WritingPracticeGame( // <--- ĐÃ SỬA TÊN CHO KHỚP VỚI GAMESCREEN
    level: Int,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
    var selectedDigit by remember { mutableStateOf<Int?>(null) }

    if (selectedDigit == null) {
        DigitSelectionScreen(
            onDigitSelected = { digit -> selectedDigit = digit },
            onBack = onBack
        )
    } else {
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

@Composable
fun DigitSelectionScreen(onDigitSelected: (Int) -> Unit, onBack: () -> Unit) {
    val ttsHelper = rememberTTSHelper()

    // Đọc hướng dẫn khi vào màn hình chọn số
    LaunchedEffect(Unit) {
        delay(500)
        ttsHelper.speak("Bé hãy chọn số muốn tập viết nhé")
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.stop()
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)))).padding(16.dp)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("← Quay lại") }
                Text("Chọn số tập viết", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(80.dp))
            }
            Spacer(Modifier.height(32.dp))

            // Grid 3x3
            for (row in 0..2) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (col in 0..2) {
                        val digit = row * 3 + col
                        DigitButton(digit) { onDigitSelected(digit) }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            DigitButton(9) { onDigitSelected(9) }
        }
    }
}

@Composable
fun DigitButton(digit: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(4.dp)
    ) {
        Text(text = digit.toString(), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

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
    // Khởi tạo AI Classifier
    val classifier = remember { MnistClassifier(context) }
    val soundHelper = rememberSoundHelper()
    val ttsHelper = rememberTTSHelper()
    val scope = rememberCoroutineScope()

    var paths by remember(targetDigit) { mutableStateOf(listOf<DrawPath>()) }
    var currentPath by remember(targetDigit) { mutableStateOf(Path()) }
    var isDrawing by remember(targetDigit) { mutableStateOf(false) }
    var showFeedback by remember(targetDigit) { mutableStateOf(false) }
    var isCorrectAnswer by remember(targetDigit) { mutableStateOf(false) }

    // Đọc hướng dẫn khi bắt đầu
    LaunchedEffect(targetDigit) {
        delay(500)
        ttsHelper.speak("Bé hãy viết số $targetDigit trên bảng nhé")
    }

    DisposableEffect(Unit) {
        onDispose {
            classifier.close()
            ttsHelper.stop()
        }
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)))).padding(8.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Header
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("←", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                Text("Bé tập viết số $targetDigit", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(10.dp))

            // Bảng vẽ (Canvas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize().pointerInput(targetDigit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDrawing = true
                                currentPath = Path().apply { moveTo(offset.x, offset.y) }
                            },
                            onDrag = { change, _ ->
                                if (isDrawing) currentPath.lineTo(change.position.x, change.position.y)
                            },
                            onDragEnd = {
                                if (isDrawing) {
                                    paths = paths + DrawPath(currentPath, Color.Black, 50f) // Nét bút đậm hơn chút (50f)
                                    isDrawing = false
                                    currentPath = Path()
                                }
                            }
                        )
                    }
                ) {
                    paths.forEach { p ->
                        drawPath(p.path, p.color, style = Stroke(width = p.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                    if (isDrawing) {
                        drawPath(currentPath, Color.Black, style = Stroke(width = 50f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Nút bấm
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { paths = emptyList(); currentPath = Path(); showFeedback = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) { Text("Xóa làm lại") }

                Button(
                    onClick = {
                        if (paths.isNotEmpty()) {
                            val bitmap = pathsToBitmap(paths, 280, 280)
                            val (predicted, _) = classifier.classify(bitmap)

                            isCorrectAnswer = (predicted == targetDigit)
                            showFeedback = true

                            // Phát sound effect và đọc kết quả
                            scope.launch {
                                if (isCorrectAnswer) {
                                    soundHelper.playCorrectSound()
                                    delay(100)
                                    ttsHelper.speak("Chính xác! Bé giỏi quá!")
                                    onCorrect()
                                } else {
                                    soundHelper.playWrongSound()
                                    delay(100)
                                    ttsHelper.speak("Sai rồi, bé thử lại nhé!")
                                    onIncorrect()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    enabled = paths.isNotEmpty()
                ) { Text("✓ Kiểm tra") }
            }

            // Thông báo kết quả
            if (showFeedback) {
                Spacer(Modifier.height(10.dp))
                Card(colors = CardDefaults.cardColors(containerColor = if (isCorrectAnswer) Color(0xFFC8E6C9) else Color(0xFFFFCDD2))) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isCorrectAnswer) "🎉 Chính xác! Bé giỏi quá" else "Sai rồi, bé thử lại nhé!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))
                        Row {
                            if (isCorrectAnswer) {
                                Button(onClick = onNextDigit) { Text("Số tiếp theo ➡️") }
                            } else {
                                Button(onClick = { paths = emptyList(); showFeedback = false }) { Text("Viết lại 🔄") }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Hàm chuyển nét vẽ thành ảnh Bitmap để AI đọc

// Hàm chuyển nét vẽ thành ảnh Bitmap (Phiên bản "Bơm mực" cho nét đậm)
fun pathsToBitmap(paths: List<DrawPath>, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE) // Nền trắng

    if (paths.isEmpty()) return bitmap

    // 1. Tính toán vùng vẽ của người dùng
    var minX = Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var maxY = Float.MIN_VALUE

    paths.forEach { p ->
        val bounds = android.graphics.RectF()
        p.path.asAndroidPath().computeBounds(bounds, true)
        minX = minOf(minX, bounds.left)
        minY = minOf(minY, bounds.top)
        maxX = maxOf(maxX, bounds.right)
        maxY = maxOf(maxY, bounds.bottom)
    }

    val pathWidth = maxX - minX
    val pathHeight = maxY - minY

    // Nếu vẽ 1 chấm thì coi như kích thước là 1 để tránh chia cho 0
    val safeWidth = if (pathWidth < 1f) 1f else pathWidth
    val safeHeight = if (pathHeight < 1f) 1f else pathHeight

    // 2. Tính tỷ lệ phóng to/thu nhỏ để hình vừa khít khung 280x280
    val padding = 30f // Chừa lề một chút
    val scale = minOf(
        (width - 2 * padding) / safeWidth,
        (height - 2 * padding) / safeHeight
    )

    // 3. Căn giữa hình vẽ
    val translateX = (width - safeWidth * scale) / 2f - minX * scale
    val translateY = (height - safeHeight * scale) / 2f - minY * scale

    canvas.translate(translateX, translateY)
    canvas.scale(scale, scale)

    // 4. Cấu hình bút vẽ (QUAN TRỌNG NHẤT)
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
        color = android.graphics.Color.BLACK
    }

    // [BÍ KÍP] Luôn giữ nét vẽ đậm khoảng 40 pixel trên ảnh đích
    // Dù bạn vẽ to hay nhỏ, nét bút sẽ tự điều chỉnh để AI nhìn rõ nhất
    val targetStrokeWidth = 45f
    paint.strokeWidth = targetStrokeWidth / scale

    paths.forEach { p ->
        canvas.drawPath(p.path.asAndroidPath(), paint)
    }

    return bitmap
}