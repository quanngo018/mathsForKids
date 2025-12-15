package com.example.mathforkids.config

/**
 * Cấu hình trò chơi - Dễ dàng thay đổi các thông số
 */
object GameConfig {
    
    // ===== Cấu hình Level =====
    
    /** Số câu trả lời đúng cần thiết để mở khóa level tiếp theo */
    const val CORRECT_ANSWERS_TO_UNLOCK_NEXT_LEVEL = 3
    
    /** Số câu đúng để đạt 1 sao */
    const val CORRECT_FOR_ONE_STAR = 2
    
    /** Số câu đúng để đạt 2 sao */
    const val CORRECT_FOR_TWO_STARS = 4
    
    /** Số câu đúng để đạt 3 sao */
    const val CORRECT_FOR_THREE_STARS = 6
    
    /** Hiển thị thông báo hoàn thành level sau bao nhiêu câu đúng */
    const val SHOW_COMPLETION_MESSAGE_AT = CORRECT_ANSWERS_TO_UNLOCK_NEXT_LEVEL
    
    
    // ===== Cấu hình Độ Khó =====
    
    /** Counting Game - Số lượng tối đa theo level */
    object CountingDifficulty {
        const val LEVEL_1_MAX = 5
        const val LEVEL_2_MAX = 7
        const val LEVEL_3_MAX = 10
    }
    
    /** Addition Game - Số lớn nhất theo level */
    object AdditionDifficulty {
        const val LEVEL_1_MAX = 5
        const val LEVEL_2_MAX = 10
        const val LEVEL_3_MAX = 15
    }
    
    /** Subtraction Game - Số lớn nhất theo level */
    object SubtractionDifficulty {
        const val LEVEL_1_MAX = 5
        const val LEVEL_2_MAX = 10
        const val LEVEL_3_MAX = 15
    }
    
    /** Matching Game - Số lớn nhất theo level */
    object MatchingDifficulty {
        const val LEVEL_1_MAX = 5
        const val LEVEL_2_MAX = 8
        const val LEVEL_3_MAX = 10
    }
    
    
    // ===== Cấu hình UI =====
    
    /** Số lượng lựa chọn đáp án */
    const val NUMBER_OF_OPTIONS = 3
    
    /** Số lượng lựa chọn cho matching game */
    const val NUMBER_OF_MATCHING_OPTIONS = 4
    
    /** Thời gian delay (ms) trước khi chuyển câu tiếp theo sau khi trả lời đúng */
    const val DELAY_BEFORE_NEXT_QUESTION = 1500L
    
    /** Thời gian delay (ms) trước khi xóa feedback sau khi trả lời sai */
    const val DELAY_BEFORE_RETRY = 1500L
    
    /** Thời gian hiển thị celebration animation (ms) */
    const val CELEBRATION_DURATION = 2000L
    
    
    // ===== Cấu hình Animation =====
    
    /** Thời gian animation cho feedback (ms) */
    const val FEEDBACK_ANIMATION_DURATION = 500
    
    /** Thời gian animation cho button scale (ms) */
    const val BUTTON_SCALE_DURATION = 300
    
    
    // ===== Cấu hình Visual =====
    
    /** Số lượng object tối đa hiển thị trong visual counting */
    const val MAX_VISUAL_OBJECTS = 10
    
    /** Số object tối đa mỗi hàng trong counting game */
    const val MAX_OBJECTS_PER_ROW = 5
    
    /** Số object tối đa mỗi hàng trong matching game */
    const val MAX_MATCHING_OBJECTS_PER_ROW = 4
    
    
    // ===== Helper Functions =====
    
    /**
     * Tính số sao dựa trên số câu trả lời đúng
     */
    fun calculateStars(correctAnswers: Int): Int {
        return when {
            correctAnswers >= CORRECT_FOR_THREE_STARS -> 3
            correctAnswers >= CORRECT_FOR_TWO_STARS -> 2
            correctAnswers >= CORRECT_FOR_ONE_STAR -> 1
            else -> 0
        }
    }
    
    /**
     * Kiểm tra xem đã đủ điều kiện unlock level tiếp theo chưa
     */
    fun canUnlockNextLevel(correctAnswers: Int): Boolean {
        return correctAnswers >= CORRECT_ANSWERS_TO_UNLOCK_NEXT_LEVEL
    }
    
    /**
     * Lấy số tối đa cho counting game theo level
     */
    fun getCountingMaxForLevel(level: Int): Int {
        return when {
            // New Counting Levels (1000+)
            level >= 1000 -> {
                val relativeLevel = level - 1000
                when (relativeLevel) {
                    1 -> 3  // Level 1: 0-3
                    2 -> 5  // Level 2: 0-5
                    3 -> 7  // Level 3: 0-7
                    else -> 10 // Level 4+: 0-10
                }
            }
            // Legacy Levels
            level == 1 -> CountingDifficulty.LEVEL_1_MAX
            level == 2 -> CountingDifficulty.LEVEL_2_MAX
            else -> CountingDifficulty.LEVEL_3_MAX
        }
    }
    
    /**
     * Lấy số tối đa cho addition game theo level
     */
    fun getAdditionMaxForLevel(level: Int): Int {
        return when {
            // New Addition Levels (2000+)
            level >= 2000 -> {
                val relativeLevel = level - 2000
                when (relativeLevel) {
                    1 -> 3  // Sum <= 3
                    2 -> 5  // Sum <= 5
                    3 -> 7  // Sum <= 7
                    else -> 10 // Sum <= 10
                }
            }
            // Legacy Levels
            level == 1 -> AdditionDifficulty.LEVEL_1_MAX
            level == 2 -> AdditionDifficulty.LEVEL_2_MAX
            else -> AdditionDifficulty.LEVEL_3_MAX
        }
    }
    
    /**
     * Lấy số tối đa cho subtraction game theo level
     */
    fun getSubtractionMaxForLevel(level: Int): Int {
        return when (level) {
            1 -> SubtractionDifficulty.LEVEL_1_MAX
            2 -> SubtractionDifficulty.LEVEL_2_MAX
            else -> SubtractionDifficulty.LEVEL_3_MAX
        }
    }
    
    /**
     * Lấy số tối đa cho matching game theo level
     */
    fun getMatchingMaxForLevel(level: Int): Int {
        return when (level) {
            1 -> MatchingDifficulty.LEVEL_1_MAX
            2 -> MatchingDifficulty.LEVEL_2_MAX
            else -> MatchingDifficulty.LEVEL_3_MAX
        }
    }
}
