package io.github.henick408.lottocheck.model

import io.github.henick408.lottocheck.domain.GameType

data class CheckResponse(
    val drawSystemId: Int? = null,
    val gameType: GameType? = null,
    val results: List<Int>? = null,
    val specialResults: List<Int>? = null,
    val winInfoJson: List<WinInfo>? = null,
    val info: String? = null
) {
    fun getPrizes(): List<Double> {
        val prizes: MutableList<Double> = mutableListOf()
        this.winInfoJson?.forEach { prizes.addAll(it.getPrizes()) }
        return prizes
    }
}