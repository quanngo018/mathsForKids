package com.example.mathforkids.ui.settings

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathforkids.util.rememberTTSHelper
import com.example.mathforkids.util.SettingsManager

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToTTSTest: () -> Unit
) {
    val context = LocalContext.current
    val ttsHelper = rememberTTSHelper()
    
    // Use SettingsManager for global state
    var ttsEnabled by remember { mutableStateOf(SettingsManager.isSoundEnabled) }
    var volume by remember { mutableStateOf(SettingsManager.volume) }
    var fontScale by remember { mutableStateOf(SettingsManager.fontScale) }
    
    // Local state for TTS specific settings (keep them here or move to SettingsManager if needed)
    var speechRate by remember { mutableStateOf(0.9f) }
    var pitch by remember { mutableStateOf(1.1f) }

    // Sync back to SettingsManager when changed
    LaunchedEffect(ttsEnabled) { SettingsManager.isSoundEnabled = ttsEnabled }
    LaunchedEffect(volume) { SettingsManager.volume = volume }
    LaunchedEffect(fontScale) { SettingsManager.fontScale = fontScale }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFF3E5F5))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp),
                    contentPadding = PaddingValues(0.dp)
                ) { Text("←", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold) }

                Text(
                    "⚙️ Cài đặt",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )

                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Sound Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "🔊 Âm thanh",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Enable/Disable Sound
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Bật âm thanh",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Bao gồm đọc câu hỏi và hiệu ứng",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = ttsEnabled,
                            onCheckedChange = { ttsEnabled = it }
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    // Volume
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Âm lượng", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text("${(volume * 100).toInt()}%", fontSize = 14.sp, color = Color.Gray)
                        }
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = volume,
                            onValueChange = { volume = it },
                            valueRange = 0f..1f,
                            steps = 9
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Display Settings Section (Font Size)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Aa Hiển thị",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("Kích thước chữ", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = fontScale == 0.85f,
                            onClick = { fontScale = 0.85f },
                            label = { Text("Nhỏ", fontSize = 14.sp) }
                        )
                        FilterChip(
                            selected = fontScale == 1.0f,
                            onClick = { fontScale = 1.0f },
                            label = { Text("Vừa", fontSize = 16.sp) }
                        )
                        FilterChip(
                            selected = fontScale == 1.15f,
                            onClick = { fontScale = 1.15f },
                            label = { Text("Lớn", fontSize = 18.sp) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // TTS Advanced Settings (Hidden if sound is off, or just kept as advanced)
            if (ttsEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            "🗣️ Cấu hình giọng đọc",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )

                        Spacer(Modifier.height(16.dp))

                        // Speech Rate
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tốc độ đọc", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("${(speechRate * 100).toInt()}%", fontSize = 14.sp, color = Color.Gray)
                            }
                            Spacer(Modifier.height(8.dp))
                            Slider(
                                value = speechRate,
                                onValueChange = { speechRate = it },
                                valueRange = 0.5f..1.5f,
                                steps = 9
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Divider()
                        Spacer(Modifier.height(16.dp))

                        // Pitch
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cao độ giọng", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("${(pitch * 100).toInt()}%", fontSize = 14.sp, color = Color.Gray)
                            }
                            Spacer(Modifier.height(8.dp))
                            Slider(
                                value = pitch,
                                onValueChange = { pitch = it },
                                valueRange = 0.8f..1.5f,
                                steps = 6
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Test TTS Button
                        Button(
                            onClick = onNavigateToTTSTest,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🔊 Kiểm tra TTS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            Spacer(Modifier.height(20.dp))

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "ℹ️ Thông tin",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "• Các cài đặt sẽ được lưu tự động\n" +
                        "• TTS cần gói ngôn ngữ Tiếng Việt\n" +
                        "• Khởi động lại app để áp dụng một số thay đổi",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
