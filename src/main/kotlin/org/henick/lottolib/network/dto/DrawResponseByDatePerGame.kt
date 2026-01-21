package org.henick.lottolib.network.dto

data class DrawResponseByDatePerGame(
    val totalRows: Int,
    val items: List<DrawResponse>,
    val meta: Any,
    val code: Int
)