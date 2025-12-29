package com.example.mathforkids.model

import androidx.compose.ui.graphics.Color

/**
 * Game types for kids - Duolingo style progression
 */
enum class GameType(val displayName: String, val emoji: String, val color: Color) {
    COUNTING("Đếm số", "🔢", Color(0xFF4CAF50)),
    ADDITION("Cộng", "➕", Color(0xFF2196F3)),
    SUBTRACTION("Trừ", "➖", Color(0xFFFFC107)),
    MATCHING("Ghép số", "🎯", Color(0xFFE91E63)),
    WRITING("Bé tập viết", "✏️", Color(0xFFFF9800)) // Đã thêm game mới
}

/**
 * Represents a level in the game path
 */
data class GameLevel(
    val id: Int,
    val gameType: GameType,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val stars: Int = 0, // 0-3 stars
    val position: LevelPosition // Position on the Duolingo-style path
)

/**
 * Position of a level on the visual path
 */
data class LevelPosition(
    val x: Float, // 0.0 to 1.0 (percentage of screen width)
    val y: Float  // Y position in dp from top
)

/**
 * Game result for tracking
 * QUAN TRỌNG: Giữ cấu trúc này để lưu vào Database
 */
data class GameResult(
    val correctAnswers: Int,   // Số câu trả lời đúng
    val totalQuestions: Int,   // Tổng số câu hỏi
    val gameType: GameType,
    val level: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Question for different game types
 */
sealed class GameQuestion {
    abstract val correctAnswer: Int

    data class CountingQuestion(
        val objectCount: Int,
        override val correctAnswer: Int = objectCount
    ) : GameQuestion()

    data class AdditionQuestion(
        val num1: Int,
        val num2: Int,
        override val correctAnswer: Int = num1 + num2
    ) : GameQuestion()

    data class SubtractionQuestion(
        val num1: Int,
        val num2: Int,
        override val correctAnswer: Int = num1 - num2
    ) : GameQuestion()

    data class MatchingQuestion(
        val number: Int,
        override val correctAnswer: Int = number
    ) : GameQuestion()

    // Đã thêm câu hỏi tập viết từ code mới
    data class WritingQuestion(
        val targetDigit: Int,
        override val correctAnswer: Int = targetDigit
    ) : GameQuestion()
}