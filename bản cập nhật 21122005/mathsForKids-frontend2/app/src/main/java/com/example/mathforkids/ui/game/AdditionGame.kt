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
 * Addition Game - Visual math learning
 * Shows objects being added together
 */
@Composable
fun AdditionGameScreen(
    level: Int,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
    onBack: () -> Unit
) {
    var question by remember { mutableStateOf(generateAdditionQuestion(level)) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val options = remember(question) {
        val correct = question.correctAnswer
        val opts = mutableSetOf(correct)
        var attempts = 0
        while (opts.size < 3 && attempts < 50) {
            val offset = Random.nextInt(-3, 4)
            val option = (correct + offset).coerceIn(0, 30)
            if (option != correct) {
                opts.add(option)
            }
            attempts++
        }
        // Backup: add nearby numbers if needed
        if (opts.size < 3 && correct > 0) opts.add(correct - 1)
        if (opts.size < 3 && correct < 30) opts.add(correct + 1)
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
                question = generateAdditionQuestion(level)
                selectedAnswer = null
                showFeedback = false
            } else {
                onIncorrect()
                selectedAnswer = null
                showFeedback = false
            }
        }
    }
    
    val additionQ = question as GameQuestion.AdditionQuestion
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameHeader(title = "➕ Phép cộng", onBack = onBack)
        
        Spacer(Modifier.height(20.dp))
        
        Text(
            "Tính nhẩm nào!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0)
        )
        
        Spacer(Modifier.height(30.dp))
        
        // Visual representation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.7f))
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First group
            VisualGroup(count = additionQ.num1, emoji = "🟦")
            
            Text("➕", fontSize = 48.sp, fontWeight = FontWeight.Bold)
            
            // Second group
            VisualGroup(count = additionQ.num2, emoji = "🟦")
        }
        
        Spacer(Modifier.height(30.dp))
        
        // Math expression
        Text(
            "${additionQ.num1} + ${additionQ.num2} = ?",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0D47A1)
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

@Composable
fun VisualGroup(count: Int, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        // Show visual representation up to 5 objects
        val displayCount = minOf(count, 5)
        Row {
            repeat(displayCount) {
                Text(emoji, fontSize = 24.sp, modifier = Modifier.padding(2.dp))
            }
            if (count > 5) {
                Text("...", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun generateAdditionQuestion(level: Int): GameQuestion.AdditionQuestion {
    val maxSum = GameConfig.getAdditionMaxForLevel(level)
    // Ensure sum <= maxSum
    val num1 = Random.nextInt(0, maxSum + 1)
    val num2 = Random.nextInt(0, maxSum - num1 + 1)
    return GameQuestion.AdditionQuestion(num1 = num1, num2 = num2)
}
