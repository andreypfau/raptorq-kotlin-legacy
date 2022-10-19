package com.github.andreypfau.raptorq.util

import kotlin.jvm.JvmStatic
import kotlin.math.min

object Deg {
    private val table = longArrayOf(
        0,
        5243,
        529531,
        704294,
        791675,
        844104,
        879057,
        904023,
        922747,
        937311,
        948962,
        958494,
        966438,
        973160,
        978921,
        983914,
        988283,
        992138,
        995565,
        998631,
        1001391,
        1003887,
        1006157,
        1008229,
        1010129,
        1011876,
        1013490,
        1014983,
        1016370,
        1017662,
        1048576
    )

    @JvmStatic
    fun deg(v: Long, w: Long): Long {
        var i = 0
        while (i < 31) {
            if (v < table[i]) break
            i++
        }
        if (i == 31) throw RuntimeException("Inconsistent table state")
        return min(i.toLong(), w - 2)
    }
}
