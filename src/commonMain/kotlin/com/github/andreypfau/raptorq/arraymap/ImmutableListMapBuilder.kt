@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.arraymap

class ImmutableListMapBuilder(
    var numKeys: Int,
    var entries: MutableList<Pair<UShort, UInt>> = ArrayList()
) {
    fun add(key: UShort, value: UInt) {
        entries.add(key to value)
    }

    fun build(): ImmutableListMap {
        entries.sortBy { it.first }
        check(entries.size < Int.MAX_VALUE)
        check(entries.isNotEmpty())
        val offsets = UIntArray(numKeys) { UInt.MAX_VALUE }
        var lastKey = entries.first().first
        offsets[lastKey.toInt()] = 0u
        val values = ArrayList<UInt>()
        entries.forEachIndexed { index, (key, value) ->
            if (lastKey != key) {
                offsets[key.toInt()] = index.toUInt()
                lastKey = key
            }
            values.add(value)
        }
        for (i in offsets.lastIndex downTo 0) {
            if (offsets[i] == UInt.MAX_VALUE) {
                if (i == offsets.lastIndex) {
                    offsets[i] = values.size.toUInt()
                } else {
                    offsets[i] = offsets[i + 1]
                }
            }
        }
        return ImmutableListMap(offsets, values.toUIntArray())
    }
}
