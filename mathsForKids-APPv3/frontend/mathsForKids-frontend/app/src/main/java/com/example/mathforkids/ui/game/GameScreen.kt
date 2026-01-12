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
import com.example.mathforkids.util.rememberSoundHelper
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import com.example.mathforkids.util.SettingsManager
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

// ✅ Tên người cho câu hỏi cộng/trừ
private val PERSON_NAMES = listOf("anh Quân", "anh Huy", "anh Nam")

@Composable
fun GameScreen(
    gameType: String,
    level: Int,
    mode: String = "practice",
    onQuestionAnswered: (GameResult) -> Unit = {},
    onComplete: (GameResult) -> Unit,
    onBack: () -> Unit
) {
    var correctCount by remember { mutableStateOf(0) }
    var incorrectCount by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var xp by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    val ttsHelper = rememberTTSHelper()
    val scope = rememberCoroutineScope()

    // Timer for Test mode
    var timerSeconds by remember { mutableStateOf(0) }
    LaunchedEffect(mode, isGameOver, showCompletionDialog) {
        if (mode == "test" && !isGameOver && !showCompletionDialog) {
            while (true) {
                delay(1000)
                timerSeconds++
            }
        }
    }

    // Key làm mới câu hỏi (luôn đổi => fix "dơ" UI)
    val questionIndex = correctCount + incorrectCount

    val type = try {
        GameType.valueOf(gameType.uppercase())
    } catch (e: Exception) {
        GameType.COUNTING
    }

    // Dọc dẹp TTS khi màn hình bị hủy
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.stop()
        }
    }

    // Logic xử lý khi trả lời xong 1 câu
    fun onAnswer(isCorrect: Boolean) {
        if (isCorrect) {
            correctCount++
            if (mode == "practice") {
                xp += 1
            }
        } else {
            incorrectCount++
            if (mode == "test") {
                lives--
            }
        }

        if (mode == "test") {
            val result = GameResult(
                correctAnswers = correctCount,
                totalQuestions = correctCount + incorrectCount,
                gameType = type,
                level = level
            )
            onQuestionAnswered(result)
        }
    }

    // Kiểm tra hoàn thành
    LaunchedEffect(correctCount, incorrectCount, lives, xp) {
        if (showCompletionDialog || isGameOver) return@LaunchedEffect

        if (mode == "test") {
            if (lives <= 0) {
                isGameOver = true
                showCompletionDialog = true
                ttsHelper.stop()
                delay(100)
                ttsHelper.speak("Hết lượt rồi! Cố gắng ở lần sau bé nhé!")
                delay(2000)
            } else if (correctCount + incorrectCount >= 10) {
                showCompletionDialog = true
                ttsHelper.stop()
                delay(100)
                ttsHelper.speak("Đã hoàn thành bài kiểm tra!")
                delay(2000)
            }
        } else if (mode == "practice") {
            if (xp >= 10) {
                showCompletionDialog = true
                ttsHelper.stop()
                delay(100)
                ttsHelper.speak("Chúc mừng bé đã hoàn thành!")
                delay(2000)
            }
        } else {
            // Normal logic (Legacy or if mode is not specified correctly)
            if (correctCount >= 3) {
                showCompletionDialog = true
                ttsHelper.stop()
                delay(100)
                ttsHelper.speak("Chúc mừng bé đã hoàn thành!")
                delay(2000)
            } else if (lives <= 0) {
                isGameOver = true
                showCompletionDialog = true
                ttsHelper.stop()
                delay(100)
                ttsHelper.speak("Hết lượt rồi! Cố gắng ở lần sau bé nhé!")
                delay(2000)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (type) {
            GameType.COUNTING -> CountingGameScreen(
                level = level,
                key = questionIndex,
                lives = lives,
                xp = xp,
                mode = mode,
                timerSeconds = timerSeconds,
                score = correctCount,
                total = correctCount + incorrectCount,
                isGameOver = isGameOver,
                showCompletionDialog = showCompletionDialog,
                onCorrect = { onAnswer(true) },
                onIncorrect = { onAnswer(false) },
                onBack = onBack
            )
            GameType.ADDITION -> AdditionGameScreen(
                level = level,
                key = questionIndex,
                lives = lives,
                xp = xp,
                mode = mode,
                timerSeconds = timerSeconds,
                score = correctCount,
                total = correctCount + incorrectCount,
                isGameOver = isGameOver,
                showCompletionDialog = showCompletionDialog,
                onCorrect = { onAnswer(true) },
                onIncorrect = { onAnswer(false) },
                onBack = onBack
            )
            GameType.SUBTRACTION -> SubtractionGameScreen(
                level = level,
                key = questionIndex,
                lives = lives,
                xp = xp,
                mode = mode,
                timerSeconds = timerSeconds,
                score = correctCount,
                total = correctCount + incorrectCount,
                isGameOver = isGameOver,
                showCompletionDialog = showCompletionDialog,
                onCorrect = { onAnswer(true) },
                onIncorrect = { onAnswer(false) },
                onBack = onBack
            )
            GameType.MATCHING -> MatchingGameScreen(
                level = level,
                key = questionIndex,
                lives = lives,
                xp = xp,
                mode = mode,
                timerSeconds = timerSeconds,
                score = correctCount,
                total = correctCount + incorrectCount,
                isGameOver = isGameOver,
                showCompletionDialog = showCompletionDialog,
                onCorrect = { onAnswer(true) },
                onIncorrect = { onAnswer(false) },
                onBack = onBack
            )

            GameType.WRITING -> WritingPracticeGame(
                level = level,
                onCorrect = { onAnswer(true) },
                onIncorrect = { onAnswer(false) },
                onBack = onBack
            )
        }

        if (showCompletionDialog) {
            LevelCompletionDialog(
                mode = mode,
                correctAnswers = correctCount,
                incorrectAnswers = incorrectCount,
                isGameOver = isGameOver,
                stars = if (isGameOver) 0 else if (incorrectCount == 0) 3 else if (incorrectCount <= 2) 2 else 1,
                onContinue = {
                    if (!isGameOver) {
                        if (mode == "practice") {
                            // Practice mode: mark current level as completed and go back to level selection
                            val result = GameResult(
                                correctAnswers = correctCount,
                                totalQuestions = correctCount + incorrectCount,
                                gameType = type,
                                level = level // Current level (will be marked as completed)
                            )
                            onComplete(result)
                        } else {
                            // Test mode: save score and complete
                            scope.launch {
                                val total = correctCount + incorrectCount
                                val result = GameResult(
                                    correctAnswers = correctCount,
                                    totalQuestions = if (total == 0) 1 else total,
                                    gameType = type,
                                    level = level
                                )
                                onComplete(result)
                            }
                        }
                    } else {
                        onBack()
                    }
                },
                onPlayAgain = {
                    showCompletionDialog = false
                    correctCount = 0
                    incorrectCount = 0
                    lives = 3
                    xp = 0
                    isGameOver = false
                    timerSeconds = 0
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
    mode: String = "practice",
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
                    Text("Hết lượt rồi! Cố gắng ở lần sau bé nhé!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                    Spacer(Modifier.height(16.dp))
                    Text("😢", fontSize = 40.sp)
                    Text("Đúng: $correctAnswers câu", fontSize = 18.sp, color = Color.DarkGray)
                } else {
                    Text("Hoàn thành!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    Spacer(Modifier.height(16.dp))
                    Text("⭐".repeat(stars), fontSize = 40.sp)
                    Text("Đúng: $correctAnswers câu", fontSize = 18.sp, color = Color.DarkGray)
                    if (incorrectAnswers > 0) {
                        Text("Sai: $incorrectAnswers câu", fontSize = 18.sp, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(24.dp))

                if (isGameOver) {
                    // Nếu game over, chỉ có nút làm lại và quay lại
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("Làm lại", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        Text(
                            if (mode == "practice") "Tiếp tục" else "Lưu điểm & Tiếp tục",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onPlayAgain,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text("Làm lại") }
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
    sizeSp: Int = 56
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val rows = (count + perRow - 1) / perRow
        
        // ✅ Tự động scale kích thước - tính toán dựa trên số lượng thực tế trên mỗi hàng
        val maxItemsInRow = minOf(count, perRow)
        
        // Tính kích thước cơ bản dựa trên số item trên hàng và tổng số lượng
        val baseSize = when {
            count == 1 -> 80.coerceAtMost(sizeSp)      // 1 icon: cực to
            count == 2 -> 70.coerceAtMost(sizeSp)      // 2 icon: rất to
            count == 3 -> 62.coerceAtMost(sizeSp)      // 3 icon: to
            count <= 5 -> 54.coerceAtMost(sizeSp)      // 4-5 icon: to vừa
            count <= 8 -> 48.coerceAtMost(sizeSp)      // 6-8 icon: vừa
            count <= 12 -> 42.coerceAtMost(sizeSp)     // 9-12 icon: vừa nhỏ
            count <= 18 -> 38.coerceAtMost(sizeSp)     // 13-18 icon: nhỏ
            count <= 24 -> 34.coerceAtMost(sizeSp)     // 19-24 icon: nhỏ hơn
            count <= 30 -> 30.coerceAtMost(sizeSp)     // 25-30 icon: rất nhỏ
            else -> 26.coerceAtMost(sizeSp)            // 31+ icon: cực nhỏ
        }
        
        // Điều chỉnh thêm dựa trên số hàng để tránh tràn theo chiều dọc
        val rowAdjustedSize = when {
            rows > 5 -> (baseSize * 0.75f).toInt()      // >5 hàng: giảm 25%
            rows > 4 -> (baseSize * 0.82f).toInt()      // >4 hàng: giảm 18%
            rows > 3 -> (baseSize * 0.88f).toInt()      // >3 hàng: giảm 12%
            rows > 2 -> (baseSize * 0.94f).toInt()      // >2 hàng: giảm 6%
            else -> baseSize
        }
        
        // Điều chỉnh thêm dựa trên số icon trên mỗi hàng để tránh tràn theo chiều ngang
        val finalSize = when {
            maxItemsInRow >= 6 -> (rowAdjustedSize * 0.90f).toInt()  // 6+ icon/hàng: giảm 10%
            maxItemsInRow >= 5 -> (rowAdjustedSize * 0.95f).toInt()  // 5 icon/hàng: giảm 5%
            else -> rowAdjustedSize
        }
        
        // Đảm bảo không quá nhỏ (tối thiểu 20sp)
        val safeSize = maxOf(finalSize, 20)
        
        repeat(rows) { r ->
            Row(horizontalArrangement = Arrangement.Center) {
                for (i in (r * perRow) until minOf((r * perRow) + perRow, count)) {
                    Text(
                        emoji,
                        fontSize = safeSize.sp,
                        color = tint(i),
                        modifier = Modifier.padding(3.dp)  // Giảm padding từ 4dp xuống 3dp
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
fun CountingGameScreen(
    level: Int,
    key: Int,
    lives: Int,
    xp: Int = 0,
    mode: String = "practice",
    timerSeconds: Int = 0,
    score: Int = 0,
    total: Int = 0,
    isGameOver: Boolean,
    showCompletionDialog: Boolean,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
    val range = getLevelRange(level)
    val number = remember(key) { range.random() }
    val icon = remember(key) { GAME_ICONS.random() }
    val options = remember(key) { generateOptions(number, range) }

    BaseGameLayout(
        title = "Bé hãy đếm xem có bao nhiêu ${icon.name} nhé",
        lives = lives,
        xp = xp,
        mode = mode,
        timerSeconds = timerSeconds,
        score = score,
        total = total,
        isGameOver = isGameOver,
        showCompletionDialog = showCompletionDialog,
        content = {
            VisualBlock("") {
                EmojiGrid(count = number, emoji = icon.emoji, perRow = 5, sizeSp = 70)
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

// ----------------------------- ADDITION -----------------------------
@Composable
fun AdditionGameScreen(
    level: Int,
    key: Int,
    lives: Int,
    xp: Int = 0,
    mode: String = "practice",
    timerSeconds: Int = 0,
    score: Int = 0,
    total: Int = 0,
    isGameOver: Boolean,
    showCompletionDialog: Boolean,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
    val range = getOperationRange(level)

    val a = remember(key) { range.random() }
    val b = remember(key) { range.random() }
    val result = a + b

    val icon = remember(key) { GAME_ICONS.random() }
    val person1 = remember(key) { PERSON_NAMES.random() }
    val person2 = remember(key) { PERSON_NAMES.filter { it != person1 }.random() }

    val optionRange = maxOf(1, result - 5)..minOf(result + 5, range.last * 2)
    val options = remember(key) { generateOptions(result, optionRange) }

    // Câu hỏi có ngữ cảnh
    val questionText = "$person1 có $a ${icon.name}, $person2 cho $person1 thêm $b ${icon.name}. Hỏi $person1 có bao nhiêu ${icon.name}?"

    BaseGameLayout(
        title = questionText,
        lives = lives,
        xp = xp,
        mode = mode,
        timerSeconds = timerSeconds,
        score = score,
        total = total,
        isGameOver = isGameOver,
        showCompletionDialog = showCompletionDialog,
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // 2 khối rõ ràng: Nhóm A + Nhóm B
                VisualBlock("") { EmojiGrid(count = a, emoji = icon.emoji, perRow = 6, sizeSp = 60) }
                Spacer(Modifier.height(10.dp))
                OperatorText("+", Color(0xFF1976D2))
                Spacer(Modifier.height(10.dp))
                VisualBlock("") { EmojiGrid(count = b, emoji = icon.emoji, perRow = 6, sizeSp = 60) }

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
fun SubtractionGameScreen(
    level: Int,
    key: Int,
    lives: Int,
    xp: Int = 0,
    mode: String = "practice",
    timerSeconds: Int = 0,
    score: Int = 0,
    total: Int = 0,
    isGameOver: Boolean,
    showCompletionDialog: Boolean,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
    val range = getOperationRange(level)

    val a = remember(key) { (range.first + 2..range.last).random() }
    val b = remember(key) { (range.first..minOf(a - 1, range.last)).random() }
    val result = a - b

    val icon = remember(key) { GAME_ICONS.random() }
    val person1 = remember(key) { PERSON_NAMES.random() }
    val person2 = remember(key) { PERSON_NAMES.filter { it != person1 }.random() }

    val optionRange = maxOf(0, result - 5)..minOf(result + 5, range.last)
    val options = remember(key) { generateOptions(result, optionRange) }

    // Câu hỏi có ngữ cảnh
    val questionText = "$person1 có $a ${icon.name}, $person1 cho $person2 $b ${icon.name}. Hỏi $person1 còn bao nhiêu ${icon.name}?"

    BaseGameLayout(
        title = questionText,
        lives = lives,
        xp = xp,
        mode = mode,
        timerSeconds = timerSeconds,
        score = score,
        total = total,
        isGameOver = isGameOver,
        showCompletionDialog = showCompletionDialog,
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Khối 1: Có a
                VisualBlock("$person1 có: $a ${icon.name}") { EmojiGrid(count = a, emoji = icon.emoji, perRow = 6, sizeSp = 60) }
                Spacer(Modifier.height(10.dp))
                OperatorText("-", Color(0xFF1976D2))
                Spacer(Modifier.height(10.dp))
                VisualBlock("Cho $person2: $b ${icon.name}") { EmojiGrid(count = b, emoji = icon.emoji, perRow = 6, sizeSp = 60) }

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
fun MatchingGameScreen(
    level: Int,
    key: Int,
    lives: Int,
    xp: Int = 0,
    mode: String = "practice",
    timerSeconds: Int = 0,
    score: Int = 0,
    total: Int = 0,
    isGameOver: Boolean,
    showCompletionDialog: Boolean,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
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
        xp = xp,
        mode = mode,
        timerSeconds = timerSeconds,
        score = score,
        total = total,
        isGameOver = isGameOver,
        showCompletionDialog = showCompletionDialog,
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
    xp: Int = 0,
    mode: String = "practice",
    timerSeconds: Int = 0,
    score: Int = 0,
    total: Int = 0,
    isGameOver: Boolean,
    showCompletionDialog: Boolean,
    content: @Composable () -> Unit,
    options: List<Int>,
    correctAnswer: Int,
    questionKey: Int, // ✅ fix dơ: luôn đổi
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
    val ttsHelper = rememberTTSHelper()
    val soundHelper = rememberSoundHelper()
    var answered by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    val fontScale = SettingsManager.fontScale

    // ✅ FIX CHÍNH: reset theo questionKey (không phụ thuộc correctAnswer có trùng hay không)
    LaunchedEffect(questionKey) {
        answered = false
        selectedAnswer = null
    }

    // ✅ Tách riêng logic đọc TTS để restart khi isGameOver/showCompletionDialog thay đổi
    // Khi showCompletionDialog chuyển thành true, effect cũ (đang delay) sẽ bị hủy -> KHÔNG ĐỌC NỮA
    LaunchedEffect(questionKey, isGameOver, showCompletionDialog) {
        // Chặn ngay lập tức nếu game đã kết thúc hoặc đang hiển dialog
        if (isGameOver || showCompletionDialog) {
            return@LaunchedEffect
        }

        // Tăng delay an toàn để đảm bảo state đã cập nhật
        delay(if (questionKey == 0) 800 else 400)

        // Kiểm tra lại một lần nữa trước khi đọc (dù effect restart đã handle, nhưng check thêm cho chắc)
        if (!isGameOver && !showCompletionDialog) {
            ttsHelper.speak(title)
        }
    }

    LaunchedEffect(answered) {
        if (answered) {
            // Phát sound effect ngay lập tức
            if (selectedAnswer == correctAnswer) {
                soundHelper.playCorrectSound()
            } else {
                soundHelper.playWrongSound()
            }

            delay(500)
            if (selectedAnswer == correctAnswer) onCorrect() else onIncorrect()
        }
    }

    val Header = @Composable {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp * fontScale),
                contentPadding = PaddingValues(0.dp)
            ) { Text("←", fontSize = 24.sp * fontScale, color = Color.White, fontWeight = FontWeight.Bold) }

            if (mode == "test") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Lives (hearts) for test mode
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            Text(
                                if (index < lives) "❤️" else "🤍",
                                fontSize = 24.sp * fontScale
                            )
                        }
                    }
                    Text(
                        "⏱️ %02d:%02d".format(timerSeconds / 60, timerSeconds % 60),
                        fontSize = 18.sp * fontScale,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                    Text(
                        "Điểm: $score/$total",
                        fontSize = 16.sp * fontScale,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }
            } else if (mode == "practice") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Luyện tập",
                        fontSize = 22.sp * fontScale,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    // XP bar
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "XP: $xp / 10",
                            fontSize = 14.sp * fontScale,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A1B9A)
                        )
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE0E0E0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((xp / 10f).coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
                                        )
                                    )
                            )
                        }
                    }
                }
            } else {
                // Hiển thị lives (3 trái tim) - Legacy mode
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Text(
                            if (index < lives) "❤️" else "🤍",
                            fontSize = 28.sp * fontScale
                        )
                    }
                }
            }

            // Button loa để đọc lại câu hỏi
            Button(
                onClick = { ttsHelper.speak(title) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp * fontScale),
                contentPadding = PaddingValues(0.dp)
            ) { Text("🔊", fontSize = 24.sp * fontScale) }
        }
    }

    // ✅ Title bị ẩn - chỉ đọc bằng âm thanh
    val Title = @Composable {
        // Text(title, fontSize = 22.sp * fontScale, fontWeight = FontWeight.Medium)
    }

    val Options = @Composable {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { option ->
                val color = if (answered) {
                    if (option == correctAnswer) Color(0xFF4CAF50)
                    else if (option == selectedAnswer) Color(0xFFE53935)
                    else Color(0xFF2196F3)
                } else Color(0xFF2196F3)

                Button(
                    onClick = { if (!answered) { selectedAnswer = option; answered = true } },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp * fontScale),
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("$option", fontSize = 24.sp * fontScale, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF7FBFF), Color(0xFFFFFFFF))
                )
            )
    ) {
        // Use scroll if screen is small OR font is large
        val useScroll = maxHeight < 600.dp || fontScale > 1.1f

        if (useScroll) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Header()
                Title()
                Box(Modifier.heightIn(min = 200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { content() }
                Options()
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Header()
                Title()
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { content() }
                Options()
            }
        }
    }
}