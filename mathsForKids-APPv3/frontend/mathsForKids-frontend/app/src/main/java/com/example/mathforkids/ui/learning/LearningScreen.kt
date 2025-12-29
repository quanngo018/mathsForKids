package com.example.mathforkids.ui.learning

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.draw.clip
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.mathforkids.util.SettingsManager
import androidx.compose.ui.text.style.TextOverflow
data class Lesson(val id: String, val title: String, val description: String)

val lessons = listOf(
    Lesson("vHQlLrjktDE", "Hình phẳng", "Giới thiệu các hình phẳng cơ bản như tròn, vuông, tam giác."),
    Lesson("eZoqago1808", "Hình khối ", "Học về hình khối lập phương, cầu, trụ với ví dụ minh họa."),
    Lesson("-qoSihzDe6E", "Số 0", "Giới thiệu khái niệm số 0 và ý nghĩa của nó."),
    Lesson("VysCGnLs2Xk", "Số 1, 2, 3 ", "Đếm số từ 1 đến 3 với hình ảnh vui nhộn."),
    Lesson("AMiRbJdOdmw", "Số 4, 5, 6 ", "Tiếp tục đếm số từ 4 đến 6 qua các trò chơi."),
    Lesson("umFR1LsduXk", "Số 7, 8, 9 ", "Học số 7, 8, 9 với bài hát và hoạt động."),
    Lesson("WfnFyZ2POqU", "Số 10 ", "Học số 10 và cách đếm đến 10."),
    Lesson("DWBGDKOkpfc", "Ôn tập từ số 0 đến số 10 ", "Ôn tập số từ 0 đến 10 qua bài tập."),
    Lesson("rWFztLVWuL0", "Nhiều hơn, ít hơn, bằng nhau ", "So sánh số lượng: nhiều hơn, ít hơn, bằng nhau."),
    Lesson("zA-w8wROazU", "Lớn hơn, bé hơn, bằng nhau ", "So sánh kích thước: lớn hơn, bé hơn, bằng nhau."),
    Lesson("2b-4DrO6Swg", "Cộng trừ trong phạm vi 6", "Phép cộng trừ trong phạm vi 6."),
    Lesson("48nDIxw670w", "Thêm, bớt trong phạm vi 10 ", "Thực hành thêm bớt số đến 10."),
    Lesson("2tLGBCHNU38", "Các buổi trong ngày ", "Học về sáng, trưa, chiều, tối."),
    Lesson("Ffn1EJRQ-es", "Các ngày trong tuần ", "Các ngày trong tuần: thứ Hai đến Chủ Nhật."),
    Lesson("SA_EpOJWlhI", "Các mùa trong năm", "Các mùa trong năm: xuân, hạ, thu, đông."),
    Lesson("S8l7Na5xXLs", "Location - Kindergarten Math [OLM.VN]", "Học vị trí: trên, dưới, trái, phải trong không gian.")
)





@Composable
fun LearningScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val fontScale = SettingsManager.fontScale

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Header with Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp * fontScale),
                contentPadding = PaddingValues(0.dp)
            ) { Text("←", fontSize = 24.sp * fontScale, color = Color.White, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.width(16.dp))

            Text(
                "Danh sách bài học",
                fontSize = 28.sp * fontScale,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp) // Thêm khoảng trống dưới cùng
        ) {
            items(lessons) { lesson ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp) // Tăng khoảng cách giữa các thẻ
                        .clickable {
                            val url = "https://www.youtube.com/watch?v=${lesson.id}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                    elevation = CardDefaults.cardElevation(6.dp), // Tăng độ nổi khối
                    shape = RoundedCornerShape(20.dp), // Bo góc tròn hơn cho trẻ em
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    // Chuyển từ Row sang Column để tên ở dưới hình
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 1. Hình ảnh hiển thị to và tràn chiều ngang
                        Image(
                            painter = rememberAsyncImagePainter("https://img.youtube.com/vi/${lesson.id}/hqdefault.jpg"),
                            contentDescription = "Thumbnail",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp) // Tăng chiều cao hình ảnh lên
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // 2. Tên bài học hiển thị to ở phía dưới hình ảnh
                        Text(
                            text = lesson.title,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally), // Căn giữa tên bài học
                            fontSize = 22.sp * fontScale, // Chữ to hơn
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF333333)
                        )


                    }
                }
            }
        }
    }
}