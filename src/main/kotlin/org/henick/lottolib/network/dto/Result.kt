package org.henick.lottolib.network.dto

data class Result(
    val drawDate: String,
    val drawSystemId: Int,
    val gameType: String,
    val resultsJson: List<Int>,
    val specialResults: List<Int>
)