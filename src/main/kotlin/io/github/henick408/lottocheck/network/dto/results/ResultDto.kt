package io.github.henick408.lottocheck.network.dto.results

data class ResultDto(
    val drawDate: String,
    val drawSystemId: Int,
    val gameType: String,
    val resultsJson: List<Int>,
    val specialResults: List<Int>
)