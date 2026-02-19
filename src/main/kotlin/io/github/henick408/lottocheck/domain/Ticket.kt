package io.github.henick408.lottocheck.domain

import java.time.LocalDate

class Ticket(
    val gameType: GameType,
    val drawDate: LocalDate,
    val ticketNumbers: MutableList<TicketNumbers> = mutableListOf()
) {
    fun addNumbers(numbers: Set<Int>, specialNumbers: Set<Int>? = null): Boolean {
        return this.ticketNumbers.add(TicketNumbers(this.gameType, numbers, specialNumbers))
    }
}