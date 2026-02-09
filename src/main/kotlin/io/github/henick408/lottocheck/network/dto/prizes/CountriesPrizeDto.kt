package io.github.henick408.lottocheck.network.dto.prizes

data class CountriesPrizeDto(
    val degree: Int,
    val countWinners: Int,
    val prizesValues: List<CountryPrizeValue>,
)