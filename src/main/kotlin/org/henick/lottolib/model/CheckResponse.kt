package org.henick.lottolib.model

data class CheckResponse(
    val winInfoJson: List<WinInfo> = listOf()
)