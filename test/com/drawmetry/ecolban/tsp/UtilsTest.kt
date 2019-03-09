package com.drawmetry.ecolban.tsp

import org.junit.Test
import java.time.Instant.now
import kotlin.test.assertEquals

class UtilsTest {

    @Test
    fun `test iteration on Fibonacci sequence`() {
        val fib = generateSequence(
                seed = Pair(1, 1),
                nextFunction = { Pair(it.second, it.first + it.second) })
                .map { it.first }

        val fib20 = fib.takeWhile { it < 10000 }.toList()
        assertEquals(1, fib20[0])
        assertEquals(1, fib20[1])
        for (n in 2 until fib20.size) {
            assertEquals(fib20[n - 2] + fib20[n - 1], fib20[n])
        }
    }

    @Test
    fun `test generateSequence on Collatz sequence`() {
        fun collatz(start: Int) = generateSequence(start) { current ->
            when {
                current == 1 -> null
                current % 2 == 0 -> current / 2
                else -> 3 * current + 1
            }
        }
        assertEquals(listOf(15, 46, 23, 70, 35, 106, 53, 160, 80, 40, 20, 10, 5, 16, 8, 4, 2, 1),
                collatz(15).toList())
        assertEquals(107, collatz(31).count())
    }

    private fun fizzBuzz(n: Int) = when {
        n % 15 == 0 -> "FizzBuzz"
        n % 3 == 0 -> "Fizz"
        n % 5 == 0 -> "Buzz"
        else -> n.toString()
    }

    private val nats
        get() = generateSequence(1) { it + 1 }

    private val fizzBuzzSeq
        get() = nats.map(::fizzBuzz)

    private val fizzBuzzSeqAlt
        get() = generateSequence(0) { it + 15 }.flatMap(::seqOfFifteen)

    private fun seqOfFifteen(i: Int): Sequence<String> = sequenceOf(
            (1 + i).toString(), (2 + i).toString(), "Fizz", (4 + i).toString(), "Buzz", "Fizz",
            (7 + i).toString(), (8 + i).toString(), "Fizz", "Buzz", (11 + i).toString(), "Fizz",
            (13 + i).toString(), (14 + i).toString(), "FizzBuzz")


    @Test
    fun `test FizzBuzz first 15 elements`() {
        assertEquals("1 2 Fizz 4 Buzz Fizz 7 8 Fizz Buzz 11 Fizz 13 14 FizzBuzz".split(" "),
                fizzBuzzSeq.take(15).toList())
    }

    @Test
    fun `test FizzBuzz first 100 elements`() {
        val first100 = "1 2 Fizz 4 Buzz Fizz 7 8 Fizz Buzz 11 Fizz 13 14 FizzBuzz " +
                "16 17 Fizz 19 Buzz Fizz 22 23 Fizz Buzz 26 Fizz 28 29 FizzBuzz " +
                "31 32 Fizz 34 Buzz Fizz 37 38 Fizz Buzz 41 Fizz 43 44 FizzBuzz " +
                "46 47 Fizz 49 Buzz Fizz 52 53 Fizz Buzz 56 Fizz 58 59 FizzBuzz " +
                "61 62 Fizz 64 Buzz Fizz 67 68 Fizz Buzz 71 Fizz 73 74 FizzBuzz " +
                "76 77 Fizz 79 Buzz Fizz 82 83 Fizz Buzz 86 Fizz 88 89 FizzBuzz " +
                "91 92 Fizz 94 Buzz Fizz 97 98 Fizz Buzz"
        val expected = first100.split(" ")
        val actual = fizzBuzzSeq.take(100).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun `test fizzBuzzSeq vs fizzBuzzSeqAlt`() {
        println("start: ${now()}")
        fizzBuzzSeq.zip(fizzBuzzSeqAlt).take(1_000_000).forEach {
            assertEquals(it.first, it.second)
        }
        println("end:   ${now()}")
    }

    @Test
    fun `test print`() {
        fizzBuzzSeqAlt.take(100).forEach(::println)
    }
}