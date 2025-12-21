package com.example.mathforkids.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.mathforkids.config.GameConfig
import com.example.mathforkids.model.GameQuestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Number Matching Game - Match numbers to quantities
 * Reinforces number recognition
 */
@Composable
fun MatchingGameScreen(
    level: Int,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
    var question by remember { mutableStateOf(generateMatchingQuestion(level)) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val options = remember(question) {
        val correct = question.correctAnswer
        val opts = mutableSetOf(correct)
        var attempts = 0
        while (opts.size < 4 && attempts < 50) { // 4 options for matching
            val randomNum = Random.nextInt(1, 11)
            if (randomNum != correct) {
                opts.add(randomNum)
            }
            attempts++
        }
        // Backup options
        if (opts.size < 4 && correct > 1) opts.add(correct - 1)
        if (opts.size < 4 && correct > 2) opts.add(correct - 2)
        if (opts.size < 4 && correct < 10) opts.add(correct + 1)
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
                question = generateMatchingQuestion(level)
                selectedAnswer = null
                showFeedback = false
            } else {
                onIncorrect()
                selectedAnswer = null
                showFeedback = false
            }
        }
    }
    
    val matchQ = question as GameQuestion.MatchingQuestion
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0))
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameHeader(title = "🎯 Ghép số", onBack = onBack)
        
        Spacer(Modifier.height(20.dp))
        
        Text(
            "Chọn số đúng!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC2185B)
        )
        
        Spacer(Modifier.height(30.dp))
        
        // Show visual objects
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.7f))
                .padding(30.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Đếm và chọn số:",
                    fontSize = 24.sp,
                    color = Color(0xFF880E4F)
                )
                Spacer(Modifier.height(20.dp))
                
                // Display objects in a grid
                val rows = (matchQ.number + 3) / 4 // Max 4 per row
                for (row in 0 until rows) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        val itemsInRow = minOf(4, matchQ.number - row * 4)
                        repeat(itemsInRow) {
                            Text(
                                "💎",
                                fontSize = 52.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(40.dp))
        
        // Number options in a grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.take(2).forEach { option ->
                    NumberOption(
                        number = option,
                        isSelected = selectedAnswer == option,
                        isCorrect = if (showFeedback) option == question.correctAnswer else null,
                        onClick = { if (!showFeedback) handleAnswer(option) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.drop(2).forEach { option ->
                    NumberOption(
                        number = option,
                        isSelected = selectedAnswer == option,
                        isCorrect = if (showFeedback) option == question.correctAnswer else null,
                        onClick = { if (!showFeedback) handleAnswer(option) }
                    )
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
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
fun NumberOption(
    number: Int,
    isSelected: Boolean,
    isCorrect: Boolean?,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect == true -> Color(0xFF4CAF50)
        isCorrect == false && isSelected -> Color(0xFFF44336)
        isSelected -> Color(0xFF2196F3)
        else -> Color.White
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(4.dp, Color(0xFFAD1457), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(Modifier.padding(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            number.toString(),
            fontSize = 56.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isCorrect != null || isSelected) Color.White else Color(0xFF880E4F)
        )
    }
}

fun generateMatchingQuestion(level: Int): GameQuestion.MatchingQuestion {
    val maxNum = GameConfig.getMatchingMaxForLevel(level)
    val number = Random.nextInt(1, maxNum + 1)
    return GameQuestion.MatchingQuestion(number = number)
}
