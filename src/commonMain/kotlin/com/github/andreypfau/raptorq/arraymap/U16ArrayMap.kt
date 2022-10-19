@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.arraymap

class U16ArrayMap(
    private val offset: Int,
    private var elements: UShortArray
) {
    constructor(startKey: Int, endKey: Int) : this(
        offset = startKey,
        elements = UShortArray(endKey - startKey)
    )

    fun swap(key: Int, otherKey: Int) {
        val temp = get(key)
        set(key, get(otherKey))
        set(otherKey, temp)
    }

    fun get(key: Int): UShort = elements[key - offset]

    fun set(key: Int, value: UShort) {
        elements[key - offset] = value
    }

    fun keys() = (offset until offset + elements.size).asSequence()

    fun increment(key: Int) {
        elements[key - offset]++
    }

    fun decrement(key: Int) {
        elements[key - offset]--
    }
}
