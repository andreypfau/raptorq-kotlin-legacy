package com.github.andreypfau.raptorq.arraymap

class ImmutableListMapBuilder(
    var numKeys: Int,
    var entries: MutableList<Pair<Short, Int>> = ArrayList()
) {
    fun add(key: Short, value: Int) {
        entries.add(key to value)
    }

    fun build(): ImmutableListMap {
        entries.sortBy { it.first }
        val offsets = IntArray(numKeys) { Int.MAX_VALUE }
        var lastKey = entries[0].first
        offsets[lastKey.toInt()] = 0
        val values = IntArray(entries.size)
        entries.forEachIndexed { index, (key, value) ->
            if (lastKey != key) {
                lastKey = key
                offsets[key.toInt()] = index
            }
            values[index] = value
        }
        for (i in offsets.size - 1 downTo 0) {
            if (offsets[i] == Int.MAX_VALUE) {
                if (i == offsets.size - 1) {
                    offsets[i] = entries.size
                } else {
                    offsets[i] = offsets[i + 1]
                }
            }
        }
        return ImmutableListMap(offsets, values)
    }
}
