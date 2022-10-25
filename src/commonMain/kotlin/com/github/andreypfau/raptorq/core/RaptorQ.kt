@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.core

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

object RaptorQ {
    /**
     * `Partition[I, J]` function, as defined in section 4.4.1.2
     */
    fun partition(i: UInt, j: UInt, result: UIntArray = UIntArray(4)): UIntArray {
        result[0] = ceil(i.toDouble() / j.toDouble()).toUInt()
        result[1] = floor(i.toDouble() / j.toDouble()).toUInt()
        result[2] = i - result[1] * j
        result[3] = j - result[2]
        return result
    }

    /**
     * `Deg[v]` as defined in section 5.3.5.2
     */
    fun deg(v: UInt, ltSymbols: UInt): UInt {
        require(v < 1048576u) { "v must be less than 1048576" }
        val f = uintArrayOf(
            0u, 5243u, 529531u, 704294u, 791675u, 844104u, 879057u, 904023u, 922747u, 937311u, 948962u, 958494u,
            966438u, 973160u, 978921u, 983914u, 988283u, 992138u, 995565u, 998631u, 1001391u, 1003887u, 1006157u,
            1008229u, 1010129u, 1011876u, 1013490u, 1014983u, 1016370u, 1017662u, 1048576u,
        )
        for (d in 1 until f.size) {
            if (v < f[d]) {
                return min(d.toUInt(), ltSymbols - 2u)
            }
        }
        error("Should never happen")
    }

    /**
     * `Tuple[K', X]` as defined in section 5.3.5.4
     */
    fun tuple(
        internalSymbolId: UInt,
        ltSymbols: UInt,
        systematicIndex: UInt,
        p1: UInt,
        output: UIntArray = UIntArray(6)
    ): UIntArray {
        val j = systematicIndex
        val w = ltSymbols

        var A = 53591u + j * 997u
        if (A % 2u == 0u) {
            A++
        }

        val B = 10267u * (j + 1u)
        val y = ((B.toULong() + internalSymbolId.toULong() * A.toULong()) % 4294967296uL).toUInt()
        val v = rand(y, 0u, 1048576u)
        val d = deg(v, w)
        val a = 1u + rand(y, 1u, w - 1u)
        val b = rand(y, 2u, w)
        val d1 = if (d < 4u) {
            2u + rand(internalSymbolId, 3u, 2u)
        } else {
            2u
        }

        val a1 = 1u + rand(internalSymbolId, 4u, p1 - 1u)
        val b1 = rand(internalSymbolId, 5u, p1)

        output[0] = d
        output[1] = a
        output[2] = b
        output[3] = d1
        output[4] = a1
        output[5] = b1
        return output
    }

    /**
     * `Rand[y, i, m]` as defined in section 5.3.5.1
     */
    fun rand(y: UInt, i: UInt, m: UInt): UInt {
        require(m > 0u)
        val x0 = (y + i) % 256u
        val x1 = ((y shr 8) + i) % 256u
        val x2 = ((y shr 16) + i) % 256u
        val x3 = ((y shr 24) + i) % 256u

        return (V0[x0.toInt()] xor V1[x1.toInt()] xor V2[x2.toInt()] xor V3[x3.toInt()]) % m
    }
}
