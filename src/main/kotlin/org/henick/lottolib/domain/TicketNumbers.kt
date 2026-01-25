package org.henick.lottolib.domain

class TicketNumbers(
    val gameType: GameType,
    val numbers: Set<Int>,
    val specialNumbers: Set<Int>? = null
) {
    internal fun isValidSize(): Boolean {
        if (specialNumbers == null) {
            return this.numbers.size == this.gameType.amount
        }
        return numbers.size == gameType.amount && specialNumbers.size == gameType.specialAmount
    }

    internal fun isValidRange(): Boolean {
        if (specialNumbers == null) {
            return numbers.all { it in gameType.range }
        }
        return numbers.all { it in gameType.range } && specialNumbers.all { it in gameType.specialRange!! }
    }
}