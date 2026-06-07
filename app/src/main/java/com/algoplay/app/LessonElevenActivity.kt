package com.algoplay.app

import android.view.View

class LessonElevenActivity : LessonOneActivity() {

    override val layoutResId: Int = R.layout.activity_lesson_eleven
    override val lessonNumber: Int = 11
    override val nextLessonNumber: Int = 12

    override val quizOneOptions: List<String> = emptyList()
    override val quizTwoOptions: List<String> = emptyList()

    override fun buildGame() {
        gameChoiceContainer.removeAllViews()
        gameSlotContainer.removeAllViews()
        btnResetGame.visibility = View.GONE
        btnCheckGame.visibility = View.GONE
    }
}
