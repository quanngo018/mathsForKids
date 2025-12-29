package com.example.mathforkids.util

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.*

class TTSHelper(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var hasVietnamese = false

    fun initialize(onReady: (Boolean) -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("vi", "VN"))
                
                hasVietnamese = when (result) {
                    TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> {
                        // Không có gói ngôn ngữ Tiếng Việt
                        Toast.makeText(
                            context,
                            "Chưa cài đặt gói ngôn ngữ Tiếng Việt cho TTS. Vui lòng cài đặt từ Google Play.",
                            Toast.LENGTH_LONG
                        ).show()
                        false
                    }
                    else -> {
                        // Có gói ngôn ngữ Tiếng Việt
                        tts?.setSpeechRate(0.9f) // Tốc độ đọc chậm hơn 1 chút cho trẻ em
                        tts?.setPitch(1.1f) // Giọng nói cao hơn 1 chút
                        true
                    }
                }
                isInitialized = true
                onReady(hasVietnamese)
            } else {
                isInitialized = false
                hasVietnamese = false
                Toast.makeText(context, "Không thể khởi tạo Text-to-Speech", Toast.LENGTH_SHORT).show()
                onReady(false)
            }
        }
    }

    fun speak(text: String) {
        if (SettingsManager.isSoundEnabled && isInitialized && hasVietnamese) {
            val params = android.os.Bundle()
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, SettingsManager.volume)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, null)
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }

    fun isReady(): Boolean = isInitialized && hasVietnamese

    fun installLanguageData() {
        // Mở cài đặt TTS để người dùng tải gói ngôn ngữ
        val installIntent = Intent()
        installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
        installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(installIntent)
    }
}

@Composable
fun rememberTTSHelper(): TTSHelper {
    val context = LocalContext.current
    val ttsHelper = remember { TTSHelper(context) }
    var isReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        ttsHelper.initialize { ready ->
            isReady = ready
        }
        onDispose {
            ttsHelper.shutdown()
        }
    }

    return ttsHelper
}
