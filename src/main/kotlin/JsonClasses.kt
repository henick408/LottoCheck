package org.henick


data class DrawResponse(
    val drawSystemId: Int,
    val drawDate: String,
    val gameType: String,
    val multiplierValue: Int?,
    val results: List<Result>,
    val showSpecialResults: Boolean,
    val isNewEuroJackpotDraw: Boolean
)

data class Result(
    val drawDate: String,
    val drawSystemId: Int,
    val gameType: String,
    val resultsJson: List<Int>,
    val specialResults: List<Int>
)

data class DrawResponseByDatePerGame(
    val totalRows: Int,
    val items: List<DrawResponse>,
    val meta: Any,
    val code: Int
)