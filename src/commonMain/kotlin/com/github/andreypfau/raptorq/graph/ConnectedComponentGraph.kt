package com.github.andreypfau.raptorq.graph

import com.github.andreypfau.raptorq.arraymap.U16ArrayMap
import kotlin.math.max
import kotlin.math.min

class ConnectedComponentGraph private constructor(
    // Mapping from nodes to their connected component id
    nodeConnectedComponent: U16ArrayMap,
    // Mapping from original connected component id to the one they've been merged with
    mergedConnectedComponents: U16ArrayMap,
    // Size of each connected component in the graph
    connectedComponentsSize: U16ArrayMap,
    numConnectedComponents: Int
) {
    var nodeConnectedComponent: U16ArrayMap = nodeConnectedComponent
        private set
    var mergedConnectedComponents: U16ArrayMap = mergedConnectedComponents
        private set
    var connectedComponentsSize: U16ArrayMap = connectedComponentsSize
        private set
    var numConnectedComponents: Int = numConnectedComponents
        private set

    fun createConnectedComponent(): UShort {
        numConnectedComponents++
        return (NO_CONNECTED_COMPONENT + numConnectedComponents.toUInt()).toUShort()
    }

    fun addNode(node: Int, connectedComponent: UShort) {
        require(connectedComponent <= numConnectedComponents.toUInt())
        require(nodeConnectedComponent[node] == NO_CONNECTED_COMPONENT)
        val canonical = canonicalComponentId(connectedComponent)
        nodeConnectedComponent[node] = canonical
        connectedComponentsSize.increment(canonical.toInt())
    }

    fun swap(node1: Int, node2: Int) {
        nodeConnectedComponent.swap(node1, node2)
    }

    operator fun contains(node: Int) =
        nodeConnectedComponent[node] != NO_CONNECTED_COMPONENT

    fun removeNode(node: Int) {
        val connectedComponent = canonicalComponentId(nodeConnectedComponent[node])
        if (connectedComponent == NO_CONNECTED_COMPONENT) {
            return
        }
        connectedComponentsSize.decrement(connectedComponent.toInt())
        nodeConnectedComponent[node] = NO_CONNECTED_COMPONENT
    }

    fun getNodeInLargestConnectedComponent(
        startNode: Int,
        endNode: Int
    ): Int {
        var maxSize = 0
        var largestConnectedComponent = NO_CONNECTED_COMPONENT
        for (i in 1..numConnectedComponents) {
            val size = connectedComponentsSize[i].toInt()
            if (size > maxSize) {
                maxSize = size
                largestConnectedComponent = i.toUShort()
            }
        }
        check(largestConnectedComponent != NO_CONNECTED_COMPONENT)

        // Find a node (column) in that connected component
        for (node in startNode until endNode) {
            if (canonicalComponentId(nodeConnectedComponent[node]) == largestConnectedComponent) {
                return node
            }
        }
        return -1
    }

    fun addEdge(node1: Int, node2: Int) {
        val connectedComponent1 = canonicalComponentId(nodeConnectedComponent[node1])
        val connectedComponent2 = canonicalComponentId(nodeConnectedComponent[node2])
        if (connectedComponent1 == NO_CONNECTED_COMPONENT && connectedComponent2 == NO_CONNECTED_COMPONENT) {
            val connectedComponentId = createConnectedComponent()
            nodeConnectedComponent[node1] = connectedComponentId
            nodeConnectedComponent[node2] = connectedComponentId
            connectedComponentsSize[connectedComponentId.toInt()] = 2u
        } else if (connectedComponent1 == NO_CONNECTED_COMPONENT) {
            connectedComponentsSize.increment(connectedComponent2.toInt())
            nodeConnectedComponent[node1] = connectedComponent2
        } else if (connectedComponent2 == NO_CONNECTED_COMPONENT) {
            connectedComponentsSize.increment(connectedComponent1.toInt())
            nodeConnectedComponent[node2] = connectedComponent1
        } else if (connectedComponent1 != connectedComponent2) {
            val mergeTo = min(connectedComponent1.toInt(), connectedComponent2.toInt())
            val mergeFrom = max(connectedComponent1.toInt(), connectedComponent2.toInt())
            val toSize = connectedComponentsSize[mergeTo]
            val fromSize = connectedComponentsSize[mergeFrom]
            connectedComponentsSize[mergeFrom] = 0u
            connectedComponentsSize[mergeTo] = (toSize + fromSize).toUShort()
            mergedConnectedComponents[mergeFrom] = mergeTo.toUShort()
        }
    }

    fun canonicalComponentId(id: UShort): UShort {
        if (id == NO_CONNECTED_COMPONENT) return id
        var currentId = id
        while (mergedConnectedComponents[currentId.toInt()] != currentId) {
            currentId = mergedConnectedComponents[currentId.toInt()]
        }
        return currentId
    }

    fun reset() {
        for (i in 1..numConnectedComponents) {
            connectedComponentsSize[i] = 0u
            mergedConnectedComponents[i] = i.toUShort()
        }
        numConnectedComponents = 0
        for (i in nodeConnectedComponent.keys()) {
            nodeConnectedComponent[i] = NO_CONNECTED_COMPONENT
        }
    }

    companion object {
        const val NO_CONNECTED_COMPONENT: UShort = 0u

        fun create(maxNodes: Int): ConnectedComponentGraph {
            val firstConnectedComponent = (NO_CONNECTED_COMPONENT + 1u).toInt()
            val result = ConnectedComponentGraph(
                nodeConnectedComponent = U16ArrayMap(0, maxNodes),
                mergedConnectedComponents = U16ArrayMap(firstConnectedComponent, firstConnectedComponent + maxNodes),
                connectedComponentsSize = U16ArrayMap(firstConnectedComponent, firstConnectedComponent + maxNodes),
                numConnectedComponents = 0
            )
            for (i in result.mergedConnectedComponents.keys()) {
                result.mergedConnectedComponents[i] = i.toUShort()
            }
            return result
        }
    }
}
