package com.algoplay.app

import android.content.Context

object ProgressBridge {
    private const val PREF_PROGRESS = "algoplay_local_progress"
    private const val KEY_COMPLETED_LESSONS = "completed_lessons"
    private const val KEY_COMPLETED_ACTIVITIES = "completed_activities"
    private const val KEY_TRAINING_BEST_SCORES = "training_best_scores"
    private const val KEY_TRAINING_LIFETIME_SCORE = "training_lifetime_score"
    private const val KEY_WEEKLY_LEADERBOARD_SCORE = "weekly_leaderboard_score"
    private const val KEY_LEADERBOARD_WEEK_KEY = "leaderboard_week_key"
    private const val KEY_FINAL_EXAM_SCORE = "final_exam_score"
    private const val KEY_MATERIAL_COMPLETE_BADGE = "material_complete_badge"
    private const val KEY_LAST_CONTINUE_TYPE = "last_continue_type"
    private const val KEY_LAST_CONTINUE_KEY = "last_continue_key"
    private const val GUEST_PENDING_PREFIX = "guest_pending"

    fun scopedKey(userId: String, key: String): String {
        return "${userId}_$key"
    }

    fun saveGuestSnapshot(
        context: Context,
        completedLessons: Set<Int>,
        completedActivities: Set<String>,
        trainingBestScores: Map<String, Int> = emptyMap(),
        trainingLifetimeScore: Int = 0,
        weeklyLeaderboardScore: Int = 0,
        leaderboardWeekKey: String = "",
        finalExamScore: Int,
        materialCompleteBadgeUnlocked: Boolean = false,
        lastContinueType: String?,
        lastContinueKey: String?
    ) {
        context.getSharedPreferences(PREF_PROGRESS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(
                "${GUEST_PENDING_PREFIX}_exists",
                completedLessons.isNotEmpty() ||
                    completedActivities.isNotEmpty() ||
                    trainingBestScores.isNotEmpty() ||
                    trainingLifetimeScore > 0 ||
                    weeklyLeaderboardScore > 0 ||
                    finalExamScore > 0 ||
                    materialCompleteBadgeUnlocked
            )
            .putString("${GUEST_PENDING_PREFIX}_$KEY_COMPLETED_LESSONS", completedLessons.sorted().joinToString(","))
            .putString("${GUEST_PENDING_PREFIX}_$KEY_COMPLETED_ACTIVITIES", completedActivities.sorted().joinToString(","))
            .putString("${GUEST_PENDING_PREFIX}_$KEY_TRAINING_BEST_SCORES", encodeTrainingScores(trainingBestScores))
            .putInt("${GUEST_PENDING_PREFIX}_$KEY_TRAINING_LIFETIME_SCORE", trainingLifetimeScore.coerceAtLeast(0))
            .putInt("${GUEST_PENDING_PREFIX}_$KEY_WEEKLY_LEADERBOARD_SCORE", weeklyLeaderboardScore.coerceAtLeast(0))
            .putString("${GUEST_PENDING_PREFIX}_$KEY_LEADERBOARD_WEEK_KEY", leaderboardWeekKey)
            .putInt("${GUEST_PENDING_PREFIX}_$KEY_FINAL_EXAM_SCORE", finalExamScore)
            .putBoolean("${GUEST_PENDING_PREFIX}_$KEY_MATERIAL_COMPLETE_BADGE", materialCompleteBadgeUnlocked)
            .putString("${GUEST_PENDING_PREFIX}_$KEY_LAST_CONTINUE_TYPE", lastContinueType)
            .putString("${GUEST_PENDING_PREFIX}_$KEY_LAST_CONTINUE_KEY", lastContinueKey)
            .apply()
    }

    fun mergePendingGuestProgress(context: Context, userId: String) {
        val store = context.getSharedPreferences(PREF_PROGRESS, Context.MODE_PRIVATE)
        if (!store.getBoolean("${GUEST_PENDING_PREFIX}_exists", false)) return

        val pendingLessons = store.getString("${GUEST_PENDING_PREFIX}_$KEY_COMPLETED_LESSONS", "")
            .orEmpty()
            .split(",")
            .mapNotNull { it.toIntOrNull() }
            .toSet()
        val pendingActivities = store.getString("${GUEST_PENDING_PREFIX}_$KEY_COMPLETED_ACTIVITIES", "")
            .orEmpty()
            .split(",")
            .filter { it.isNotBlank() }
            .toSet()

        val accountLessons = store.getString(scopedKey(userId, KEY_COMPLETED_LESSONS), "")
            .orEmpty()
            .split(",")
            .mapNotNull { it.toIntOrNull() }
            .toSet()
        val accountActivities = store.getString(scopedKey(userId, KEY_COMPLETED_ACTIVITIES), "")
            .orEmpty()
            .split(",")
            .filter { it.isNotBlank() }
            .toSet()

        val pendingExamScore = store.getInt("${GUEST_PENDING_PREFIX}_$KEY_FINAL_EXAM_SCORE", 0)
        val accountExamScore = store.getInt(scopedKey(userId, KEY_FINAL_EXAM_SCORE), 0)
        val pendingTrainingLifetimeScore = store.getInt("${GUEST_PENDING_PREFIX}_$KEY_TRAINING_LIFETIME_SCORE", 0)
        val accountTrainingLifetimeScore = store.getInt(scopedKey(userId, KEY_TRAINING_LIFETIME_SCORE), 0)
        val pendingLeaderboardWeekKey = store.getString("${GUEST_PENDING_PREFIX}_$KEY_LEADERBOARD_WEEK_KEY", "").orEmpty()
        val accountLeaderboardWeekKey = store.getString(scopedKey(userId, KEY_LEADERBOARD_WEEK_KEY), "").orEmpty()
        val pendingWeeklyLeaderboardScore = store.getInt("${GUEST_PENDING_PREFIX}_$KEY_WEEKLY_LEADERBOARD_SCORE", 0)
        val accountWeeklyLeaderboardScore = store.getInt(scopedKey(userId, KEY_WEEKLY_LEADERBOARD_SCORE), 0)
        val pendingTrainingScores = parseTrainingScores(store.getString("${GUEST_PENDING_PREFIX}_$KEY_TRAINING_BEST_SCORES", "").orEmpty())
        val accountTrainingScores = parseTrainingScores(store.getString(scopedKey(userId, KEY_TRAINING_BEST_SCORES), "").orEmpty())
        val mergedTrainingScores = (pendingTrainingScores.keys + accountTrainingScores.keys).associateWith { key ->
            maxOf(pendingTrainingScores[key] ?: 0, accountTrainingScores[key] ?: 0)
        }
        val pendingMaterialBadge = store.getBoolean("${GUEST_PENDING_PREFIX}_$KEY_MATERIAL_COMPLETE_BADGE", false)
        val accountMaterialBadge = store.getBoolean(scopedKey(userId, KEY_MATERIAL_COMPLETE_BADGE), false)
        val mergedWeeklyScore = if (pendingLeaderboardWeekKey == accountLeaderboardWeekKey) {
            pendingWeeklyLeaderboardScore + accountWeeklyLeaderboardScore
        } else {
            accountWeeklyLeaderboardScore
        }

        store.edit()
            .putString(scopedKey(userId, KEY_COMPLETED_LESSONS), (accountLessons + pendingLessons).sorted().joinToString(","))
            .putString(scopedKey(userId, KEY_COMPLETED_ACTIVITIES), (accountActivities + pendingActivities).sorted().joinToString(","))
            .putString(scopedKey(userId, KEY_TRAINING_BEST_SCORES), encodeTrainingScores(mergedTrainingScores))
            .putInt(scopedKey(userId, KEY_TRAINING_LIFETIME_SCORE), accountTrainingLifetimeScore + pendingTrainingLifetimeScore)
            .putInt(scopedKey(userId, KEY_WEEKLY_LEADERBOARD_SCORE), mergedWeeklyScore)
            .putString(scopedKey(userId, KEY_LEADERBOARD_WEEK_KEY), accountLeaderboardWeekKey.ifBlank { pendingLeaderboardWeekKey })
            .putInt(scopedKey(userId, KEY_FINAL_EXAM_SCORE), maxOf(accountExamScore, pendingExamScore))
            .putBoolean(scopedKey(userId, KEY_MATERIAL_COMPLETE_BADGE), accountMaterialBadge || pendingMaterialBadge)
            .putString(scopedKey(userId, KEY_LAST_CONTINUE_TYPE), store.getString("${GUEST_PENDING_PREFIX}_$KEY_LAST_CONTINUE_TYPE", null))
            .putString(scopedKey(userId, KEY_LAST_CONTINUE_KEY), store.getString("${GUEST_PENDING_PREFIX}_$KEY_LAST_CONTINUE_KEY", null))
            .remove("${GUEST_PENDING_PREFIX}_exists")
            .remove("${GUEST_PENDING_PREFIX}_$KEY_COMPLETED_LESSONS")
            .remove("${GUEST_PENDING_PREFIX}_$KEY_COMPLETED_ACTIVITIES")
            .remove("${GUEST_PENDING_PREFIX}_$KEY_TRAINING_BEST_SCORES")
            .remove("${GUEST_PENDING_PREFIX}_$KEY_TRAINING_LIFETIME_SCORE")
            .remove("${GUEST_PENDING_PREFIX}_$KEY_WEEKLY_LEADERBOARD_SCORE")
            .remove("${GUEST_PENDING_PREFIX}_$KEY_LEADERBOARD_WEEK_KEY")
            .remove("${GUEST_PENDING_PREFIX}_$KEY_FINAL_EXAM_SCORE")
            .remove("${GUEST_PENDING_PREFIX}_$KEY_MATERIAL_COMPLETE_BADGE")
            .remove("${GUEST_PENDING_PREFIX}_$KEY_LAST_CONTINUE_TYPE")
            .remove("${GUEST_PENDING_PREFIX}_$KEY_LAST_CONTINUE_KEY")
            .apply()
    }

    private fun parseTrainingScores(raw: String): Map<String, Int> {
        return raw.split(";")
            .mapNotNull { item ->
                val parts = item.split("=", limit = 2)
                val key = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val score = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                key to score.coerceAtLeast(0)
            }
            .toMap()
    }

    private fun encodeTrainingScores(scores: Map<String, Int>): String {
        return scores.entries
            .sortedBy { it.key }
            .joinToString(";") { "${it.key}=${it.value.coerceAtLeast(0)}" }
    }
}
