package com.drawmetry.ecolban.tsp

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestReversibleList() {

    @Test
    fun `test initialization of a list`() {
        val list = ReversibleList(1)
        assertNotNull(list.start)
        assertEquals(1, list.start.value)
    }

    @Test
    fun `test ReversibleList listOf`() {
        val list = ReversibleList.listOf(1, 2, 3, 4)
        assertNotNull(list)
        assertEquals(4, list.size)
        assertEquals("[1, 2, 3, 4]", list.toString())
    }

    @Test
    fun `test adding an element to a list`() {
        val list = ReversibleList(1)
        list.add(2)
        assertEquals(2, list.size)
        assertEquals("[1, 2]", list.toString())
        list.add(3)
        list.add(4)
        list.add(5)
        assertEquals(5, list.size)
        assertEquals("[1, 2, 3, 4, 5]", list.toString())
    }

    @Test
    fun `test reversing a singleton list`() {
        val list = ReversibleList.listOf(1)
        list.reverse(list.start, list.start)
        assertEquals("[1]", list.toString())
    }

    @Test
    fun `test reversing a doubleton list`() {
        val list = ReversibleList.listOf(1, 2)
        list.reverse(list.start, list.start.next)
        assertEquals("[1, 2]", list.toString())
    }

    @Test
    fun `test indexing operator`() {
        val list = ReversibleList.listOf(1, 2, 3, 4)
        for (i in 0 until list.size) {
            assertEquals(i + 1, list[i])
        }
    }

    @Test
    fun `test reversing a list with more than 2 elements`() {
        val list = ReversibleList.listOf(1, 2, 3, 4, 5)
        val node1 = list.start
        val node2 = node1.next
        val node3 = node2.next
        val node4 = node3.next
        val node5 = node4.next
        list.reverse(node1, node5)
        assertEquals("[1, 5, 4, 3, 2]", list.toString())
        list.reverse(node5, node2)
        assertEquals("[1, 2, 3, 4, 5]", list.toString())
        list.reverse(node2, node4)
        assertEquals("[1, 4, 3, 2, 5]", list.toString())
    }

    @Test
    fun `test nodeSequence`() {
        val list = ReversibleList.listOf('a', 'b', 'c', 'd')
        val node1 = list.start
        val node2 = node1.next
        val node3 = node2.next
        val node4 = node3.next
        assertEquals(listOf(node1, node2, node3, node4), list.nodeSequence.toList())
        assertEquals(listOf('a', 'b', 'c', 'd'), list.nodeSequence.map { it.value }.toList())
    }
}