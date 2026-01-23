package org.henick.lottolib.domain

import java.time.LocalDate

// Jakby kartka z losami
class Ticket(
    val gameType: GameType,
    val drawDate: LocalDate,
    val ticketNumbers: List<TicketNumbers>
)