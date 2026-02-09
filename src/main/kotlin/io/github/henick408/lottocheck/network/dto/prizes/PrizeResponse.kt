package io.github.henick408.lottocheck.network.dto.prizes

data class PrizeResponse(
    val prizes: Map<String, PrizeDto>,
    val drawDate: String,
    val drawSystemId: Int,
    val gameType: String,
    val prizesEmpty: Boolean
)
