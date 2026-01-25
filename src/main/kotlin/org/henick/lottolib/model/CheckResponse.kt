package org.henick.lottolib.model

data class CheckResponse(
    val drawSystemId: Int? = null,
    val results: List<Int>? = null,
    val specialResults: List<Int>? = null,
    val winInfoJson: List<WinInfo>? = null,
    val info: String? = null
)