package com.drawmetry.ecolban.tsp

fun <T : Any> iteration(first: T?, step: (T) -> T?): Sequence<T> {
    var next = first
    return generateSequence {
        next?.let {
            next = step(it)
            it
        }
    }
}