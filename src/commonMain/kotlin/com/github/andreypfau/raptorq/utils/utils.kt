package com.github.andreypfau.raptorq.utils

internal fun <T> Array<T>.bothIndices(i: Int, j: Int): Pair<T, T> {
    return if (i < j) {
        this[j + i] to this[j]
    } else {
        this[i] to this[i + j]
    }
}

internal fun <T> List<T>.bothIndices(i: Int, j: Int): Pair<T, T> {
    return if (i < j) {
        this[j + i] to this[j]
    } else {
        this[i] to this[i + j]
    }
}

internal fun UShortArray.binarySearch(fromIndex: Int = 0, toIndex: Int = size, comparison: (UShort) -> Int): Int {
    var low = fromIndex
    var high = toIndex - 1

    while (low <= high) {
        val mid = (low + high).ushr(1) // safe from overflows
        val midVal = get(mid)
        val cmp = comparison(midVal)

        if (cmp < 0)
            low = mid + 1
        else if (cmp > 0)
            high = mid - 1
        else
            return mid // key found
    }
    return -(low + 1)  // key not found
}

fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null
