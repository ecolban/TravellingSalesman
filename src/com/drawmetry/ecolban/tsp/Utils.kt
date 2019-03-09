package com.drawmetry.ecolban.tsp

import java.util.*

fun <T : Comparable<T>> PriorityQueue<T>.pollUntil(isGood: (T) -> Boolean): T? {
    var element: T = poll() ?: return null
    while (!isGood(element)) {
        element = poll() ?: return null
    }
    return element
}
