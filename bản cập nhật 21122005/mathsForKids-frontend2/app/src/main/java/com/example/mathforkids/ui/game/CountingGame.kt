package com.example.mathforkids.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathforkids.model.GameQuestion
import com.example.mathforkids.config.GameConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GameIcon(val emoji: String, val name: String)

val availableIcons = listOf(
    GameIcon("🍎", "quả táo"),
    GameIcon("⭐", "ngôi sao"),
    GameIcon("🎈", "bóng bay"),
    GameIcon("🌟", "ngôi sao lấp lánh"),
    GameIcon("🍇", "chùm nho"),
    GameIcon("🎨", "bảng màu"),
    GameIcon("🐶", "chú chó"),
    GameIcon("🐱", "chú mèo"),
    GameIcon("🚗", "ô tô"),
    GameIcon("🍦", "cây kem")
)

/**
 * Counting Game - Visual learning with objects
 * Perfect for 4-5 year olds learning to count
 */
@Composable
fun CountingGameScreen(
    level: Int,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
    var question by remember { mutableStateOf(generateCountingQuestion(level)) }
    var currentIcon by remember { mutableStateOf(availableIcons.random()) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val options = remember(question) {
        val correct = question.correctAnswer
        val opts = mutableSetOf(correct)
        var attempts = 0
        while (opts.size < 3 && attempts < 50) {
            val randomNum = Random.nextInt(0, 11)
            if (randomNum != correct) {
                opts.add(randomNum)
            }
            attempts++
        }
        // Nếu không đủ 3, thêm các số gần correct
        if (opts.size < 3) {
            if (correct > 0) opts.add(correct - 1)
            if (correct < 10 && opts.size < 3) opts.add(correct + 1)
        }
        opts.toList().shuffled()
    }
    
    fun handleAnswer(answer: Int) {
        selectedAnswer = answer
        isCorrect = answer == question.correctAnswer
        showFeedback = true
        
        scope.launch {
            delay(1500)
            if (isCorrect) {
                onCorrect()
                // Generate new question
                question = generateCountingQuestion(level)
                currentIcon = availableIcons.random()
                selectedAnswer = null
                showFeedback = false
            } else {
                onIncorrect()
                selectedAnswer = null
                showFeedback = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        GameHeader(
            title = "🔢 Đếm số",
            onBack = onBack
        )
        
        Spacer(Modifier.height(20.dp))
        
        // Question
        Text(
            "Bé ơi, có bao nhiêu ${currentIcon.name} nhỉ?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(Modifier.height(30.dp))
        
        // Visual objects to count
        ObjectsDisplay(
            count = (question as GameQuestion.CountingQuestion).objectCount,
            emoji = currentIcon.emoji
        )
        
        Spacer(Modifier.height(40.dp))
        
        // Answer options
        AnswerOptions(
            options = options,
            selectedAnswer = selectedAnswer,
            correctAnswer = if (showFeedback) question.correctAnswer else null,
            onAnswerClick = { if (!showFeedback) handleAnswer(it) }
        )
        
        Spacer(Modifier.height(20.dp))
        
        // Feedback
        AnimatedVisibility(
            visible = showFeedback,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            FeedbackDisplay(isCorrect = isCorrect)
        }
    }
}

@Composable
fun ObjectsDisplay(count: Int, emoji: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display objects in rows
        val rows = (count + 4) / 5 // Max 5 per row
        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                val itemsInRow = minOf(5, count - row * 5)
                repeat(itemsInRow) {
                    Text(
                        emoji,
                        fontSize = 48.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

fun generateCountingQuestion(level: Int): GameQuestion.CountingQuestion {
    val maxCount = GameConfig.getCountingMaxForLevel(level)
    val count = Random.nextInt(0, maxCount + 1)
    return GameQuestion.CountingQuestion(objectCount = count)
}
