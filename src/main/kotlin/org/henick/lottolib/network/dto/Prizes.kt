package org.henick.lottolib.network.dto

import com.google.gson.annotations.SerializedName

data class Prizes(
    @SerializedName("1")
    val prize1: Prize?,
    @SerializedName("2")
    val prize2: Prize?,
    @SerializedName("3")
    val prize3: Prize?
)
