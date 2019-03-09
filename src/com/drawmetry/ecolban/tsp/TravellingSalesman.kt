package com.drawmetry.ecolban.tsp

import java.awt.*
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.lang.Math.min
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

private class Panel : JPanel(), Display {

    private var nodes: Array<Node> = arrayOf()

    override fun display(nodes: Array<Node>) {
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
        if (nodes.size > 1) {
            val path = Path2D.Double()
            path.append(nodes.toPathIterator(), true)
            g2.draw(path)
        }
        g2.drawString("Length = %.2f".format(len(nodes)), 10, 30)
    }

    companion object {

        const val WINDOW_WIDTH: Int = 900
        const val WINDOW_HEIGHT: Int = 600

    }

}

fun Array<Node>.toPathIterator() = object : PathIterator {

    var index = 0

    override fun next() {
        index++
    }

    override fun getWindingRule(): Int = PathIterator.WIND_NON_ZERO

    override fun currentSegment(coords: DoubleArray): Int {
        if (index < size) {
            val node = this@toPathIterator[index]
            coords[0] = node.x
            coords[1] = node.y
        }
        return when {
            index == 0 -> PathIterator.SEG_MOVETO
            index < size -> PathIterator.SEG_LINETO
            else -> PathIterator.SEG_CLOSE
        }
    }

    override fun currentSegment(coords: FloatArray?): Int {
        throw UnsupportedOperationException("Not implemented")
    }

    override fun isDone(): Boolean = index > size
}

private fun len(nodes: Array<Node>): Double = (0 until nodes.size - 1)
        .sumByDouble { nodes[it].distanceTo(nodes[it + 1]) }

interface Display {
    fun display(nodes: Array<Node>)
}

class TravellingSalesman(private val nodeArray: Array<Node>, val display: Display)
    : SwingWorker<Array<Node>, Array<Node>>() {

    private val initialTemperature = 100.0

    override fun doInBackground(): Array<Node> {
        var temperature: Double = initialTemperature
        val nodes = ReversibleList<Node>(nodeArray[0])
        (1 until nodeArray.size).forEach {
            nodes.add(nodeArray[it])
        }
        val nodesPublished = nodes.toList().toTypedArray()
        publish(nodesPublished)
        var count = 0
        while (temperature > 0.5) {
            temperature *= 0.99
            localOptimize(nodes, temperature)
            count++
            if (count % 2 == 0) {
                nodes.forEachIndexed { i, node -> nodesPublished[i] = node }
                publish(nodesPublished)
            }
            progress = (temperature * 100.0 / initialTemperature).toInt()
        }
        progress = 0

        return nodes.toList().toTypedArray()

    }

    override fun process(chunks: List<Array<Node>>) {
        val nodes = chunks[chunks.size - 1]
        display.display(nodes)
    }

    private fun localOptimize(nodes: ReversibleList<Node>, temperature: Double) {
        for (a: ReversibleNode<Node> in nodes.nodeSequence) {
            if (a.next.next == nodes.start) break
            var c: ReversibleNode<Node> = a.next.next
            while (c.next != a.prev.prev) {
                val b = a.next
                val d = c.next
                val before = a.value.distanceTo(b.value) + c.value.distanceTo(d.value)
                val after = a.value.distanceTo(c.value) + b.value.distanceTo(d.value)
                val threshold = before + (if (Random.nextInt(5) < 2) temperature else 0.0)
                if (after < threshold) {
                    nodes.reverse(b, c)
                }
                c = d
            }
        }
    }
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


fun main() {

//    val nodeArray1: Array<Node> =
//            File("/Users/erikc/IdeaProjects/Robot/examples/res/homer-simpson.txt").useLines {
//                val lines = it.iterator()
//                val nodes = mutableListOf<Node>()
//                val coords = doubleArrayOf(0.0, 0.0)
//                var currentPath = initPathNode(coords, lines.next())
//                for (line in lines) {
//                    if (line.startsWith("M")) {
//                        nodes.add(endPathNode(coords, currentPath))
//                        currentPath = initPathNode(coords, line)
//                    } else {
//                        currentPath.add(line)
//                    }
//                }
//                nodes.add(endPathNode(coords, currentPath))
//                nodes.add(dummyNode)
//                nodes
//            }.toTypedArray()
    val numRows = 120
    val numCols = 90
    val numPoints = numRows * numCols
//    val start = randomNode()
//    val nodeArray2: Array<Node> = Array(numPoints + 1) {
//        if (it == 0 || it == numPoints) start else randomNode()
//    }
    val xUnit = Panel.WINDOW_WIDTH.toDouble() / numCols.toDouble()
    val yUnit = Panel.WINDOW_HEIGHT.toDouble() / numRows.toDouble()
//    val unit = min(xUnit, yUnit)
    val xMargin = (Panel.WINDOW_WIDTH - (numCols - 1).toDouble() * xUnit) / 2.0
    val yMargin = (Panel.WINDOW_HEIGHT - (numRows - 1).toDouble() * yUnit) / 2.0
    val nodeArray3: Array<Node> = Array(numPoints + 1) {
        val x = (it % numPoints / numRows).toDouble() * xUnit + xMargin
        val y = (it % numRows).toDouble() * yUnit + yMargin
        PathNode(x, y, "", false)
    }

    SwingUtilities.invokeLater {
        val panel = Panel()
        val progressBar = JProgressBar(VERTICAL, 0, 100)
        val frame = JFrame()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        panel.preferredSize = Dimension(Panel.WINDOW_WIDTH, Panel.WINDOW_HEIGHT)
        frame.layout = BorderLayout()
        frame.add(progressBar, BorderLayout.WEST)
        frame.add(panel, BorderLayout.CENTER)
        frame.pack()
        frame.isVisible = true
        val travellingSalesman = TravellingSalesman(nodeArray3, display = panel)
        travellingSalesman.addPropertyChangeListener { evt ->
            if ("progress" == evt.propertyName) {
                progressBar.value = evt.newValue as Int
            }
        }
        travellingSalesman.execute()
    }

}
