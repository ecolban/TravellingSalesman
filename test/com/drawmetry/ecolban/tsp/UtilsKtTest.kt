package com.drawmetry.ecolban.tsp

import org.junit.Assert.*
import org.junit.Test

class UtilsKtTest {

    @Test
    fun `test iteration on Fibonacci sequence`() {
        val fib = iteration(first = Pair(1, 1), step = { Pair(it.second, it.first + it.second) })
                .map { it.first }

        val fib20 = fib.takeWhile { it < 10000 }.toList()
        assertEquals(1, fib20[0])
        assertEquals(1, fib20[1])
        for (i in 0 until fib20.size - 2) {
            assertTrue(fib20[i] + fib20[i + 1] == fib20[i + 2])
        }
    }

    @Test
    fun `test iteration on Collatz sequence`() {
        fun collatz(start: Int) = iteration(start) {
            when {
                it == 1 -> null
                it % 2 == 0 -> it / 2
                else -> 3 * it + 1
            }
        }
        assertEquals(listOf(15, 46, 23, 70, 35, 106, 53, 160, 80, 40, 20, 10, 5, 16, 8, 4, 2, 1),
                collatz(15).toList())
        assertEquals(107, collatz(31).count())
    }
}