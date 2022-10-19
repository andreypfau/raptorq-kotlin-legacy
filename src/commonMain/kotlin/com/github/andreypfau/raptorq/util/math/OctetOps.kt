package com.github.andreypfau.raptorq.util.math

import kotlin.experimental.xor

object OctetOps {
    fun vectorVectorAddition(vector1: ByteArray, vector2: ByteArray, result: ByteArray) {
        vectorVectorAddition(vector1, 0, vector2, 0, result, 0, result.size)
    }

    fun vectorVectorAddition(
        vector1: ByteArray,
        vecPos1: Int,
        vector2: ByteArray,
        vecPos2: Int,
        result: ByteArray,
        resPos: Int,
        length: Int
    ) {
        val resEnd = resPos + length
        var v1 = vecPos1
        var v2 = vecPos2
        var r = resPos
        while (r < resEnd) {
            result[r] = aPlusB(vector1[v1], vector2[v2])
            v1++
            v2++
            r++
        }
    }

    fun aPlusB(u: Byte, v: Byte): Byte = u xor v
}
