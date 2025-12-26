package com.example.mathforkids.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mathforkids.model.GameType
import com.example.mathforkids.model.GameResult
import com.example.mathforkids.util.rememberTTSHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ✅ Icon đa dạng (dùng cho Đếm / Cộng / Trừ)
private data class GameIcon(val emoji: String, val name: String)

private val GAME_ICONS = listOf(
    GameIcon("🍎", "quả táo"),
    GameIcon("⭐", "ngôi sao"),
    GameIcon("🎈", "bóng bay"),
    GameIcon("🍇", "chùm nho"),
    GameIcon("🍦", "cây kem"),
    GameIcon("🚗", "ô tô"),
    GameIcon("🐶", "chú chó"),
    GameIcon("🐱", "chú mèo"),
    GameIcon("🎨", "bảng màu"),
    GameIcon("🌟", "ngôi sao lấp lánh"),
)

@Composable
fun GameScreen(
    gameType: String,
    level: Int,
    onComplete: (GameResult) -> Unit,
    onBack: () -> Unit
) {
    var correctCount by remember { mutableStateOf(0) }
    var incorrectCount by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var isGameOver by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    val ttsHelper = rememberTTSHelper()
    val scope = rememberCoroutineScope()

    // Key làm mới câu hỏi (luôn đổi => fix "dơ" UI)
    val questionIndex = correctCount + incorrectCount

    val type = try {
        GameType.valueOf(gameType.uppercase())
    } catch (e: Exception) {
        GameType.COUNTING
    }

    // Kiểm tra hoàn thành level (3 câu đúng)
    LaunchedEffect(correctCount) {
        if (correctCount >= 3 && !showCompletionDialog && !isGameOver) {
            ttsHelper.speak("Chúc mừng bạn đã hoàn thành!")
            delay(2000)
            showCompletionDialog = true
        }
    }

    // Kiểm tra hết mạng
    LaunchedEffect(lives) {
        if (lives <= 0 && !showCompletionDialog && !isGameOver) {
            isGameOver = true
            ttsHelper.speak("Hết lượt chơi rồi! Cố gắng ở lần sau bé nhé!")
            delay(2000)
            showCompletionDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (type) {
            GameType.COUNTING -> CountingGameScreen(
                level = level,
                key = questionIndex,
                lives = lives,
                isGameOver = isGameOver,
                onCorrect = { correctCount++ },
                onIncorrect = { 
                    incorrectCount++
                    if (lives > 0) lives--
                },
                onBack = onBack
            )
            GameType.ADDITION -> AdditionGameScreen(
                level = level,
                key = questionIndex,
                lives = lives,
                isGameOver = isGameOver,
                onCorrect = { correctCount++ },
                onIncorrect = { 
                    incorrectCount++
                    if (lives > 0) lives--
                },
                onBack = onBack
            )
            GameType.SUBTRACTION -> SubtractionGameScreen(
                level = level,
                key = questionIndex,
                lives = lives,
                isGameOver = isGameOver,
                onCorrect = { correctCount++ },
                onIncorrect = { 
                    incorrectCount++
                    if (lives > 0) lives--
                },
                onBack = onBack
            )
            GameType.MATCHING -> MatchingGameScreen(
                level = level,
                key = questionIndex,
                lives = lives,
                isGameOver = isGameOver,
                onCorrect = { correctCount++ },
                onIncorrect = { 
                    incorrectCount++
                    if (lives > 0) lives--
                },
                onBack = onBack
            )

            GameType.WRITING -> WritingPracticeGame(
                level = level,
                onCorrect = { correctCount++ },
                onIncorrect = { 
                    incorrectCount++
                    if (lives > 0) lives--
                },
                onBack = onBack
            )
        }

        if (showCompletionDialog) {
            LevelCompletionDialog(
                correctAnswers = correctCount,
                incorrectAnswers = incorrectCount,
                isGameOver = isGameOver,
                stars = if (isGameOver) 0 else if (incorrectCount == 0) 3 else if (incorrectCount <= 2) 2 else 1,
                onContinue = {
                    if (!isGameOver) {
                        showCompletionDialog = false
                        scope.launch {
                            delay(300)
                            val total = correctCount + incorrectCount
                            val result = GameResult(
                                correctAnswers = correctCount,
                                totalQuestions = if (total == 0) 1 else total,
                                gameType = type,
                                level = level
                            )
                            onComplete(result)
                        }
                    } else {
                        // Game over: quay lại
                        onBack()
                    }
                },
                onPlayAgain = {
                    showCompletionDialog = false
                    correctCount = 0
                    incorrectCount = 0
                    lives = 3
                    isGameOver = false
                },
                onBack = {
                    showCompletionDialog = false
                    onBack()
                }
            )
        }
    }
}

// ----------------------------- DIALOG -----------------------------
@Composable
fun LevelCompletionDialog(
    correctAnswers: Int, 
    incorrectAnswers: Int, 
    isGameOver: Boolean = false,
    stars: Int,
    onContinue: () -> Unit, 
    onPlayAgain: () -> Unit, 
    onBack: () -> Unit
) {
    Dialog(onDismissRequest = onBack) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color.White).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isGameOver) {
                    Text("Hết lượt chơi rồi! Cố gắng ở lần sau bé nhé!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                    Spacer(Modifier.height(16.dp))
                    Text("😢", fontSize = 40.sp)
                    Text("Đúng: $correctAnswers câu", fontSize = 18.sp, color = Color.DarkGray)
                } else {
                    Text("Hoàn thành!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    Spacer(Modifier.height(16.dp))
                    Text("⭐".repeat(stars), fontSize = 40.sp)
                    Text("Đúng: $correctAnswers câu", fontSize = 18.sp, color = Color.DarkGray)
                }
                Spacer(Modifier.height(24.dp))
                
                if (isGameOver) {
                    // Nếu game over, chỉ có nút chơi lại và quay lại
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("Chơi lại", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text("Quay lại") }
                } else {
                    // Hoàn thành bình thường
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Lưu điểm & Tiếp tục", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onPlayAgain,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text("Chơi lại") }
                }
            }
        }
    }
}

// ----------------------------- UTILS -----------------------------
fun getDifficulty(level: Int): Int = if (level > 1000) level % 1000 else level

/**
 * Tính toán range số dựa trên level
 * Level 1-3: số từ 1-5
 * Level 4-6: số từ 1-10
 * Level 7-9: số từ 5-15
 * Level 10+: số từ 5-20
 */
fun getLevelRange(level: Int): IntRange {
    val actualLevel = getDifficulty(level)
    return when {
        actualLevel <= 3 -> 1..5      // Dễ: 1-5
        actualLevel <= 6 -> 1..10     // Trung bình: 1-10
        actualLevel <= 9 -> 5..15     // Khó: 5-15
        else -> 5..20                 // Rất khó: 5-20
    }
}

/**
 * Tính toán số tối đa cho phép cộng/trừ dựa trên level
 * Level 1-3: mỗi số từ 1-3
 * Level 4-6: mỗi số từ 1-5
 * Level 7-9: mỗi số từ 3-8
 * Level 10+: mỗi số từ 5-10
 */
fun getOperationRange(level: Int): IntRange {
    val actualLevel = getDifficulty(level)
    return when {
        actualLevel <= 3 -> 1..3      // Dễ: 1-3
        actualLevel <= 6 -> 1..5      // Trung bình: 1-5
        actualLevel <= 9 -> 3..8      // Khó: 3-8
        else -> 5..10                 // Rất khó: 5-10
    }
}

@Composable
private fun EmojiGrid(
    count: Int,
    emoji: String,
    perRow: Int = 6,
    tint: (Int) -> Color = { Color.Unspecified },
    sizeSp: Int = 36
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val rows = (count + perRow - 1) / perRow
        repeat(rows) { r ->
            Row(horizontalArrangement = Arrangement.Center) {
                for (i in (r * perRow) until minOf((r * perRow) + perRow, count)) {
                    Text(
                        emoji,
                        fontSize = sizeSp.sp,
                        color = tint(i),
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun VisualBlock(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun OperatorText(op: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(op, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

// ----------------------------- COUNTING -----------------------------
@Composable
fun CountingGameScreen(level: Int, key: Int, lives: Int, isGameOver: Boolean, onCorrect: () -> Unit, onIncorrect: () -> Unit, onBack: () -> Unit) {
    val range = getLevelRange(level)
    val number = remember(key) { range.random() }
    val icon = remember(key) { GAME_ICONS.random() }
    val options = remember(key) { generateOptions(number, range) }

    BaseGameLayout(
        title = "Bé hãy đếm xem có bao nhiêu ${icon.name} nhé",
        lives = lives,
        isGameOver = isGameOver,
        content = {
            VisualBlock("") {
                EmojiGrid(count = number, emoji = icon.emoji, perRow = 5, sizeSp = 50)
            }
        },
        options = options,
        correctAnswer = number,
        questionKey = key,
        onCorrect = onCorrect,
        onIncorrect = onIncorrect,
        onBack = onBack
    )
}

// ----------------------------- ADDITION (RÕ NHÓM A + NHÓM B) -----------------------------
@Composable
fun AdditionGameScreen(level: Int, key: Int, lives: Int, isGameOver: Boolean, onCorrect: () -> Unit, onIncorrect: () -> Unit, onBack: () -> Unit) {
    val range = getOperationRange(level)
    
    val a = remember(key) { range.random() }
    val b = remember(key) { range.random() }
    val result = a + b

    val icon = remember(key) { GAME_ICONS.random() } // ✅ nhiều icon như đếm
    val optionRange = maxOf(1, result - 5)..minOf(result + 5, range.last * 2)
    val options = remember(key) { generateOptions(result, optionRange) }

    BaseGameLayout(
        title = "Phép tính cộng:",
        lives = lives,
        isGameOver = isGameOver,
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // 2 khối rõ ràng: Nhóm A + Nhóm B
                VisualBlock("") { EmojiGrid(count = a, emoji = icon.emoji, perRow = 6, sizeSp = 40) }
                Spacer(Modifier.height(10.dp))
                OperatorText("+", Color(0xFF1976D2))
                Spacer(Modifier.height(10.dp))
                VisualBlock("") { EmojiGrid(count = b, emoji = icon.emoji, perRow = 6, sizeSp = 40) }

                Spacer(Modifier.height(14.dp))
                Text("$a + $b = ?", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
            }
        },
        options = options,
        correctAnswer = result,
        questionKey = key,
        onCorrect = onCorrect,
        onIncorrect = onIncorrect,
        onBack = onBack
    )
}

// ----------------------------- SUBTRACTION (A − B = CÒN LẠI) -----------------------------
@Composable
fun SubtractionGameScreen(level: Int, key: Int, lives: Int, isGameOver: Boolean, onCorrect: () -> Unit, onIncorrect: () -> Unit, onBack: () -> Unit) {
    val range = getOperationRange(level)
    
    val a = remember(key) { (range.first + 2..range.last).random() }
    val b = remember(key) { (range.first..minOf(a - 1, range.last)).random() }
    val result = a - b

    val icon = remember(key) { GAME_ICONS.random() } // ✅ nhiều icon như đếm
    val optionRange = maxOf(0, result - 5)..minOf(result + 5, range.last)
    val options = remember(key) { generateOptions(result, optionRange) }

    BaseGameLayout(
        title = "Phép tính trừ:",
        lives = lives,
        isGameOver = isGameOver,
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Khối 1: Có a
                VisualBlock("Nhóm A: $a") { EmojiGrid(count = a, emoji = icon.emoji, perRow = 6, sizeSp = 40) }
                Spacer(Modifier.height(10.dp))
                OperatorText("-", Color(0xFF1976D2))
                Spacer(Modifier.height(10.dp))
                VisualBlock("Nhóm B: $b") { EmojiGrid(count = b, emoji = icon.emoji, perRow = 6, sizeSp = 40) }

                Spacer(Modifier.height(14.dp))
                Text("$a - $b = ?", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))



            }
        },
        options = options,
        correctAnswer = result,
        questionKey = key,
        onCorrect = onCorrect,
        onIncorrect = onIncorrect,
        onBack = onBack
    )
}

// ----------------------------- MATCHING -----------------------------
@Composable
fun MatchingGameScreen(level: Int, key: Int, lives: Int, isGameOver: Boolean, onCorrect: () -> Unit, onIncorrect: () -> Unit, onBack: () -> Unit) {
    val actualLevel = getDifficulty(level)
    // Matching game: số có 2 chữ số, tăng dần theo level
    val range = when {
        actualLevel <= 3 -> 10..30    // Dễ: 10-30
        actualLevel <= 6 -> 20..60    // Trung bình: 20-60  
        actualLevel <= 9 -> 40..80    // Khó: 40-80
        else -> 50..99                // Rất khó: 50-99
    }
    val number = remember(key) { range.random() }
    val options = remember(key) { generateOptions(number, range) }

    BaseGameLayout(
        title = "Tìm số giống số này:",
        lives = lives,
        isGameOver = isGameOver,
        content = {
            Box(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.LightGray)
                    .padding(20.dp)
            ) {
                Text("$number", fontSize = 50.sp, fontWeight = FontWeight.Bold)
            }
        },
        options = options,
        correctAnswer = number,
        questionKey = key,
        onCorrect = onCorrect,
        onIncorrect = onIncorrect,
        onBack = onBack
    )
}

// ----------------------------- OPTIONS + LAYOUT (GIỮ NGUYÊN, FIX “DƠ”) -----------------------------
fun generateOptions(correct: Int, range: IntRange): List<Int> {
    val list = mutableListOf(correct)
    var attempts = 0
    while (list.size < 3 && attempts < 100) {
        val r = range.random()
        if (r != correct && r >= 0 && r !in list) list.add(r)
        attempts++
    }
    while (list.size < 3) list.add((list.maxOrNull() ?: correct) + 1)
    return list.shuffled()
}

@Composable
fun BaseGameLayout(
    title: String,
    lives: Int,
    isGameOver: Boolean,
    content: @Composable () -> Unit,
    options: List<Int>,
    correctAnswer: Int,
    questionKey: Int, // ✅ fix dơ: luôn đổi
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
    val ttsHelper = rememberTTSHelper()
    var answered by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }

    // ✅ FIX CHÍNH: reset theo questionKey (không phụ thuộc correctAnswer có trùng hay không)
    // ✅ Chỉ đọc TTS khi chưa game over
    LaunchedEffect(questionKey) {
        answered = false
        selectedAnswer = null
        // Tự động đọc câu hỏi khi hiển thị - NHƯNG CHỈ KHI CHƯA GAME OVER
        if (!isGameOver) {
            delay(300) // Đợi UI render xong
            ttsHelper.speak(title)
        }
    }

    LaunchedEffect(answered) {
        if (answered) {
            delay(500)
            if (selectedAnswer == correctAnswer) onCorrect() else onIncorrect()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF7FBFF), Color(0xFFFFFFFF))
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header với back button, lives và speaker button
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("←", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold) }
            
            // Hiển thị lives (3 trái tim)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    Text(
                        if (index < lives) "❤️" else "🤍",
                        fontSize = 28.sp
                    )
                }
            }
            
            // Button loa để đọc lại câu hỏi
            Button(
                onClick = { ttsHelper.speak(title) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("🔊", fontSize = 24.sp) }
        }

        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Medium)

        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { content() }

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { option ->
                val color = if (answered) {
                    if (option == correctAnswer) Color(0xFF4CAF50)
                    else if (option == selectedAnswer) Color(0xFFE53935)
                    else Color(0xFF2196F3)
                } else Color(0xFF2196F3)

                Button(
                    onClick = { if (!answered) { selectedAnswer = option; answered = true } },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("$option", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
