package dev.ryanness.lighteuchre.data

import com.thelightphone.sdk.SealedLightContext
import com.thelightphone.sdk.buildDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Progress(
    val highestUnlockedLesson: Int,
    val completedLessons: Set<Int>,
    val gamesPlayed: Int,
    val gamesWon: Int,
) {
    companion object {
        val INITIAL = Progress(highestUnlockedLesson = 0, completedLessons = emptySet(), gamesPlayed = 0, gamesWon = 0)
    }
}

private fun ProgressEntity.toProgress() = Progress(
    highestUnlockedLesson = highestUnlockedLesson,
    completedLessons = completedLessons.split(",").filter { it.isNotBlank() }.map { it.toInt() }.toSet(),
    gamesPlayed = gamesPlayed,
    gamesWon = gamesWon,
)

class ProgressRepository(context: SealedLightContext) {
    private val dao = context.buildDatabase(ProgressDatabase::class.java, "light-euchre-progress.db").progressDao()

    val progress: Flow<Progress> = dao.observe().map { it?.toProgress() ?: Progress.INITIAL }

    suspend fun markLessonComplete(lessonIndex: Int, totalLessons: Int) {
        val current = dao.get()?.toProgress() ?: Progress.INITIAL
        val completed = current.completedLessons + lessonIndex
        val nextUnlock = maxOf(current.highestUnlockedLesson, minOf(lessonIndex + 1, totalLessons - 1))
        dao.upsert(
            ProgressEntity(
                highestUnlockedLesson = nextUnlock,
                completedLessons = completed.sorted().joinToString(","),
                gamesPlayed = current.gamesPlayed,
                gamesWon = current.gamesWon,
            ),
        )
    }

    suspend fun resetProgress() {
        dao.upsert(
            ProgressEntity(
                highestUnlockedLesson = Progress.INITIAL.highestUnlockedLesson,
                completedLessons = "",
                gamesPlayed = 0,
                gamesWon = 0,
            ),
        )
    }

    suspend fun recordGameResult(won: Boolean) {
        val current = dao.get()?.toProgress() ?: Progress.INITIAL
        dao.upsert(
            ProgressEntity(
                highestUnlockedLesson = current.highestUnlockedLesson,
                completedLessons = current.completedLessons.sorted().joinToString(","),
                gamesPlayed = current.gamesPlayed + 1,
                gamesWon = current.gamesWon + if (won) 1 else 0,
            ),
        )
    }
}
