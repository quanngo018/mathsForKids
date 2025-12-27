package com.example.mathforkids

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mathforkids.navigation.Screen
import com.example.mathforkids.ui.game.GameScreen
import com.example.mathforkids.ui.levelselection.LevelSelectionScreen
import com.example.mathforkids.ui.theme.MathForKidsTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.mathforkids.ui.learning.LearningScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MathForKidsTheme {
                AppNavigator()
            }
        }
    }
}

// ----------------------------- APP NAVIGATOR -----------------------------
@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val context = LocalContext.current

    var currentUserId by remember { mutableStateOf(0) }
    var currentUserName by remember { mutableStateOf("") }
    var dashboardUserId by remember { mutableStateOf(0) }

    // Set lưu trữ các level đã hoàn thành để vẽ Map
    val completedLevels = remember { mutableStateListOf<Int>() }

    // [MỚI] Tự động tải tiến độ từ Server mỗi khi User đăng nhập thành công
    LaunchedEffect(currentUserId) {
        if (currentUserId != 0) {
            ApiService.create().getDashboard(currentUserId).enqueue(object : Callback<BaseResponse<DashboardData>> {
                override fun onResponse(
                    call: Call<BaseResponse<DashboardData>>,
                    response: Response<BaseResponse<DashboardData>>
                ) {
                    if (response.body()?.status == "success") {
                        val levels = response.body()?.data?.completedLevels
                        if (levels != null) {
                            completedLevels.clear()
                            completedLevels.addAll(levels)
                        }
                    }
                }

                override fun onFailure(call: Call<BaseResponse<DashboardData>>, t: Throwable) {
                    /* Ignore error silently */
                }
            })
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        // --- LOGIN ---
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    currentUserId = user.userId
                    currentUserName = user.fullName
                    dashboardUserId = user.userId
                    val destination = when (user.role) {
                        "student" -> Screen.StudentHome.route
                        "parent" -> Screen.ParentHome.route
                        "teacher" -> Screen.TeacherHome.route
                        "admin" -> Screen.AdminHome.route
                        else -> Screen.Menu.route
                    }
                    navController.navigate(destination) { popUpTo(Screen.Login.route) { inclusive = true } }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        // --- REGISTER ---
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // --- PARENT HOME ---
        composable(Screen.ParentHome.route) {
            ParentHomeScreen(
                username = currentUserName,
                userId = currentUserId,
                onNavigateToDashboard = { childId ->
                    dashboardUserId = childId
                    navController.navigate(Screen.Dashboard.route)
                },
                onLogout = {
                    currentUserId = 0
                    navController.navigate(Screen.Login.route) { popUpTo(Screen.ParentHome.route) { inclusive = true } }
                }
            )
        }

        // --- STUDENT HOME ---
        composable(Screen.StudentHome.route) {
            StudentHomeScreen(
                username = currentUserName,
                onNavigateToLuyenTap = { navController.navigate(Screen.LevelSelection.route) },
                onNavigateToDashboard = {
                    dashboardUserId = currentUserId
                    navController.navigate(Screen.Dashboard.route)
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToLearning = { navController.navigate(Screen.Learning.route) },  // <-- Thêm cái này mới
                onLogout = {
                    currentUserId = 0
                    navController.navigate(Screen.Login.route) { popUpTo(Screen.StudentHome.route) { inclusive = true } }
                }
            )
        }

        // --- DASHBOARD (THỐNG KÊ) ---
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                userId = dashboardUserId,
                onBack = { navController.popBackStack() }
            )
        }

        // --- MENU CHUNG ---
        // --- MENU CHUNG ---
        composable(Screen.Menu.route) {
            MainMenuScreen(
                username = currentUserName,
                onNavigateToMath = { navController.navigate(Screen.LevelSelection.route) },
                onNavigateToDashboard = {
                    dashboardUserId = currentUserId
                    navController.navigate(Screen.Dashboard.route)
                },
                onNavigateToLearning = { navController.navigate(Screen.Learning.route) }, // Thêm dòng này
                onLogout = {
                    currentUserId = 0
                    navController.navigate(Screen.Login.route) { popUpTo(Screen.Menu.route) { inclusive = true } }
                }
            )
        }

        // --- LEVEL SELECTION ---
        composable(
            route = "${Screen.LevelSelection.route}?gameType={gameType}",
            arguments = listOf(navArgument("gameType") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val gameTypeStr = backStackEntry.arguments?.getString("gameType")
            val initialGameType =
                if (gameTypeStr != null) try { com.example.mathforkids.model.GameType.valueOf(gameTypeStr) } catch (e: Exception) { null }
                else null

            LevelSelectionScreen(
                completedLevels = completedLevels.toSet(),
                initialGameType = initialGameType,
                onLevelClick = { gameType, level ->
                    navController.navigate(Screen.Game.createRoute(gameType.name, level))
                },
                onBack = {
                    if (initialGameType != null) {
                        navController.navigate(Screen.LevelSelection.route) { popUpTo(Screen.LevelSelection.route) { inclusive = true } }
                    } else {
                        navController.navigate(Screen.StudentHome.route) { popUpTo(Screen.StudentHome.route) { inclusive = true } }
                    }
                }
            )
        }

        // --- GAME SCREEN ---
        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("gameType") { type = NavType.StringType },
                navArgument("level") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val gameType = backStackEntry.arguments?.getString("gameType") ?: "COUNTING"
            val level = backStackEntry.arguments?.getInt("level") ?: 1

            GameScreen(
                gameType = gameType,
                level = level,
                onComplete = { result ->
                    if (!completedLevels.contains(level)) completedLevels.add(level)

                    if (currentUserId != 0) {
                        val score = if (result.totalQuestions > 0)
                            (result.correctAnswers.toFloat() / result.totalQuestions) * 10
                        else 0f

                        val req = GameResultRequest(
                            userId = currentUserId,
                            gameType = gameType,
                            levelId = level,
                            score = score,
                            correctCount = result.correctAnswers,
                            totalQuestions = result.totalQuestions
                        )

                        ApiService.create().saveResult(req).enqueue(object : Callback<BaseResponse<Any>> {
                            override fun onResponse(call: Call<BaseResponse<Any>>, response: Response<BaseResponse<Any>>) {}
                            override fun onFailure(call: Call<BaseResponse<Any>>, t: Throwable) {
                                Toast.makeText(context, "Lỗi lưu điểm", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }

                    if (gameType == "WRITING") {
                        navController.navigate(Screen.LevelSelection.route) {
                            popUpTo(Screen.LevelSelection.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate("${Screen.LevelSelection.route}?gameType=$gameType") {
                            popUpTo(Screen.LevelSelection.route) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TeacherHome.route) {
            TeacherHomeScreen(username = currentUserName, onLogout = { navController.navigate(Screen.Login.route) })
        }
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(username = currentUserName, onLogout = { navController.navigate(Screen.Login.route) })
        }
        
        // --- SETTINGS SCREEN ---
        composable(Screen.Settings.route) {
            com.example.mathforkids.ui.settings.SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTTSTest = { navController.navigate(Screen.TTSTest.route) }
            )
        }
        
        // --- TTS TEST SCREEN ---
        composable(Screen.TTSTest.route) {
            com.example.mathforkids.ui.test.TTSTestScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Learning.route) {
            LearningScreen(onBack = { navController.popBackStack() })
        }
    }
}

// ----------------------------- DASHBOARD SCREEN -----------------------------
data class SubjectStat(val name: String, val score: Float, val color: Color, val icon: String)

@Composable
fun DashboardScreen(userId: Int, onBack: () -> Unit) {
    var totalQuestions by remember { mutableStateOf(0) }
    var totalCorrect by remember { mutableStateOf(0) }
    var averageScore by remember { mutableStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }

    val subjects = remember(totalCorrect) {
        if (totalQuestions == 0) emptyList() else listOf(
            SubjectStat("Đếm số", 9.5f, Color(0xFF4CAF50), "🔢"),
            SubjectStat("Phép cộng", (totalCorrect.toFloat() / totalQuestions * 10).coerceAtLeast(5f), Color(0xFF2196F3), "➕"),
            SubjectStat("Phép trừ", (totalCorrect.toFloat() / totalQuestions * 10).coerceAtLeast(4f), Color(0xFFFFC107), "➖"),
            SubjectStat("Tập viết", 8.0f, Color(0xFFFF9800), "✏️")
        )
    }

    LaunchedEffect(userId) {
        if (userId != 0) {
            ApiService.create().getDashboard(userId).enqueue(object : Callback<BaseResponse<DashboardData>> {
                override fun onResponse(call: Call<BaseResponse<DashboardData>>, response: Response<BaseResponse<DashboardData>>) {
                    if (response.body()?.status == "success") {
                        val data = response.body()?.data
                        totalQuestions = data?.totalQuestions ?: 0
                        totalCorrect = data?.totalCorrect ?: 0
                        averageScore = if (totalQuestions > 0) (totalCorrect.toFloat() / totalQuestions) * 10 else 0f
                    }
                    isLoading = false
                }
                override fun onFailure(call: Call<BaseResponse<DashboardData>>, t: Throwable) { isLoading = false }
            })
        }
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) { Text("Quay lại Menu", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFF3E5F5))))
                    .padding(padding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text("Bảng thành tích 🏆", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    Spacer(Modifier.height(20.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Điểm trung bình", fontSize = 16.sp, color = Color.Gray)
                            Text(
                                String.format("%.1f", averageScore),
                                fontSize = 64.sp, fontWeight = FontWeight.ExtraBold,
                                color = if (averageScore >= 8) Color(0xFF4CAF50) else if (averageScore >= 5) Color(0xFFFFC107) else Color(0xFFF44336)
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Đã làm", fontSize = 14.sp, color = Color.Gray)
                                    Text("$totalQuestions câu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Làm đúng", fontSize = 14.sp, color = Color.Gray)
                                    Text("$totalCorrect câu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    Text("Chi tiết môn học", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
                    Spacer(Modifier.height(12.dp))
                }

                items(subjects.size) { index ->
                    val subject = subjects[index]
                    val animatedProgress by animateFloatAsState(
                        targetValue = subject.score / 10f,
                        animationSpec = tween(1000),
                        label = ""
                    )

                    Card(
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).clip(CircleShape).background(subject.color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) { Text(subject.icon, fontSize = 24.sp) }

                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(subject.name, fontWeight = FontWeight.Bold)
                                    Text("${subject.score}", fontWeight = FontWeight.Bold, color = subject.color)
                                }
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = animatedProgress,
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = subject.color,
                                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

// ----------------------------- PARENT HOME SCREEN -----------------------------
@Composable
fun ParentHomeScreen(username: String, userId: Int, onNavigateToDashboard: (Int) -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    var studentUsername by remember { mutableStateOf("") }
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE1BEE7), Color(0xFFFFF8E1)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
            Text("Phụ huynh: $username", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(30.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔗 Kết nối với con", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = studentUsername,
                        onValueChange = { studentUsername = it },
                        label = { Text("Nhập tên đăng nhập của con") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = {
                        if (studentUsername.isNotEmpty()) {
                            ApiService.create().linkStudent(LinkRequest(userId, studentUsername)).enqueue(object : Callback<BaseResponse<Any>> {
                                override fun onResponse(call: Call<BaseResponse<Any>>, response: Response<BaseResponse<Any>>) {
                                    if (response.body()?.status == "success") Toast.makeText(context, "Kết nối thành công!", Toast.LENGTH_SHORT).show()
                                    else Toast.makeText(context, response.body()?.message ?: "Lỗi", Toast.LENGTH_SHORT).show()
                                }
                                override fun onFailure(call: Call<BaseResponse<Any>>, t: Throwable) { Toast.makeText(context, "Lỗi mạng", Toast.LENGTH_SHORT).show() }
                            })
                        }
                    }) { Text("Kết nối ngay") }
                }
            }
            Spacer(Modifier.height(30.dp))
            Button(onClick = {
                ApiService.create().getMyChild(userId).enqueue(object : Callback<BaseResponse<Int>> {
                    override fun onResponse(call: Call<BaseResponse<Int>>, response: Response<BaseResponse<Int>>) {
                        if (response.body()?.status == "success") {
                            response.body()?.data?.let(onNavigateToDashboard)
                        } else Toast.makeText(context, "Chưa kết nối với con nào!", Toast.LENGTH_SHORT).show()
                    }
                    override fun onFailure(call: Call<BaseResponse<Int>>, t: Throwable) { Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show() }
                })
            }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                Text("📊 Xem thống kê của con", fontSize = 18.sp)
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Đăng xuất") }
        }
    }
}

// ----------------------------- LOGIN & REGISTER & OTHERS -----------------------------
@Composable
fun LoginScreen(
    onLoginSuccess: (UserData) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var offlineMode by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.bg_login),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = (-50).dp).padding(30.dp)) {
            Text("Đăng nhập", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            
            // Hiển thị badge offline mode nếu đang bật
            if (offlineMode) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "📴 Chế độ Offline",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Tên đăng nhập") })
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                // Kiểm tra tài khoản offline trước
                if (username == "hs" && password == "1") {
                    offlineMode = true
                    error = ""
                    onLoginSuccess(UserData(
                        userId = 999,
                        fullName = "Học sinh Demo",
                        role = "student",
                        avatar = ""
                    ))
                    return@Button
                }
                
                // Nếu không phải tài khoản offline, gọi API
                offlineMode = false
                ApiService.create().login(LoginRequest(username, password)).enqueue(object : Callback<BaseResponse<UserData>> {
                    override fun onResponse(call: Call<BaseResponse<UserData>>, response: Response<BaseResponse<UserData>>) {
                        if (response.body()?.status == "success") {
                            error = ""
                            response.body()?.data?.let(onLoginSuccess)
                        } else {
                            error = response.body()?.message ?: "Lỗi"
                        }
                    }
                    override fun onFailure(call: Call<BaseResponse<UserData>>, t: Throwable) { 
                        error = "Lỗi kết nối - Thử dùng tài khoản offline (hs/1)"
                    }
                })
            }, modifier = Modifier.fillMaxWidth()) { Text("Đăng nhập") }
            if (error.isNotEmpty()) Text(error, color = Color.Red, fontSize = 12.sp)
            
            // Hint về tài khoản offline
            /*
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "💡 Mẹo: Test offline",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        "Username: hs | Password: 1",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }
            */
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onNavigateToRegister) { Text("Đăng ký ngay") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("student") }
    var expanded by remember { mutableStateOf(false) }
    val roles = mapOf("student" to "Học sinh", "parent" to "Phụ huynh", "teacher" to "Giáo viên")
    val context = LocalContext.current

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(painter = painterResource(id = R.drawable.bg_dangky), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = (-30).dp).padding(30.dp)) {
            Text("Đăng ký", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Họ tên") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Tên đăng nhập") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Mật khẩu") }, visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = roles[selectedRole] ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vai trò") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    roles.forEach { (k, v) ->
                        DropdownMenuItem(text = { Text(v) }, onClick = { selectedRole = k; expanded = false })
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                ApiService.create().register(RegisterRequest(username, password, fullName, selectedRole)).enqueue(object : Callback<BaseResponse<Any>> {
                    override fun onResponse(call: Call<BaseResponse<Any>>, response: Response<BaseResponse<Any>>) {
                        if (response.body()?.status == "success") {
                            Toast.makeText(context, "Thành công!", Toast.LENGTH_SHORT).show()
                            onRegisterSuccess()
                        }
                    }
                    override fun onFailure(call: Call<BaseResponse<Any>>, t: Throwable) { Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show() }
                })
            }, modifier = Modifier.fillMaxWidth()) { Text("Đăng ký") }
            TextButton(onClick = onBack) { Text("Quay lại") }
        }
    }
}

@Composable
fun StudentHomeScreen(
    username: String,
    onNavigateToLuyenTap: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToLearning: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE1BEE7), Color(0xFFFFF8E1)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Xin chào, $username 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onNavigateToLuyenTap,
                modifier = Modifier.fillMaxWidth(0.7f).height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text("📚 Luyện tập", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onNavigateToDashboard,
                modifier = Modifier.fillMaxWidth(0.7f).height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text("📊 Xem kết quả", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            // ✅ NÚT MỚI: DẠY HỌC (giống style 3 nút kia)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onNavigateToLearning,
                modifier = Modifier.fillMaxWidth(0.7f).height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text("🎥 Học tập", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Button Cài đặt
            if (onNavigateToSettings != null) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.fillMaxWidth(0.7f).height(70.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text("⚙️ Cài đặt", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(0.7f).height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text("Đăng xuất", fontSize = 20.sp, color = Color.White)
            }
        }
    }
}



@Composable fun MainMenuScreen(username: String, onNavigateToMath: () -> Unit, onNavigateToDashboard: () -> Unit, onNavigateToLearning: () -> Unit, onLogout: () -> Unit) {
    StudentHomeScreen(
        username = username, 
        onNavigateToLuyenTap = onNavigateToMath, 
        onNavigateToDashboard = onNavigateToDashboard, 
        onNavigateToSettings = null,
        onNavigateToLearning= onNavigateToLearning,
        onLogout = onLogout
    ) 
}
@Composable fun TeacherHomeScreen(username: String, onLogout: () -> Unit) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column { Text("Giáo viên: $username"); Button(onClick = onLogout) { Text("Đăng xuất") } } } }
@Composable fun AdminHomeScreen(username: String, onLogout: () -> Unit) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column { Text("Admin: $username"); Button(onClick = onLogout) { Text("Đăng xuất") } } } }
