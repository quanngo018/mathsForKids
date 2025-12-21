package com.example.mathforkids.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mathforkids.config.GameConfig
import com.example.mathforkids.model.GameType
import com.example.mathforkids.model.GameResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    gameType: String,
    level: Int,
    onComplete: (GameResult) -> Unit,
    onBack: () -> Unit
) {
    var correctCount by remember { mutableStateOf(0) }
    var incorrectCount by remember { mutableStateOf(0) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // [FIX 1] Tạo key để làm mới câu hỏi. Mỗi khi trả lời (đúng/sai), key này tăng lên -> Câu hỏi mới được tạo
    val questionIndex = correctCount + incorrectCount

    val type = try {
        GameType.valueOf(gameType.uppercase())
    } catch (e: Exception) {
        GameType.COUNTING
    }

    LaunchedEffect(correctCount) {
        if (correctCount >= 3 && !showCompletionDialog) {
            showCompletionDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (type) {
            // [FIX 2] Truyền questionIndex vào để ép các màn hình game reset số liệu
            GameType.COUNTING -> CountingGameScreen(level, questionIndex, { correctCount++ }, { incorrectCount++ }, onBack)
            GameType.ADDITION -> AdditionGameScreen(level, questionIndex, { correctCount++ }, { incorrectCount++ }, onBack)
            GameType.SUBTRACTION -> SubtractionGameScreen(level, questionIndex, { correctCount++ }, { incorrectCount++ }, onBack)
            GameType.MATCHING -> MatchingGameScreen(level, questionIndex, { correctCount++ }, { incorrectCount++ }, onBack)

            GameType.WRITING -> WritingPracticeGame(
                level = level,
                onCorrect = { correctCount++ },
                onIncorrect = { incorrectCount++ },
                onBack = onBack
            )
        }

        if (showCompletionDialog) {
            LevelCompletionDialog(
                correctAnswers = correctCount,
                incorrectAnswers = incorrectCount,
                stars = if (incorrectCount == 0) 3 else if (incorrectCount <= 2) 2 else 1,
                onContinue = {
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
                },
                onPlayAgain = {
                    showCompletionDialog = false
                    correctCount = 0
                    incorrectCount = 0
                },
                onBack = {
                    showCompletionDialog = false
                    onBack()
                }
            )
        }
    }
}

// ------------------------------------------------------------------------
// PHẦN LOGIC CÁC TRÒ CHƠI (ĐÃ FIX LỖI)
// ------------------------------------------------------------------------

@Composable
fun LevelCompletionDialog(
    correctAnswers: Int, incorrectAnswers: Int, stars: Int,
    onContinue: () -> Unit, onPlayAgain: () -> Unit, onBack: () -> Unit
) {
    Dialog(onDismissRequest = onBack) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color.White).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hoàn thành!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                Spacer(Modifier.height(16.dp))
                Text("⭐".repeat(stars), fontSize = 40.sp)
                Text("Đúng: $correctAnswers câu", fontSize = 18.sp, color = Color.DarkGray)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Text("Lưu điểm & Tiếp tục", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Chơi lại") }
            }
        }
    }
}

// Hàm xử lý độ khó: Chuyển Level ID (2001, 2002) thành độ khó thực tế (1, 2)
fun getDifficulty(level: Int): Int {
    return if (level > 1000) level % 1000 else level
}

@Composable
fun CountingGameScreen(level: Int, key: Int, onCorrect: () -> Unit, onIncorrect: () -> Unit, onBack: () -> Unit) {
    // [FIX 3] remember(key): Khi key thay đổi, code trong { } sẽ chạy lại -> Tạo số mới
    val number = remember(key) { (1..10).random() }
    val options = remember(key) { generateOptions(number, 1..10) }

    BaseGameLayout("Bé hãy đếm xem có bao nhiêu quả táo? 🍎", {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val rows = (number + 2) / 3
            repeat(rows) { r ->
                Row { for (i in (r * 3) until minOf((r * 3) + 3, number)) Text("🍎", fontSize = 48.sp, modifier = Modifier.padding(4.dp)) }
            }
        }
    }, options, number, onCorrect, onIncorrect, onBack)
}

@Composable
fun AdditionGameScreen(level: Int, key: Int, onCorrect: () -> Unit, onIncorrect: () -> Unit, onBack: () -> Unit) {
    // [FIX 4] Sử dụng getDifficulty để số không bị quá to (Vd: level 2005 -> độ khó 5 -> số max 25)
    val diff = getDifficulty(level)
    val maxNum = 5 * diff

    val a = remember(key) { (1..maxNum).random() }
    val b = remember(key) { (1..maxNum).random() }
    val result = a + b

    // Tạo đáp án sai trong khoảng hợp lý
    val options = remember(key) { generateOptions(result, (result - 5)..(result + 5)) }

    // [FIX 5] Tự động chỉnh cỡ chữ nếu số quá dài
    val text = "$a + $b = ?"
    val fontSize = if (text.length > 8) 40.sp else 60.sp

    BaseGameLayout("Phép tính cộng:", {
        Text(text, fontSize = fontSize, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
    }, options, result, onCorrect, onIncorrect, onBack)
}

@Composable
fun SubtractionGameScreen(level: Int, key: Int, onCorrect: () -> Unit, onIncorrect: () -> Unit, onBack: () -> Unit) {
    val diff = getDifficulty(level)
    val maxNum = 5 * diff + 5 // Cộng thêm 5 để số bị trừ lớn hơn chút

    val a = remember(key) { (5..maxNum).random() }
    val b = remember(key) { (1 until a).random() } // Đảm bảo b < a để không ra số âm
    val result = a - b

    val options = remember(key) { generateOptions(result, (result - 5)..(result + 5)) }

    val text = "$a - $b = ?"
    val fontSize = if (text.length > 8) 40.sp else 60.sp

    BaseGameLayout("Phép tính trừ:", {
        Text(text, fontSize = fontSize, fontWeight = FontWeight.Bold, color = Color(0xFFE64A19))
    }, options, result, onCorrect, onIncorrect, onBack)
}

@Composable
fun MatchingGameScreen(level: Int, key: Int, onCorrect: () -> Unit, onIncorrect: () -> Unit, onBack: () -> Unit) {
    val number = remember(key) { (10..99).random() }
    val options = remember(key) { generateOptions(number, 10..99) }

    BaseGameLayout("Tìm số giống số này:", {
        Box(Modifier.clip(RoundedCornerShape(10.dp)).background(Color.LightGray).padding(20.dp)) {
            Text("$number", fontSize = 50.sp, fontWeight = FontWeight.Bold)
        }
    }, options, number, onCorrect, onIncorrect, onBack)
}

fun generateOptions(correct: Int, range: IntRange): List<Int> {
    val list = mutableListOf(correct)
    // Đảm bảo không bị lặp vô tận nếu range quá nhỏ
    var attempts = 0
    while (list.size < 3 && attempts < 100) {
        val r = range.random()
        if (r != correct && r >= 0 && r !in list) list.add(r)
        attempts++
    }
    // Fallback nếu không tìm đủ số
    while (list.size < 3) {
        list.add(list.max() + 1)
    }
    return list.shuffled()
}

@Composable
fun BaseGameLayout(title: String, content: @Composable () -> Unit, options: List<Int>, correctAnswer: Int, onCorrect: () -> Unit, onIncorrect: () -> Unit, onBack: () -> Unit) {
    var answered by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }

    // Reset trạng thái khi câu hỏi thay đổi (correctAnswer thay đổi)
    LaunchedEffect(correctAnswer) {
        answered = false
        selectedAnswer = null
    }

    LaunchedEffect(answered) {
        if (answered) {
            delay(500)
            if (selectedAnswer == correctAnswer) onCorrect() else onIncorrect()
            // Không cần reset ở đây nữa vì correctAnswer thay đổi sẽ trigger LaunchedEffect bên trên
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth()) { IconButton(onClick = onBack) { Text("🔙", fontSize = 24.sp) } }
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