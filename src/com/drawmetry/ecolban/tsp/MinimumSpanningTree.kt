package com.drawmetry.ecolban.tsp

import java.util.*

fun minimumSpanningTree(nodes: List<Node>): Sequence<Edge> {
    assert(nodes.all { !it.mark })
    if (nodes.size < 2) return emptySequence()
    val queue = PriorityQueue<Edge>()
    // Invariant: queue.all {it.fromNode.mark}
    nodes[0].mark = true
    queue.addAll(nodes.filter { !it.mark }.map { Edge(nodes[0], it) })

    val firstEdge = queue.pollUntil { !it.toNode.mark }
    return generateSequence<Edge>(firstEdge) { currentEdge ->
        currentEdge.toNode.mark = true
        queue.addAll(nodes.filter { !it.mark }.map { Edge(currentEdge.toNode, it) })
        queue.pollUntil { !it.toNode.mark }
    }
}