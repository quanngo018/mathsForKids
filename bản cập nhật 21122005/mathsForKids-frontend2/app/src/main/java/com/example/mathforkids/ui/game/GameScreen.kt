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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mathforkids.config.GameConfig
import com.example.mathforkids.model.GameType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Unified game screen that routes to the appropriate game type
 * Tracks progress and unlocks next level automatically
 */
@Composable
fun GameScreen(
    gameType: String,
    level: Int,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    // Track results for this game session
    var correctCount by remember { mutableStateOf(0) }
    var incorrectCount by remember { mutableStateOf(0) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val type = when (gameType.uppercase()) {
        "COUNTING" -> GameType.COUNTING
        "ADDITION" -> GameType.ADDITION
        "SUBTRACTION" -> GameType.SUBTRACTION
        "MATCHING" -> GameType.MATCHING
        "WRITING" -> GameType.WRITING
        else -> GameType.COUNTING
    }
    
    // Check if level is completed (not for WRITING mode - it's free practice)
    LaunchedEffect(correctCount) {
        if (type != GameType.WRITING && GameConfig.canUnlockNextLevel(correctCount) && !showCompletionDialog) {
            showCompletionDialog = true
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Route to appropriate game based on type
        when (type) {
            GameType.COUNTING -> CountingGameScreen(
                level = level,
                onCorrect = { correctCount++ },
                onIncorrect = { incorrectCount++ },
                onBack = onBack
            )
            GameType.ADDITION -> AdditionGameScreen(
                level = level,
                onCorrect = { correctCount++ },
                onIncorrect = { incorrectCount++ },
                onBack = onBack
            )
            GameType.SUBTRACTION -> SubtractionGameScreen(
                level = level,
                onCorrect = { correctCount++ },
                onIncorrect = { incorrectCount++ },
                onBack = onBack
            )
            GameType.MATCHING -> MatchingGameScreen(
                level = level,
                onCorrect = { correctCount++ },
                onIncorrect = { incorrectCount++ },
                onBack = onBack
            )
            GameType.WRITING -> WritingPracticeGameScreen(
                level = level,
                onCorrect = { /* No tracking for free practice mode */ },
                onIncorrect = { /* No tracking for free practice mode */ },
                onBack = onBack
            )
        }
        
        // Completion Dialog
        if (showCompletionDialog) {
            LevelCompletionDialog(
                correctAnswers = correctCount,
                incorrectAnswers = incorrectCount,
                stars = GameConfig.calculateStars(correctCount),
                onContinue = {
                    showCompletionDialog = false
                    scope.launch {
                        delay(300)
                        onComplete()
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

/**
 * Dialog hiển thị khi hoàn thành level
 */
@Composable
fun LevelCompletionDialog(
    correctAnswers: Int,
    incorrectAnswers: Int,
    stars: Int,
    onContinue: () -> Unit,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit
) {
    Dialog(onDismissRequest = onBack) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Hoàn thành!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )

                Spacer(Modifier.height(16.dp))

                // Stars
                Text(
                    "⭐".repeat(stars),
                    fontSize = 40.sp
                )

                Spacer(Modifier.height(24.dp))

                // Buttons
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Tiếp tục",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Chơi lại", fontSize = 16.sp)
                }
            }
        }
    }
}



