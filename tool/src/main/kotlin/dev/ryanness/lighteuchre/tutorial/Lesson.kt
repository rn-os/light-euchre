package dev.ryanness.lighteuchre.tutorial

sealed interface LessonStep {
    data class Explain(val title: String?, val body: String) : LessonStep

    data class Quiz(
        val question: String,
        val options: List<String>,
        val correctIndex: Int,
        val explanation: String,
    ) : LessonStep
}

data class Lesson(
    val id: Int,
    val title: String,
    val summary: String,
    val steps: List<LessonStep>,
)
