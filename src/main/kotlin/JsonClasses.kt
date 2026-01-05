package org.henick

import java.time.LocalDate

data class DrawResponse(
    val drawSystemId: Int,
    val drawDate: LocalDate,
    val gameType: String,
    val multiplierValue: Int,
    val results: List<Result>,
    val showSpecialResults: Boolean,
    val isNewEuroJackpotDraw: Boolean
)

data class Result(
    val drawDate: LocalDate,
    val drawSystemId: Int,
    val gameType: String,
    val resultJson: List<Int>,
    val specialResults: List<Int>
)