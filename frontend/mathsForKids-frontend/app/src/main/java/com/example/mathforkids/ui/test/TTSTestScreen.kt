package com.example.mathforkids.ui.test

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathforkids.util.rememberTTSHelper

@Composable
fun TTSTestScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val ttsHelper = rememberTTSHelper()
    var ttsStatus by remember { mutableStateOf("Đang khởi tạo...") }
    var hasVietnamese by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Kiểm tra TTS status
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsStatus = "✅ TTS khả dụng"
            } else {
                ttsStatus = "❌ TTS không khả dụng"
            }
        }
        
        kotlinx.coroutines.delay(500)
        val viResult = tts.setLanguage(java.util.Locale("vi", "VN"))
        hasVietnamese = viResult != TextToSpeech.LANG_MISSING_DATA && viResult != TextToSpeech.LANG_NOT_SUPPORTED
        
        tts.shutdown()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
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
                "Kiểm tra Text-to-Speech",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(24.dp))

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (hasVietnamese) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    ttsStatus,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasVietnamese) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                
                Spacer(Modifier.height(12.dp))
                
                Text(
                    if (hasVietnamese) 
                        "✅ Gói ngôn ngữ Tiếng Việt đã được cài đặt"
                    else
                        "⚠️ Chưa có gói ngôn ngữ Tiếng Việt",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Test sentences
        val testSentences = listOf(
            "Xin chào, bé yêu!",
            "Bé hãy đếm xem có bao nhiêu quả táo nhé",
            "Phép tính cộng: Hai cộng ba bằng mấy?",
            "Bé tập viết số một",
            "Chúc mừng bé đã hoàn thành!",
            "Hãy chọn đáp án đúng nhé"
        )

        Text(
            "Các câu mẫu để kiểm tra:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        testSentences.forEach { sentence ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        sentence,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Button(
                        onClick = { ttsHelper.speak(sentence) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("🔊", fontSize = 20.sp) }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Install button nếu chưa có gói ngôn ngữ
        if (!hasVietnamese) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Hướng dẫn cài đặt gói ngôn ngữ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        "1. Nhấn nút bên dưới để mở cài đặt TTS\n" +
                        "2. Tìm 'Google Text-to-Speech'\n" +
                        "3. Tải về gói ngôn ngữ 'Tiếng Việt'\n" +
                        "4. Đặt làm ngôn ngữ mặc định",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            val installIntent = Intent()
                            installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                            installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(installIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text("Mở cài đặt TTS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Info card
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
                    "• TTS (Text-to-Speech) giúp đọc câu hỏi tự động\n" +
                    "• Cần có gói ngôn ngữ Tiếng Việt để phát âm chuẩn\n" +
                    "• Nếu chưa có, giọng đọc sẽ bị 'Anh hóa'\n" +
                    "• Tốc độ đọc được điều chỉnh chậm hơn cho trẻ em",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
