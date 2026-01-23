package org.henick.lottolib.domain

// rząd liczb na karteczce
class TicketNumbers(
    val numbers: Set<Int>,
    val gameType: GameType,
    val specialNumbers: Set<Int>? = null
) {
    private fun isValidSize(): Boolean {
        if (specialNumbers == null) {
            return this.numbers.size != this.gameType.amount
        }
        return numbers.size != gameType.amount || specialNumbers.size != gameType.specialAmount
    }

    private fun isValidRange(): Boolean {
        if (specialNumbers == null) {
            return numbers.all { it in gameType.range }
        }
        return numbers.all { it in gameType.range } && specialNumbers.all { it in gameType.specialRange!! }
    }
}