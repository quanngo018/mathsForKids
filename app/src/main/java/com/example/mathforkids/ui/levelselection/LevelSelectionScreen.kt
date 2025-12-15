package com.example.mathforkids.ui.levelselection

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathforkids.model.GameLevel
import com.example.mathforkids.model.GameType
import com.example.mathforkids.model.LevelPosition

/**
 * Duolingo-style level selection screen with winding path
 * Designed for 4-5 year old kids with big, colorful buttons
 */
@Composable
fun LevelSelectionScreen(
    completedLevels: Set<Int>,
    onLevelClick: (GameType, Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(GameType.COUNTING, GameType.ADDITION)

    // Generate levels based on selected tab
    val levels = remember(completedLevels, selectedTab) { 
        generateGameLevels(completedLevels, tabs[selectedTab]) 
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF3E0),
                        Color(0xFFE1F5FE),
                        Color(0xFFF3E5F5)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            LevelSelectionHeader(onBack = onBack)

            // Mode Selection Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF6A1B9A),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF9C27B0),
                        height = 4.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, type ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                text = type.displayName,
                                fontSize = 18.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            ) 
                        }
                    )
                }
            }
            
            // Scrollable path
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Draw the winding path
                DrawPath(levels)
                
                // Level nodes on the path
                levels.forEach { level ->
                    LevelNode(
                        level = level,
                        onClick = { onLevelClick(level.gameType, level.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelSelectionHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
        ) {
            Text("⬅ Menu", fontSize = 18.sp, color = Color.White)
        }
        Spacer(Modifier.width(16.dp))
        Text(
            "🎮 Chọn bài học",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6A1B9A)
        )
    }
}

@Composable
fun BoxScope.DrawPath(levels: List<GameLevel>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(((levels.size * 120)).dp)
    ) {
        val path = Path()
        var isFirst = true
        
        levels.forEach { level ->
            val x = size.width * level.position.x
            val y = level.position.y.dp.toPx()
            
            if (isFirst) {
                path.moveTo(x, y)
                isFirst = false
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = Color(0xFFBDBDBD),
            style = Stroke(width = 8f)
        )
    }
}

@Composable
fun BoxScope.LevelNode(
    level: GameLevel,
    onClick: () -> Unit
) {
    val scale by rememberInfiniteTransition(label = "scale").animateFloat(
        initialValue = 1f,
        targetValue = if (level.isUnlocked && !level.isCompleted) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .offset(
                x = (level.position.x * 300).dp,
                y = level.position.y.dp
            )
            .size(80.dp)
            .scale(if (level.isUnlocked) scale else 1f)
            .clip(CircleShape)
            .background(
                if (level.isUnlocked) level.gameType.color else Color.Gray
            )
            .clickable(enabled = level.isUnlocked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = level.gameType.emoji,
                fontSize = 32.sp
            )
            if (level.isCompleted) {
                Text(
                    text = "⭐".repeat(level.stars),
                    fontSize = 12.sp
                )
            } else if (!level.isUnlocked) {
                Text("🔒", fontSize = 20.sp)
            }
        }
    }
}

/**
 * Generate sample game levels in a winding path
 * Unlocks levels based on completed levels
 */
fun generateGameLevels(completedLevels: Set<Int> = setOf(), gameType: GameType): List<GameLevel> {
    val levels = mutableListOf<GameLevel>()
    
    // Define level count and ID offset based on game type
    val (levelCount, idOffset) = when (gameType) {
        GameType.COUNTING -> Pair(11, 1000) // Levels 1001-1011 (Numbers 0-10)
        GameType.ADDITION -> Pair(20, 2000) // Levels 2001-2020
        else -> Pair(10, 3000)
    }
    
    for (i in 0 until levelCount) {
        // Create a winding path (left-right-left pattern)
        val xPos = when {
            i % 3 == 0 -> 0.25f
            i % 3 == 1 -> 0.5f
            else -> 0.75f
        }
        
        val levelId = idOffset + i + 1
        
        // Level unlocked if it's the first level of the type OR previous level is completed
        val isUnlocked = i == 0 || completedLevels.contains(levelId - 1)
        val isCompleted = completedLevels.contains(levelId)
        
        levels.add(
            GameLevel(
                id = levelId,
                gameType = gameType,
                isUnlocked = isUnlocked,
                isCompleted = isCompleted,
                stars = if (isCompleted) 3 else 0,
                position = LevelPosition(
                    x = xPos,
                    y = 100f + (i * 120f)
                )
            )
        )
    }
    
    return levels
}
