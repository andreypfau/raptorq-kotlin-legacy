@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.arraymap

class U32VecMap(
    val offset: Int,
    var elements: UIntArray
) {
    constructor(startKey: Int) : this(
        offset = startKey,
        elements = UIntArray(1)
    )

    constructor(startKey: Int, endKey: Int) : this(
        offset = startKey,
        elements = UIntArray(endKey - startKey)
    )

    operator fun get(key: Int): UInt {
        if (key - offset >= elements.size) {
            return 0u
        }
        return elements[key - offset]
    }

    operator fun set(key: Int, value: UInt) {
        growIfNecessary(key - offset)
        elements[key - offset] = value
    }

    fun decrement(key: Int) {
        growIfNecessary(key - offset)
        elements[key - offset]--
    }

    fun increment(key: Int) {
        growIfNecessary(key - offset)
        elements[key - offset]++
    }

    private fun growIfNecessary(index: Int) {
        if (index >= elements.size) {
            elements = elements.copyOf(index + 1)
        }
    }
}
