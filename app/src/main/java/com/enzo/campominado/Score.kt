package com.enzo.campominado

data class Score(
    val name: String,
    val time: Int,
    val difficulty: String,
    val win: Boolean,
    val points: Int,
    val avatar: String = "👦"
)