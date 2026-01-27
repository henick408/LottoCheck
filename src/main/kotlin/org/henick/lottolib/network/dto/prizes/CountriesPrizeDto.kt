package org.henick.lottolib.network.dto.prizes

data class CountriesPrizeDto(
    val degree: Int,
    val countWinners: Int,
    val prizesValues: List<CountryPrizeValue>,
)