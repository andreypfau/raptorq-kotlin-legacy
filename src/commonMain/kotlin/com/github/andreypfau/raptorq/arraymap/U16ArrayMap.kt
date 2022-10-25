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
        val temp = elements[key]
        elements[key] = elements[otherKey]
        elements[otherKey] = temp
    }

    operator fun get(key: Int): UShort = elements[key - offset]

    operator fun set(key: Int, value: UShort) {
        elements[key - offset] = value
    }

    fun keys() = (offset until offset + elements.size).asSequence()

    fun increment(key: Int) {
        elements[key - offset]++
    }

    fun decrement(key: Int) {
        elements[key - offset]--
    }

    fun copy(
        offset: Int = this.offset,
        elements: UShortArray = this.elements
    ) = U16ArrayMap(offset, elements.copyOf())
}
