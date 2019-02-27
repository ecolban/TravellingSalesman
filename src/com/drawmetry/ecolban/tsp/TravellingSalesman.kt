package com.drawmetry.ecolban.tsp

import java.awt.*
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON
import java.awt.geom.Line2D
import java.io.File
import java.util.*
import javax.swing.*
import javax.swing.SwingConstants.VERTICAL
import kotlin.random.Random
import java.lang.System.currentTimeMillis as now


interface Node {

    val x: Double
    val y: Double
    var mark: Boolean
    fun distanceTo(other: Node): Double
}

private val dummyNode: Node = PathNode(-1.0, -1.0, "")

private data class PathNode(override val x: Double,
                            override val y: Double,
                            val path: String,
                            override var mark: Boolean = false) : Node {

    override fun distanceTo(other: Node): Double {
        if (other == dummyNode) return 0.0
        val a = x - other.x
        val b = y - other.y
        return Math.sqrt(a * a + b * b)
    }

    override fun toString() = path

}

data class Edge(val fromNode: Node, val toNode: Node) : Comparable<Edge> {

    private val weight: Double = fromNode.distanceTo(toNode)

    override fun compareTo(other: Edge): Int = Math.signum(weight - other.weight).toInt()
}

private const val INITIAL_TEMPERATURE = 50.0

private class Panel : JPanel() {

    private var nodes: Array<Node> = arrayOf()

    fun display(nodes: Array<Node>) {
        this.nodes = nodes
        repaint()
    }

    override fun paintComponent(g: Graphics?) {
        val g2 = g as Graphics2D
        g2.color = Color.BLUE
        g2.fillRect(0, 0, width, height)
        g2.color = Color.WHITE
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
        g2.stroke = BasicStroke(1.5F)
        val n = nodes.size
        for (i in 0 until n - 1) {
            val line = Line2D.Double(nodes[i].x, nodes[i].y, nodes[i + 1].x, nodes[i + 1].y)
            g2.draw(line)
        }
        g2.drawString("Length = %.2f".format(len(nodes)), 10, 30)
    }

    companion object {

        const val WINDOW_WIDTH: Int = 900
        const val WINDOW_HEIGHT: Int = 600

    }

}

private fun len(nodes: Array<Node>): Double = (0 until nodes.size - 1)
        .map { nodes[it].distanceTo(nodes[it + 1]) }
        .sum()

private class TravellingSalesman(private val panel: Panel) : SwingWorker<Array<Node>, Array<Node>>() {

    override fun doInBackground(): Array<Node> {
        var temperature: Double = INITIAL_TEMPERATURE
        val nodes = nodeArray2
        val nodesPublished = nearestNeighbor(nodes.toList()).toList().toTypedArray()
        publish(nodesPublished)
        var count = 0
        while (temperature > 0.5) {
            temperature *= 0.999
            localOptimize(nodes, temperature)
            count++
            if (count % 10 == 0) {
                System.arraycopy(nodes, 0, nodesPublished, 0, nodes.size)
                publish(nodesPublished)
            }
            progress = (temperature * 100.0 / INITIAL_TEMPERATURE).toInt()
            Thread.sleep(1)
        }
        progress = 0

        return nodesPublished

    }

    override fun process(chunks: List<Array<Node>>) {
        val nodes = chunks[chunks.size - 1]
        panel.display(nodes)
    }

    private fun localOptimize(nodes: Array<Node>, temperature: Double) {
        val n = nodes.size
        val indexOrder = (0 until n).shuffled()
        for (i in 0 until n) {
            for (j in 0 until n) {
                val a = indexOrder[i]
                val c = indexOrder[j]
                if (c - a < 2 || c == n - 1) continue
                val b = a + 1
                val d = c + 1
                val before = nodes[a].distanceTo(nodes[b]) + nodes[c].distanceTo(nodes[d])
                val after = nodes[a].distanceTo(nodes[c]) + nodes[b].distanceTo(nodes[d])
                if (before > after || before + temperature > after && Random.nextInt(5) < 2) {
                    nodes.reverse(b, c)
                }
            }
        }
    }
}

fun nearestNeighbor(nodes: List<Node>): Sequence<Node> {
    // pre-condition: nodes.all { !it.mark }
    var next: Node? = if (nodes.isNotEmpty()) nodes[0] else null
    return generateSequence {
        next?.let { current ->
            current.mark = true
            next = nodes.filter { !it.mark }.minBy { current.distanceTo(it) }
            current
        }
    }
}

fun minimumSpanningTree(nodes: List<Node>): Sequence<Edge> {
    assert(nodes.all { !it.mark })
    if (nodes.size < 2) return emptySequence()
    val queue = PriorityQueue<Edge>()
    // Invariant: queue.all {it.fromNode.mark}
    nodes[0].mark = true
    queue.addAll(nodes.filter { !it.mark }.map { Edge(nodes[0], it) })
    var nextEdge: Edge? = queue.pollUntil { !it.toNode.mark }
    return generateSequence {
        nextEdge?.let { currentEdge ->
            currentEdge.toNode.mark = true
            queue.addAll(nodes.filter { !it.mark }.map { Edge(currentEdge.toNode, it) })
            nextEdge = queue.pollUntil { !it.toNode.mark }
            currentEdge
        }
    }
}

private fun <T : Comparable<T>> PriorityQueue<T>.pollUntil(isGood: (T) -> Boolean): T? {
    var element: T = poll() ?: return null
    while (!isGood(element)) {
        element = poll() ?: return null
    }
    return element
}

private val nodeArray1: Array<Node>
    get() =
        File("examples/res/homer-simpson.txt").useLines {
            val lines = it.iterator()
            if (!lines.hasNext()) return emptyArray()
            val nodes = mutableListOf<Node>()
            val coords = doubleArrayOf(0.0, 0.0)
            var currentPath = initPathNode(coords, lines.next())
            for (line in lines) {
                if (line.startsWith("M")) {
                    nodes.add(endPathNode(coords, currentPath))
                    currentPath = initPathNode(coords, line)
                } else {
                    currentPath.add(line)
                }
            }
            nodes.add(endPathNode(coords, currentPath))
            nodes.add(dummyNode)
            nodes
        }.toTypedArray()


private val start = randomNode()
private val nodeArray2: Array<Node> = Array(5001) {
    if (it == 0 || it == 5000) start else randomNode()
}

private val nodeArray3: Array<Node> = Array(1001) {
    val xUnit = Panel.WINDOW_WIDTH.toDouble() / 40
    val yUnit = Panel.WINDOW_HEIGHT.toDouble() / 25
    val x = (it % 1000 / 25).toDouble() * xUnit + xUnit / 2.0
    val y = (it % 25).toDouble() * yUnit + yUnit / 2.0
    PathNode(x, y, "", false)
}

private fun randomNode() = PathNode(
        Random.nextDouble() * Panel.WINDOW_WIDTH,
        Random.nextDouble() * Panel.WINDOW_HEIGHT,
        "", false)


fun initPathNode(coords: DoubleArray, line: String): MutableList<String> {
    val floatRegex = """[-+]?\d*\.?\d+""".toRegex()
    floatRegex.findAll(line).forEachIndexed { i, matchResult ->
        coords[i] = matchResult.value.toDouble()
    }
    return mutableListOf(line)
}

private fun endPathNode(coords: DoubleArray, currentPath: MutableList<String>) =
        PathNode(coords[0], coords[1], currentPath.joinToString(separator = "\n"))

private fun <T> Array<T>.reverse(start: Int, end: Int) {
    var i = start
    var j = end
    while (i < j) {
        val tmp = this[i]
        this[i++] = this[j]
        this[j--] = tmp
    }
}

fun main() {

    val panel: Panel by lazy {
        Panel()
    }

    val progressBar: JProgressBar by lazy {
        JProgressBar(VERTICAL, 0, 100)
    }

    SwingUtilities.invokeAndWait {
        val frame = JFrame()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        panel.preferredSize = Dimension(Panel.WINDOW_WIDTH, Panel.WINDOW_HEIGHT)
        frame.layout = BorderLayout()
        frame.add(progressBar, BorderLayout.WEST)
        frame.add(panel, BorderLayout.CENTER)
        frame.pack()
        frame.isVisible = true
    }
    val travellingSalesman = TravellingSalesman(panel = panel)
    travellingSalesman.addPropertyChangeListener { evt ->
        if ("progress" == evt.propertyName) {
            progressBar.value = evt.newValue as Int
        }
    }
    travellingSalesman.execute()
//    val nodes = travellingSalesman.get()
//    File("examples/res/homer-simpson-opt.txt").printWriter().use {
//        for (node in nodes.reversed()) {
//            it.println(node.toString())
//        }
//    }


}
