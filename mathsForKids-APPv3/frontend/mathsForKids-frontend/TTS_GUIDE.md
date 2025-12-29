# Text-to-Speech (TTS) Integration - Hướng dẫn

## Tổng quan
Ứng dụng đã được tích hợp Text-to-Speech (TTS) của Google để đọc câu hỏi tự động, giúp trẻ em học toán hiệu quả hơn.

## Tính năng

### 1. Tự động đọc câu hỏi
- Khi mỗi câu hỏi mới hiển thị, TTS sẽ tự động đọc câu hỏi
- Tốc độ đọc được điều chỉnh chậm hơn (0.9x) phù hợp cho trẻ em
- Giọng đọc cao hơn một chút (pitch 1.1x) để thân thiện hơn

### 2. Button loa để đọc lại
- Mỗi màn hình game có button loa (🔊) màu xanh dương
- Kích thước và hình dạng giống button back (48x48dp)
- Vị trí: Góc trên bên phải màn hình
- Chức năng: Bấm để nghe lại câu hỏi bất cứ lúc nào

### 3. Màn hình kiểm tra TTS
- Truy cập từ Student Home Screen → "🔊 Kiểm tra TTS"
- Kiểm tra xem gói ngôn ngữ Tiếng Việt đã được cài đặt chưa
- Có các câu mẫu để test giọng đọc
- Hướng dẫn cài đặt nếu chưa có gói ngôn ngữ

## Cài đặt gói ngôn ngữ Tiếng Việt

### Tại sao cần cài đặt?
Nếu không cài đặt gói ngôn ngữ Tiếng Việt, TTS sẽ đọc bằng giọng Anh hóa, phát âm không chuẩn và khó hiểu.

### Cách cài đặt:

#### Phương pháp 1: Qua ứng dụng
1. Mở ứng dụng và đăng nhập
2. Vào Student Home Screen
3. Bấm "🔊 Kiểm tra TTS"
4. Nếu chưa có gói Tiếng Việt, bấm "Mở cài đặt TTS"
5. Tìm "Google Text-to-Speech"
6. Tải về gói ngôn ngữ "Tiếng Việt"
7. Đặt Tiếng Việt làm ngôn ngữ mặc định

#### Phương pháp 2: Cài đặt thủ công
1. Mở **Settings** (Cài đặt)
2. Tìm **System** → **Languages & input** → **Text-to-speech output**
3. Chọn **Google Text-to-Speech Engine**
4. Bấm vào biểu tượng **⚙️ (Settings)**
5. Chọn **Install voice data**
6. Tìm và tải **Vietnamese (Tiếng Việt)**
7. Sau khi tải xong, đặt làm ngôn ngữ mặc định

#### Phương pháp 3: Google Play Store
1. Mở Google Play Store
2. Tìm "Google Text-to-Speech"
3. Đảm bảo đã cài đặt và cập nhật lên phiên bản mới nhất
4. Làm theo Phương pháp 2 để tải gói ngôn ngữ

## Cấu trúc code

### TTSHelper.kt
```
com.example.mathforkids.util.TTSHelper
```
- Class helper quản lý TTS
- Khởi tạo và kiểm tra gói ngôn ngữ
- Cung cấp các phương thức speak(), stop(), shutdown()
- Composable function `rememberTTSHelper()` để sử dụng trong Compose

### GameScreen.kt
```kotlin
// Import
import com.example.mathforkids.util.rememberTTSHelper

// Trong BaseGameLayout
val ttsHelper = rememberTTSHelper()

LaunchedEffect(questionKey) {
    // Tự động đọc khi câu hỏi mới
    delay(300)
    ttsHelper.speak(title)
}

// Button loa
Button(
    onClick = { ttsHelper.speak(title) },
    // ... styling
) { Text("🔊", fontSize = 24.sp) }
```

### TTSTestScreen.kt
```
com.example.mathforkids.ui.test.TTSTestScreen
```
- Màn hình kiểm tra TTS
- Hiển thị trạng thái gói ngôn ngữ
- Cung cấp các câu mẫu để test
- Hướng dẫn cài đặt gói ngôn ngữ

## Lưu ý kỹ thuật

### 1. Lifecycle management
- TTS được khởi tạo trong `DisposableEffect`
- Tự động shutdown khi component bị dispose
- Không bị memory leak

### 2. Hiệu suất
- TTS chỉ khởi tạo 1 lần per screen
- Sử dụng `remember` để cache instance
- Delay 300ms trước khi đọc để đảm bảo UI đã render

### 3. Error handling
- Kiểm tra status trước khi speak
- Hiển thị Toast nếu không thể khởi tạo
- Graceful fallback nếu không có gói ngôn ngữ

### 4. Accessibility
- Phù hợp cho trẻ em, kể cả trẻ chưa biết đọc
- Hỗ trợ trẻ khiếm thị
- Tốc độ và pitch được điều chỉnh phù hợp

## Kiểm tra và debugging

### 1. Kiểm tra gói ngôn ngữ
```kotlin
val tts = TextToSpeech(context) { status ->
    if (status == TextToSpeech.SUCCESS) {
        val result = tts.setLanguage(Locale("vi", "VN"))
        if (result == TextToSpeech.LANG_MISSING_DATA || 
            result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Chưa có gói Tiếng Việt
        }
    }
}
```

### 2. Test câu đọc
Sử dụng TTSTestScreen với các câu mẫu:
- "Xin chào, bé yêu!"
- "Bé hãy đếm xem có bao nhiêu quả táo nhé"
- "Phép tính cộng: Hai cộng ba bằng mấy?"
- "Bé tập viết số một"
- "Chúc mừng bé đã hoàn thành!"

### 3. Logcat
Kiểm tra logs từ TextToSpeech engine:
```
adb logcat | grep TTS
```

## Future enhancements

### Có thể cải tiến:
1. **Chọn giọng đọc**: Nam/nữ, miền Bắc/Nam
2. **Điều chỉnh tốc độ**: Cho phép người dùng tùy chỉnh
3. **Highlight text**: Highlight từng từ khi đọc
4. **Cache audio**: Pre-generate audio files để giảm latency
5. **Offline mode**: Tải trước các câu hỏi phổ biến
6. **Multi-language**: Hỗ trợ tiếng Anh cho trẻ học song ngữ

## Troubleshooting

### Vấn đề: Không nghe được gì
- **Nguyên nhân**: Volume bị tắt hoặc TTS chưa khởi tạo xong
- **Giải pháp**: Kiểm tra volume, đợi 1-2 giây sau khi mở màn hình

### Vấn đề: Giọng đọc bị "Anh hóa"
- **Nguyên nhân**: Chưa cài gói ngôn ngữ Tiếng Việt
- **Giải pháp**: Làm theo hướng dẫn cài đặt ở trên

### Vấn đề: App bị crash khi bấm button loa
- **Nguyên nhân**: TTS chưa ready hoặc đã bị shutdown
- **Giải pháp**: Kiểm tra `isReady()` trước khi gọi `speak()`

### Vấn đề: Đọc chậm quá / nhanh quá
- **Nguyên nhân**: speechRate không phù hợp
- **Giải pháo**: Điều chỉnh trong TTSHelper.kt:
```kotlin
tts?.setSpeechRate(0.9f) // Giảm xuống 0.7-0.8 nếu muốn chậm hơn
```

## License & Credits
- Google Text-to-Speech Engine
- Android TTS API
- Vietnamese language pack by Google
