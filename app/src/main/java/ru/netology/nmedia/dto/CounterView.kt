package ru.netology.nmedia.dto

object CounterView {

    fun createCount(count: Int): String {
        var first: Int = 0
        var second: Int = 0
        var symbol: String = ""

        when {
            (count < 1000) -> first = count
            (count in 1000..9999) -> {
                first = count / 1000
                second = (count / 100) - (first * 10)
                symbol = "K"
            }

            (count in 10000..999_999) -> {
                first = count / 1000
                symbol = "K"
            }

            (count in 1_000_000..9_999_999) -> {
                first = count / 1_000_000
                second = (count / 100_000) - (first * 10)
                symbol = "M"
            }

            (count >= 10_000_000) -> {
                first = count / 1_000_000
                symbol = "M"
            }
        }
        return first.toString() + if (second != 0) ".$second$symbol" else symbol
    }
}