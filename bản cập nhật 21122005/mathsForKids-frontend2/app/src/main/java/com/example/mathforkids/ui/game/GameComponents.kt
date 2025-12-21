package com.example.mathforkids.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared UI components for all game screens
 */

@Composable
fun GameHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
        ) {
            Text("⬅ Quay lại", fontSize = 16.sp, color = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun AnswerOptions(
    options: List<Int>,
    selectedAnswer: Int?,
    correctAnswer: Int?,
    onAnswerClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        options.forEach { option ->
            AnswerButton(
                answer = option,
                isSelected = selectedAnswer == option,
                isCorrect = if (correctAnswer != null) option == correctAnswer else null,
                onClick = { onAnswerClick(option) }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun AnswerButton(
    answer: Int,
    isSelected: Boolean,
    isCorrect: Boolean?,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect == true -> Color(0xFF4CAF50) // Green for correct
        isCorrect == false && isSelected -> Color(0xFFF44336) // Red for incorrect
        isSelected -> Color(0xFF2196F3) // Blue for selected
        else -> Color.White
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(70.dp)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            answer.toString(),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect != null || isSelected) Color.White else Color(0xFF212121)
        )
    }
}

@Composable
fun FeedbackDisplay(isCorrect: Boolean) {
    val scale = rememberInfiniteTransition(label = "feedback_scale")
    val scaleAnim by scale.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336))
            .padding(24.dp)
            .scale(scaleAnim),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isCorrect) "🎉" else "💪",
                fontSize = 64.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isCorrect) "Bé giỏi quá!" else "Thử lại nhé!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (isCorrect) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "⭐⭐⭐",
                    fontSize = 24.sp
                )
            }
        }
    }
}

/**
 * Celebration animation for completing a level
 */
@Composable
fun CelebrationAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var visible by remember { mutableStateOf(true) }
        
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            visible = false
        }
        
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0xFFFFD54F))
                    .padding(40.dp)
            ) {
                Text("🎊", fontSize = 80.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Xuất sắc!",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "⭐⭐⭐",
                    fontSize = 40.sp
                )
            }
        }
    }
}
