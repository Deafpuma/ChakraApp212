package com.chakrawellness.app.models

data class QuizResult(
    val quizResults: Map<String, Map<String, Int>> = emptyMap(),
    val timestamp: Long = 0
)
