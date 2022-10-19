package com.github.andreypfau.raptorq.parameters

import com.github.andreypfau.raptorq.util.SystematicIndices
import com.github.andreypfau.raptorq.util.math.ceilDiv
import kotlin.math.min

// requires individually bounded arguments
internal fun getPossibleTotalSymbols(F: Long, T: Int): Long {
    return ceilDiv(F, T.toLong())
}

// requires individually and in unison bounded arguments
internal fun getTotalSymbols(F: Long, T: Int): Int {
    return ceilDiv(F, T.toLong()).toInt() // downcast never overflows since F and T are bounded
}

// requires bounded argument
// since interleaving is disabled, this should always return 1
internal fun topInterleaverLength(T: Int): Int {
    // interleaving is disabled for now

    // the maximum allowed interleaver length
    return T / T
}

// requires valid arguments
internal fun KL(WS: Long, T: Int, Al: Int, n: Int): Int {
    // must cast to int after getting the minimum to avoid integer overflow
    val K_upper_bound = min(K_max.toLong(), WS / subSymbolSize(T, Al, n)).toInt()
    return SystematicIndices.floor(K_upper_bound)
}

// requires valid arguments
internal fun minWS(Kprime: Int, T: Int, Al: Int, n: Int): Long {
    // must cast to long because product may exceed Integer.MAX_VALUE
    return SystematicIndices.ceil(Kprime).toLong() * subSymbolSize(T, Al, n)
}

// since interleaving is disabled, this should always return T
private fun subSymbolSize(T: Int, Al: Int, n: Int): Int {
    return Al * ceilDiv(T, Al * n)
}
