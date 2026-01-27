package org.henick.lottolib.network.dto

data class PrizeResponse(
    val prizes: Prizes,
    val drawDate: String,
    val drawSystemId: Int,
    val gameType: String,
    val prizesEmpty: Boolean
)
