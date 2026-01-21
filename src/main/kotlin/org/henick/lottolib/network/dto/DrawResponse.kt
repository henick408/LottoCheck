package org.henick.lottolib.network.dto

data class DrawResponse(
    val drawSystemId: Int,
    val drawDate: String,
    val gameType: String,
    val multiplierValue: Int?,
    val results: List<Result>,
    val showSpecialResults: Boolean,
    val isNewEuroJackpotDraw: Boolean
)