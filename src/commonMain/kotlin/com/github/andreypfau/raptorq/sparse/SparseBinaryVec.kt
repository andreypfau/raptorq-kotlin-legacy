@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.sparse

import com.github.andreypfau.raptorq.octet.Octet
import com.github.andreypfau.raptorq.utils.nextOrNull

class SparseBinaryVec(
    val elements: MutableList<UShort>
) : Iterable<UShort> by elements {
    fun keyToInternalIndex(i: UShort): Int = elements.binarySearch { it.compareTo(i) }

    val size: Int get() = elements.size

    fun getByRawIndex(i: Int): Pair<Int, Octet> = elements[i].toInt() to Octet.ONE

    fun addAssign(other: SparseBinaryVec): Boolean {
        if (other.elements.size == 1) {
            val otherIndex = other.elements[0]
            val index = keyToInternalIndex(otherIndex)
            if (index >= 0) {
                elements.removeAt(index)
            } else {
                elements.add(-index - 1, otherIndex)
                return true
            }
            return false
        }

        val result = mutableListOf<UShort>()
        val selfIter = elements.iterator()
        val otherIter = other.elements.iterator()
        var selfNext = selfIter.nextOrNull()
        var otherNext = otherIter.nextOrNull()

        var columnAdded = false
        while (true) {
            if (selfNext != null) {
                if (otherNext != null) {
                    when (selfNext.compareTo(otherNext)) {
                        -1 -> {
                            result.add(selfNext)
                            selfNext = selfIter.nextOrNull()
                        }

                        0 -> {
                            selfNext = selfIter.nextOrNull()
                            otherNext = otherIter.nextOrNull()
                        }

                        1 -> {
                            columnAdded = true
                            result.add(otherNext)
                            otherNext = otherIter.nextOrNull()
                        }
                    }
                } else {
                    result.add(selfNext)
                    selfNext = selfIter.nextOrNull()
                }
            } else if (otherNext != null) {
                columnAdded = true
                result.add(otherNext)
                otherNext = otherIter.nextOrNull()
            } else {
                break
            }
        }
        elements.clear()
        elements.addAll(result)

        return columnAdded
    }

    fun remove(i: Int): Octet? {
        val index = keyToInternalIndex(i.toUShort())
        if (index >= 0) {
            elements.removeAt(index)
            return Octet.ONE
        }
        return null
    }

    fun retain(predicate: (Pair<Int, Octet>) -> Boolean) {
        elements.retainAll { predicate(it.toInt() to Octet.ONE) }
    }

    operator fun get(i: Int): Octet? {
        val index = keyToInternalIndex(i.toUShort())
        if (index >= 0) {
            return Octet.ONE
        }
        return null
    }

    fun keysValues(): Sequence<Pair<Int, Octet>> = elements.asSequence().map { it.toInt() to Octet.ONE }

    fun insert(i: Int, value: Octet) {
        require(i < 65536)
        if (value == Octet.ZERO) {
            remove(i)
        } else {
            val index = keyToInternalIndex(i.toUShort())
            if (index < 0) {
                elements.add(-index - 1, i.toUShort())
            }
        }
    }
}
