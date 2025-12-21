package com.example.mathforkids.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.example.mathforkids.config.GameConfig
import com.example.mathforkids.model.GameQuestion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Subtraction Game - Visual learning with objects disappearing
 * Perfect for teaching "taking away" concept
 */
@Composable
fun SubtractionGameScreen(
    level: Int,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
    var question by remember { mutableStateOf(generateSubtractionQuestion(level)) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var showAnimation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val options = remember(question) {
        val correct = question.correctAnswer
        val opts = mutableSetOf(correct)
        var attempts = 0
        while (opts.size < 3 && attempts < 50) {
            val offset = Random.nextInt(-2, 3)
            val option = (correct + offset).coerceIn(0, 20)
            if (option != correct) {
                opts.add(option)
            }
            attempts++
        }
        // Backup options
        if (opts.size < 3 && correct > 0) opts.add(correct - 1)
        if (opts.size < 3 && correct < 15) opts.add(correct + 1)
        opts.toList().shuffled()
    }
    
    fun handleAnswer(answer: Int) {
        selectedAnswer = answer
        isCorrect = answer == question.correctAnswer
        showFeedback = true
        showAnimation = true
        
        scope.launch {
            delay(1500)
            if (isCorrect) {
                onCorrect()
                question = generateSubtractionQuestion(level)
                selectedAnswer = null
                showFeedback = false
                showAnimation = false
            } else {
                onIncorrect()
                selectedAnswer = null
                showFeedback = false
                showAnimation = false
            }
        }
    }
    
    val subQ = question as GameQuestion.SubtractionQuestion
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF9C4), Color(0xFFFFF59D))
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameHeader(title = "➖ Phép trừ", onBack = onBack)
        
        Spacer(Modifier.height(20.dp))
        
        Text(
            "Lấy đi bao nhiêu?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF57F17)
        )
        
        Spacer(Modifier.height(30.dp))
        
        // Visual representation with disappearing effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.7f))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Show all objects first
                Text(
                    "Ban đầu có ${subQ.num1} quả:",
                    fontSize = 20.sp,
                    color = Color(0xFFF57C00)
                )
                Spacer(Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(minOf(subQ.num1, 10)) { index ->
                        AnimatedVisibility(
                            visible = !showAnimation || index < subQ.num1 - subQ.num2,
                            exit = fadeOut() + shrinkOut()
                        ) {
                            Text(
                                "🍊",
                                fontSize = 36.sp,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
                
                if (showAnimation) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Lấy đi ${subQ.num2} quả",
                        fontSize = 18.sp,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(30.dp))
        
        // Math expression
        Text(
            "${subQ.num1} - ${subQ.num2} = ?",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFF6F00)
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
        
        AnimatedVisibility(
            visible = showFeedback,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            FeedbackDisplay(isCorrect = isCorrect)
        }
    }
}

fun generateSubtractionQuestion(level: Int): GameQuestion.SubtractionQuestion {
    val maxNum = GameConfig.getSubtractionMaxForLevel(level)
    val num1 = Random.nextInt(2, maxNum + 1)
    val num2 = Random.nextInt(1, num1) // Ensure result is non-negative
    return GameQuestion.SubtractionQuestion(num1 = num1, num2 = num2)
}
