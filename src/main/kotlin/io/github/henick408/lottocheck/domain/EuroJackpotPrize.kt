package io.github.henick408.lottocheck.domain

internal object EuroJackpotPrize {

    private val prizesMatrix: List<List<Int?>> = listOf(
        listOf(null, null, null),
        listOf(null, null, 11),
        listOf(null, 12, 8),
        listOf(10, 9, 6),
        listOf(7, 5, 4),
        listOf(3, 2, 1)
    )

    operator fun get(index1: Int, index2: Int): Int? {
        return prizesMatrix[index1][index2]
    }
}