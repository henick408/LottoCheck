package io.github.henick408.lottocheck.domain

enum class GameType(
    val gameName: String,
    val range: IntRange,
    val amount: Int,
    val specialRange: IntRange? = null,
    val specialAmount: Int? = null,
    val nonSpecialGame: GameType? = null
) {
    LOTTO( gameName = "Lotto", range = 1..49, amount = 6),
    LOTTOPLUS( gameName = "LottoPlus", range = 1..49, amount = 6, nonSpecialGame = LOTTO),
    MINILOTTO( gameName = "MiniLotto", range = 1..42, amount = 5),
    EUROJACKPOT( gameName = "EuroJackpot", range = 1..50, amount = 5, specialRange = 1..12, specialAmount = 2)
}