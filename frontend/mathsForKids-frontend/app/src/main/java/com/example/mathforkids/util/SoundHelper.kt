package com.example.mathforkids.util

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Helper class để quản lý các sound effects trong game
 */
class SoundHelper(private val context: Context) {
    private var correctSoundPlayer: MediaPlayer? = null
    private var wrongSoundPlayer: MediaPlayer? = null

    init {
        try {
            // Load sound effects từ assets/sounds/
            val assetFileDescriptor1 = context.assets.openFd("sounds/rightanswer.mp3")
            correctSoundPlayer = MediaPlayer().apply {
                setDataSource(
                    assetFileDescriptor1.fileDescriptor,
                    assetFileDescriptor1.startOffset,
                    assetFileDescriptor1.length
                )
                prepare()
            }
            assetFileDescriptor1.close()

            val assetFileDescriptor2 = context.assets.openFd("sounds/wronganswer.mp3")
            wrongSoundPlayer = MediaPlayer().apply {
                setDataSource(
                    assetFileDescriptor2.fileDescriptor,
                    assetFileDescriptor2.startOffset,
                    assetFileDescriptor2.length
                )
                prepare()
            }
            assetFileDescriptor2.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Phát âm thanh khi trả lời đúng
     */
    fun playCorrectSound() {
        if (!SettingsManager.isSoundEnabled) return
        try {
            correctSoundPlayer?.let { player ->
                player.setVolume(SettingsManager.volume, SettingsManager.volume)
                if (player.isPlaying) {
                    player.seekTo(0)
                } else {
                    player.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Phát âm thanh khi trả lời sai
     */
    fun playWrongSound() {
        if (!SettingsManager.isSoundEnabled) return
        try {
            wrongSoundPlayer?.let { player ->
                player.setVolume(SettingsManager.volume, SettingsManager.volume)
                if (player.isPlaying) {
                    player.seekTo(0)
                } else {
                    player.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Giải phóng tài nguyên MediaPlayer
     */
    fun release() {
        try {
            correctSoundPlayer?.release()
            wrongSoundPlayer?.release()
            correctSoundPlayer = null
            wrongSoundPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Composable function để tạo và quản lý SoundHelper
 * Tự động release khi component bị dispose
 */
@Composable
fun rememberSoundHelper(): SoundHelper {
    val context = LocalContext.current
    val soundHelper = remember { SoundHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            soundHelper.release()
        }
    }

    return soundHelper
}
