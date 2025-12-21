package com.example.mathforkids

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.mathforkids.ui.theme.MathForKidsTheme
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.mathforkids.model.GameResult
import com.example.mathforkids.navigation.Screen
import com.example.mathforkids.ui.levelselection.LevelSelectionScreen
import com.example.mathforkids.ui.game.GameScreen

// ----------------------------- Main Activity -----------------------------

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

// ----------------------------- App Navigator -----------------------------

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigator() {

    val navController = rememberNavController()

    // Demo users with roles: username to (password, role)
    var registeredUsers = remember {
        mutableStateListOf(
            Triple("ph", "1", "parent"),
            Triple("hs", "1", "student"),
            Triple("admin", "1", "admin"),
            Triple("teacher", "1", "teacher"),
            Triple("demo", "123", "student"),
            Triple("test", "123", "student")
        )
    }

    var currentUser by remember { mutableStateOf("") }
    var currentUserRole by remember { mutableStateOf("") }

    // Game results
    var results = remember { mutableStateListOf<GameResult>() }

    // Track completed levels (empty = chưa hoàn thành level nào)
    val completedLevels = remember { mutableSetOf<Int>() }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        // ---------------- Login ----------------
        composable(Screen.Login.route) {
            LoginScreen(
                onLogin = { username, password ->
                    val user = registeredUsers.find { it.first == username && it.second == password }
                    if (user != null) {
                        currentUser = username
                        currentUserRole = user.third
                        
                        // Navigate based on role
                        val destination = when (user.third) {
                            "student" -> Screen.StudentHome.route
                            "parent" -> Screen.ParentHome.route
                            "teacher" -> Screen.TeacherHome.route
                            "admin" -> Screen.AdminHome.route
                            else -> Screen.Menu.route
                        }
                        
                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        true
                    } else false
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // ---------------- Register ----------------
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegister = { username, password, role ->
                    if (registeredUsers.any { it.first == username }) return@RegisterScreen
                    registeredUsers.add(Triple(username, password, role))
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- Menu ----------------
        composable(Screen.Menu.route) {
            MainMenuScreen(
                username = currentUser,
                onNavigateToMath = {
                    navController.navigate(Screen.LevelSelection.route)
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Menu.route) { inclusive = true }
                    }
                }
            )
        }

        // ---------------- Level Selection ----------------
        composable(
            route = "${Screen.LevelSelection.route}?gameType={gameType}",
            arguments = listOf(
                navArgument("gameType") { 
                    type = NavType.StringType 
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val gameTypeStr = backStackEntry.arguments?.getString("gameType")
            val initialGameType = if (gameTypeStr != null) {
                try {
                    com.example.mathforkids.model.GameType.valueOf(gameTypeStr)
                } catch (e: Exception) {
                    null
                }
            } else null

            LevelSelectionScreen(
                completedLevels = completedLevels.toSet(),
                initialGameType = initialGameType,
                onLevelClick = { gameType, level ->
                    navController.navigate(
                        Screen.Game.createRoute(gameType.name, level)
                    )
                },
                onBack = { 
                    if (initialGameType != null) {
                        // If we came here with a specific game type (e.g. from game completion),
                        // go back to main menu instead of mode selection
                        navController.navigate(Screen.Menu.route) {
                            popUpTo(Screen.Menu.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack() 
                    }
                }
            )
        }

        // ---------------- Game Screen ----------------
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
                onComplete = {
                    // Mark current level as completed
                    completedLevels.add(level)
                    // Navigate back to LevelSelection with the same game type to show map
                    navController.navigate("${Screen.LevelSelection.route}?gameType=$gameType") {
                        popUpTo(Screen.LevelSelection.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- Student Home ----------------
        composable(Screen.StudentHome.route) {
            StudentHomeScreen(
                username = currentUser,
                onNavigateToLuyenTap = {
                    navController.navigate(Screen.LevelSelection.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.StudentHome.route) { inclusive = true }
                    }
                }
            )
        }

        // ---------------- Parent Home ----------------
        composable(Screen.ParentHome.route) {
            ParentHomeScreen(
                username = currentUser,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ParentHome.route) { inclusive = true }
                    }
                }
            )
        }

        // ---------------- Teacher Home ----------------
        composable(Screen.TeacherHome.route) {
            TeacherHomeScreen(
                username = currentUser,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.TeacherHome.route) { inclusive = true }
                    }
                }
            )
        }

        // ---------------- Admin Home ----------------
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(
                username = currentUser,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminHome.route) { inclusive = true }
                    }
                }
            )
        }

        // ---------------- Dashboard ----------------
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                results = results,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ----------------------------- Login Screen -----------------------------

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Boolean,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Image(
            painter = painterResource(id = R.drawable.bg_login),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.offset(y = (-100).dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Đăng nhập", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🎮 Tài khoản demo:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("• ph / 1 (Phụ huynh)", fontSize = 12.sp)
                    Text("• hs / 1 (Học sinh)", fontSize = 12.sp)
                    Text("• admin / 1 (Admin)", fontSize = 12.sp)
                    Text("• teacher / 1 (Giáo viên)", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; error = "" },
                label = { Text("Tên đăng nhập") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = "" },
                label = { Text("Mật khẩu") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                if (username.isEmpty() || password.isEmpty()) {
                    error = "Vui lòng nhập đủ thông tin!"
                } else {
                    if (!onLogin(username, password)) {
                        error = "Sai tên đăng nhập hoặc mật khẩu!"
                    }
                }
            }) { Text("Đăng nhập") }

            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onNavigateToRegister) { Text("Chưa có tài khoản? Đăng ký ngay") }

            if (error.isNotEmpty()) Text(error, color = Color.Red)
        }
    }
}

// ----------------------------- Register Screen -----------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegister: (String, String, String) -> Unit, onBack: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("student") }
    var expanded by remember { mutableStateOf(false) }

    val roles = mapOf(
        "student" to "🎓 Học sinh",
        "parent" to "👨‍👩‍👧 Phụ huynh",
        "teacher" to "👨‍🏫 Giáo viên",
        "admin" to "⚙️ Quản trị viên"
    )

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.bg_dangky),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.offset(y = (-40).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Đăng ký", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(30.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Tên đăng nhập") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3F51B5),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF3F51B5),
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3F51B5),
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color(0xFF3F51B5),
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                )
            )

            Spacer(Modifier.height(12.dp))

            // Dropdown for Role Selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = roles[selectedRole] ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Loại người dùng") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3F51B5),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF3F51B5),
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    roles.forEach { (roleValue, roleLabel) ->
                        DropdownMenuItem(
                            text = { Text(roleLabel, color = Color.Black) },
                            onClick = {
                                selectedRole = roleValue
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.Black
                            ),
                            modifier = Modifier.background(Color.White)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        onRegister(username, password, selectedRole)
                    }
                }
            ) {
                Text("Tạo tài khoản")
            }

            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onBack) { 
                Text("⬅ Quay lại đăng nhập", color = Color.White) 
            }
        }
    }
}

// ----------------------------- Main Menu -----------------------------

@Composable
fun MainMenuScreen(
    username: String,
    onNavigateToMath: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onLogout: () -> Unit
) {

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
                onClick = onNavigateToMath,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Text("🎯 Bé học Toán", fontSize = 24.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onNavigateToDashboard,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) { Text("📊 Thống kê", fontSize = 24.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Text("Đăng xuất", fontSize = 20.sp, color = Color.White) }
        }
    }
}

// ----------------------------- Dashboard -----------------------------

@Composable
fun DashboardScreen(results: List<GameResult>, onBack: () -> Unit) {

    val totalQuestions = results.size
    val correctAnswers = results.count { it.isCorrect }
    val incorrectAnswers = totalQuestions - correctAnswers
    val accuracy = if (totalQuestions > 0) (correctAnswers * 100 / totalQuestions) else 0

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
            .padding(20.dp),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text("📊 Kết quả học tập", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
            Spacer(Modifier.height(20.dp))

            if (totalQuestions == 0) {
                Text("Chưa có dữ liệu. Hãy chơi vài ván nhé!", fontSize = 20.sp, color = Color.Gray)
            } else {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard("Tổng câu", totalQuestions, Color(0xFF1976D2))
                    StatCard("Đúng", correctAnswers, Color(0xFF43A047))
                    StatCard("Chính xác", "$accuracy%", Color(0xFFFBC02D))
                }

                Spacer(Modifier.height(40.dp))

                Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    val total = correctAnswers + incorrectAnswers
                    if (total > 0) {
                        val barWidth = size.width / 4
                        val maxHeight = size.height * 0.8f
                        val correctHeight = (correctAnswers.toFloat() / total) * maxHeight
                        val incorrectHeight = (incorrectAnswers.toFloat() / total) * maxHeight

                        drawRect(
                            color = Color(0xFF43A047),
                            topLeft = androidx.compose.ui.geometry.Offset(size.width / 4 - barWidth / 2, size.height - correctHeight),
                            size = androidx.compose.ui.geometry.Size(barWidth, correctHeight)
                        )
                        drawRect(
                            color = Color(0xFFE53935),
                            topLeft = androidx.compose.ui.geometry.Offset(size.width * 3 / 4 - barWidth / 2, size.height - incorrectHeight),
                            size = androidx.compose.ui.geometry.Size(barWidth, incorrectHeight)
                        )
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(60.dp)
            ) {
                Text("⬅ Quay lại menu", fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: Any, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.DarkGray)
        Text(value.toString(), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ----------------------------- Student Home Screen -----------------------------

@Composable
fun StudentHomeScreen(
    username: String,
    onNavigateToLuyenTap: () -> Unit,
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
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Text("📚 Luyện tập", fontSize = 24.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { 
                    Toast.makeText(context, "Chức năng đang cập nhật", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) { Text("✏️ Học tập", fontSize = 24.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Text("Đăng xuất", fontSize = 20.sp, color = Color.White) }
        }
    }
}

// ----------------------------- Parent Home Screen -----------------------------

@Composable
fun ParentHomeScreen(
    username: String,
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
                onClick = { 
                    Toast.makeText(context, "Chức năng đang cập nhật", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Text("📊 Xem thống kê học tập", fontSize = 22.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Text("Đăng xuất", fontSize = 20.sp, color = Color.White) }
        }
    }
}

// ----------------------------- Teacher Home Screen -----------------------------

@Composable
fun TeacherHomeScreen(
    username: String,
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
                onClick = { 
                    Toast.makeText(context, "Chức năng đang cập nhật", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Text("📖 Chuẩn bị bài giảng", fontSize = 22.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { 
                    Toast.makeText(context, "Chức năng đang cập nhật", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) { Text("✏️ Giao bài tập", fontSize = 22.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { 
                    Toast.makeText(context, "Chức năng đang cập nhật", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(70.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBC02D))
            ) { Text("📊 Xem thống kê", fontSize = 22.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Text("Đăng xuất", fontSize = 20.sp, color = Color.White) }
        }
    }
}

// ----------------------------- Admin Home Screen -----------------------------

@Composable
fun AdminHomeScreen(
    username: String,
    onLogout: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE1BEE7), Color(0xFFFFF8E1)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Xin chào, $username 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(40.dp))
            
            Text("Trang quản trị", fontSize = 20.sp, color = Color.Gray)

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Text("Đăng xuất", fontSize = 20.sp, color = Color.White) }
        }
    }
}
