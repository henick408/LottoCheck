package org.henick.lottolib.network.dto.results

data class ResultDto(
    val drawDate: String,
    val drawSystemId: Int,
    val gameType: String,
    val resultsJson: List<Int>,
    val specialResults: List<Int>
)