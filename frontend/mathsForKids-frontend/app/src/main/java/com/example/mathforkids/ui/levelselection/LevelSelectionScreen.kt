package com.example.mathforkids.ui.levelselection

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathforkids.model.GameLevel
import com.example.mathforkids.model.GameType
import com.example.mathforkids.model.LevelPosition

/**
 * Màn hình chọn chế độ chơi (Menu) và Bản đồ màn chơi (Map)
 */
@Composable
fun LevelSelectionScreen(
    mode: String = "practice",
    completedLevels: Set<Int>,
    initialGameType: GameType? = null,
    onLevelClick: (GameType, Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedGameType by remember { mutableStateOf(initialGameType) }

    // Cập nhật state khi tham số đầu vào thay đổi
    LaunchedEffect(initialGameType) {
        if (initialGameType != null) {
            selectedGameType = initialGameType
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF3E0), Color(0xFFE1F5FE), Color(0xFFF3E5F5))
                )
            )
    ) {
        if (mode == "test") {
            TestSelectionView(
                onTestSelected = { gameType, level -> onLevelClick(gameType, level) },
                onBack = onBack
            )
        } else {
            if (selectedGameType == null) {
                // 1. Nếu chưa chọn game -> Hiện Menu chọn chế độ
                ModeSelectionView(
                    onModeSelected = { selectedGameType = it },
                    onLevelClick = onLevelClick, // Truyền hàm này vào để xử lý game Tập viết
                    onBack = onBack
                )
            } else {
                // 2. Các game khác -> Hiện bản đồ Level (Map)
                // Lưu ý: Game WRITING sẽ không bao giờ vào đây vì đã được xử lý ở ModeSelectionView
                LevelMapView(
                    gameType = selectedGameType!!,
                    completedLevels = completedLevels,
                    onLevelClick = onLevelClick,
                    onBack = { selectedGameType = null }
                )
            }
        }
    }
}

@Composable
fun TestSelectionView(
    onTestSelected: (GameType, Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LevelSelectionHeader(onBack = onBack, title = "Chọn bài kiểm tra")

        Spacer(modifier = Modifier.height(40.dp))

        // Các số
        TestButton(
            title = "Các số",
            subtitle = "Nhận biết số đếm",
            color = Color(0xFF4CAF50),
            onClick = { onTestSelected(GameType.COUNTING, 1) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Phép toán 1-3
        TestButton(
            title = "Phép toán 1-3",
            subtitle = "Cộng trừ phạm vi nhỏ",
            color = Color(0xFF2196F3),
            onClick = { onTestSelected(GameType.ADDITION, 1) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Phép toán 3-6
        TestButton(
            title = "Phép toán 3-6",
            subtitle = "Cộng trừ phạm vi trung bình",
            color = Color(0xFFFF9800),
            onClick = { onTestSelected(GameType.ADDITION, 4) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Phép toán 6-10
        TestButton(
            title = "Phép toán 6-10",
            subtitle = "Thử thách lớn hơn",
            color = Color(0xFFE91E63),
            onClick = { onTestSelected(GameType.ADDITION, 10) }
        )
    }
}

@Composable
fun TestButton(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}


@Composable
fun ModeSelectionView(
    onModeSelected: (GameType) -> Unit,
    onLevelClick: (GameType, Int) -> Unit, // [FIX] Thêm tham số này
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LevelSelectionHeader(onBack = onBack, title = "Chọn chế độ")

        Spacer(modifier = Modifier.height(40.dp))

        // Hàng 1: Đếm số & Cộng
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModeButton(
                gameType = GameType.COUNTING,
                onClick = { onModeSelected(GameType.COUNTING) }
            )

            ModeButton(
                gameType = GameType.ADDITION,
                onClick = { onModeSelected(GameType.ADDITION) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hàng 2: Trừ & Tập viết
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModeButton(
                gameType = GameType.SUBTRACTION,
                onClick = { onModeSelected(GameType.SUBTRACTION) }
            )

            ModeButton(
                gameType = GameType.WRITING,
                onClick = {
                    // [FIX] Vào thẳng game Tập viết level 1, không cần qua Map
                    onLevelClick(GameType.WRITING, 1)
                }
            )
        }
    }
}

@Composable
fun ModeButton(
    gameType: GameType,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(gameType.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = gameType.emoji,
                fontSize = 60.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = gameType.displayName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242)
        )
    }
}

@Composable
fun LevelMapView(
    gameType: GameType,
    completedLevels: Set<Int>,
    onLevelClick: (GameType, Int) -> Unit,
    onBack: () -> Unit
) {
    // Tạo danh sách level ảo để vẽ bản đồ
    val levels = remember(completedLevels, gameType) {
        generateGameLevels(completedLevels, gameType)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LevelSelectionHeader(onBack = onBack, title = gameType.displayName)

        // [FIX] Dùng BoxWithConstraints để lấy chiều rộng màn hình thực tế
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val screenWidth = maxWidth // Chiều rộng màn hình

            DrawPath(levels, screenWidth) // Vẽ đường nối

            // Vẽ các nút level
            levels.forEach { level ->
                LevelNode(
                    level = level,
                    screenWidth = screenWidth,
                    onClick = { onLevelClick(level.gameType, level.id) }
                )
            }
        }
    }
}

@Composable
fun LevelSelectionHeader(onBack: () -> Unit, title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp),
            contentPadding = PaddingValues(0.dp)
        ) { Text("←", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold) }

        Spacer(Modifier.width(16.dp))

        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
    }
}

@Composable
fun DrawPath(levels: List<GameLevel>, screenWidth: Dp) {
    Canvas(modifier = Modifier.fillMaxWidth().height(((levels.size * 120)).dp)) {
        val path = Path()
        var isFirst = true

        levels.forEach { level ->
            // [FIX] Tính toạ độ X dựa trên chiều rộng màn hình
            val x = screenWidth.toPx() * level.position.x
            val y = level.position.y.dp.toPx()

            if (isFirst) {
                path.moveTo(x, y)
                isFirst = false
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(path = path, color = Color(0xFFBDBDBD), style = Stroke(width = 8f))
    }
}

@Composable
fun LevelNode(level: GameLevel, screenWidth: Dp, onClick: () -> Unit) {
    val scale by rememberInfiniteTransition(label = "scale").animateFloat(
        initialValue = 1f,
        targetValue = if (level.isUnlocked && !level.isCompleted) 1.1f else 1f,
        animationSpec = infiniteRepeatable(animation = tween(800, easing = EaseInOut), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        modifier = Modifier
            // [FIX] Căn giữa node: (Vị trí % * Chiều rộng) - (Một nửa kích thước Node)
            .offset(x = (screenWidth * level.position.x) - 40.dp, y = level.position.y.dp)
            .size(80.dp)
            .scale(if (level.isUnlocked) scale else 1f)
            .clip(CircleShape)
            .background(if (level.isUnlocked) level.gameType.color else Color.Gray)
            .clickable(enabled = level.isUnlocked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = level.gameType.emoji, fontSize = 32.sp)
            if (level.isCompleted) {
                Text(text = "⭐⭐⭐", fontSize = 12.sp)
            } else if (!level.isUnlocked) {
                Text("🔒", fontSize = 20.sp)
            }
        }
    }
}

fun generateGameLevels(completedLevels: Set<Int>, gameType: GameType): List<GameLevel> {
    val levels = mutableListOf<GameLevel>()
    val (levelCount, idOffset) = when (gameType) {
        GameType.COUNTING -> Pair(11, 1000)
        GameType.ADDITION -> Pair(20, 2000)
        GameType.SUBTRACTION -> Pair(20, 3000)
        else -> Pair(10, 5000)
    }

    for (i in 0 until levelCount) {
        val xPos = when {
            i % 3 == 0 -> 0.25f
            i % 3 == 1 -> 0.5f
            else -> 0.75f
        }
        val levelId = idOffset + i + 1
        val isUnlocked = i == 0 || completedLevels.contains(levelId - 1)
        val isCompleted = completedLevels.contains(levelId)

        levels.add(GameLevel(
            id = levelId,
            gameType = gameType,
            isUnlocked = isUnlocked,
            isCompleted = isCompleted,
            stars = if (isCompleted) 3 else 0,
            position = LevelPosition(x = xPos, y = 100f + (i * 120f))
        ))
    }
    return levels
}