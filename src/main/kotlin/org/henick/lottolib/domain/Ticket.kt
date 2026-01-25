package org.henick.lottolib.domain

import java.time.LocalDate

class Ticket(
    val gameType: GameType,
    val drawDate: LocalDate,
    val ticketNumbers: MutableList<TicketNumbers> = mutableListOf()
) {
    fun add(ticketNumbers: TicketNumbers): Boolean {
        this.ticketNumbers.add(ticketNumbers)
        return true
    }
}