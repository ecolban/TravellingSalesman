package com.drawmetry.ecolban.tsp

import java.lang.IllegalArgumentException

class ReversibleNode<V>(val value: V, private var positiveDirection: Boolean = true) {
    private var prevPtr: ReversibleNode<V>? = null
    private var nextPtr: ReversibleNode<V>? = null

    var prev: ReversibleNode<V>
        get() = (if (positiveDirection) prevPtr else nextPtr) ?: this
        set(node) {
            if (positiveDirection) {
                prevPtr = node
            } else {
                nextPtr = node
            }
        }

    var next: ReversibleNode<V>
        get() = (if (positiveDirection) nextPtr else prevPtr) ?: this
        set(node) {
            if (positiveDirection) {
                nextPtr = node
            } else {
                prevPtr = node
            }
        }

    internal fun reverse() {
        positiveDirection = !positiveDirection
    }
}

class ReversibleList<T : Any>(startValue: T) : Iterable<T> {

    var start: ReversibleNode<T> = ReversibleNode(value = startValue)

    val nodeSequence: Sequence<ReversibleNode<T>>
        get() = iteration(start) { if (it.next == start) null else it.next }

    var size: Int = 1

    override fun iterator(): Iterator<T> = nodeSequence.map { it.value }.iterator()

    fun add(value: T) {
        val newNode = ReversibleNode(value)
        val last = start.prev
        last.next = newNode
        newNode.next = start
        start.prev = newNode
        newNode.prev = last
        size += 1
    }


    fun reverse(b: ReversibleNode<T>, c: ReversibleNode<T>) {
        if (b == c) return
        if (c.next == b) { // reverse the entire list
            nodeSequence.forEach { it.reverse() }
            return
        }
        val a = b.prev
        val d = c.next
        a.next = c
        c.next = a
        d.prev = b
        b.prev = d
        var current = c
        while (current != d) {
            current.reverse()
            current = current.next
        }
    }

    override fun toString(): String = joinToString(prefix = "[", separator = ", ", postfix = "]")

    operator fun get(i: Int): T {
        var current = start
        var count = 0
        while (count < i) {
            count++
            current = current.next
        }
        return current.value
    }

    companion object {
        fun <T:Any> listOf(vararg elements: T): ReversibleList<T> {
            if (elements.isEmpty()) throw IllegalArgumentException("A ReversibleList must contain at least on element.")
            val iterator = elements.iterator()
            val result = ReversibleList(iterator.next())
            for (e in iterator) {
                result.add(e)
            }
            return result
        }
    }
}
