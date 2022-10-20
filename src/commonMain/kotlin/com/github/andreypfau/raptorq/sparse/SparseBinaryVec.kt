@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.sparse

import com.github.andreypfau.raptorq.utils.nextOrNull

class SparseBinaryVec(
    // Kept sorted by the usize (key). Only ones are stored, zeros are implicit
    private var elements: MutableList<UShort>
) : Iterable<UShort> {
    constructor(capacity: Int) : this(ArrayList(capacity))

    // Returns the internal index into self.elements matching key i, or the index
    // at which it can be inserted (maintaining sorted order)
    fun keyToInternalIndex(i: UShort): Int = elements.binarySearch { it.compareTo(i) }

    val size: Int get() = elements.size

    fun getByRawIndex(i: Int): Pair<Int, Boolean> = elements[i].toInt() to true

    // Returns true, if a new column was added
    fun addAssign(other: SparseBinaryVec): Boolean {
        // Fast path for a single value that's being eliminated
        if (other.elements.size == 1) {
            val otherIndex = other.elements[0]
            val index = keyToInternalIndex(otherIndex)
            if (index >= 0) {
                // Adding 1 + 1 = 0 in GF(256), so remove this
                elements.removeAt(index)
            } else {
                elements.add(otherIndex)
                elements.sort()
                return true
            }
            return false
        }

        val result = ArrayList<UShort>(elements.size + other.elements.size)
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
                            // Adding 1 + 1 = 0 in GF(256), so skip this index
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
        elements = result
        return columnAdded
    }

    fun remove(i: Int): Boolean {
        val index = keyToInternalIndex(i.toUShort())
        if (index >= 0) {
            elements.removeAt(index)
            return true
        }
        return false
    }

    fun retain(predicate: (Pair<Int, Boolean>) -> Boolean) {
        elements.retainAll { predicate(it.toInt() to true) }
    }

    fun keysValues(): Sequence<Pair<Int, Boolean>> = elements.asSequence().map { it.toInt() to true }

    operator fun get(i: Int): Boolean {
        val index = keyToInternalIndex(i.toUShort())
        if (index >= 0) {
            return true
        }
        return false
    }

    operator fun set(i: Int, value: Boolean) {
        if (value) {
            val index = keyToInternalIndex(i.toUShort())
            if (index < 0) {
                elements.add(-index - 1, i.toUShort())
            }
        } else {
            remove(i)
        }
    }

    override fun iterator(): Iterator<UShort> = elements.iterator()
}
