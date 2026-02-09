package io.github.henick408.lottocheck.network.dto.results

data class DrawResponse(
    val drawSystemId: Int,
    val drawDate: String,
    val gameType: String,
    val multiplierValue: Int?,
    val results: List<ResultDto>,
    val showSpecialResults: Boolean,
    val isNewEuroJackpotDraw: Boolean
)