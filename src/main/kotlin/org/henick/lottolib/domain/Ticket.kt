package org.henick.lottolib.domain

import java.time.LocalDate

class Ticket(
    val gameType: GameType,
    val drawDate: LocalDate,
    val ticketNumbers: MutableList<TicketNumbers> = mutableListOf()
) {
    fun addNumbers(numbers: Set<Int>, specialNumbers: Set<Int>? = null): Boolean {
        this.ticketNumbers.add(TicketNumbers(this.gameType, numbers, specialNumbers))
        return true
    }
}