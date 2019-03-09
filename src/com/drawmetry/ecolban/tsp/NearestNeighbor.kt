package com.drawmetry.ecolban.tsp

// pre-condition: nodes.all { !it.mark }
fun nearestNeighbor(nodes: List<Node>): Sequence<Node> = generateSequence(nodes[0]) { current ->
    current.mark = true;
    nodes.filter { !it.mark }.minBy { current.distanceTo(it) }
}

