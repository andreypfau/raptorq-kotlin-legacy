package com.github.andreypfau.raptorq.core

/**
 * Calculates, `K'`, the extended source block size, in symbols, for a given source block size
 * See section 5.3.1
 */
internal fun extendedSourceBlockSymbols(sourceBlockSymbols: Int): Int {
    require(sourceBlockSymbols <= MAX_SOURCE_SYMBOLS_PER_BLOCK) { "Source block symbols must be less than $MAX_SOURCE_SYMBOLS_PER_BLOCK" }
    SYSTEMATIC_INDICES_AND_PARAMETERS.forEach { (blockSize, _, _, _, _) ->
        if (blockSize >= sourceBlockSymbols) return blockSize
    }
    error("No block size found")
}

/**
 * Calculates, `J(K')`, the systematic index, for a given number of source block symbols
 * See section 5.6
 */
internal fun systematicIndex(sourceBlockSymbols: Int): Int {
    require(sourceBlockSymbols <= MAX_SOURCE_SYMBOLS_PER_BLOCK) { "Source block symbols must be less than $MAX_SOURCE_SYMBOLS_PER_BLOCK" }
    SYSTEMATIC_INDICES_AND_PARAMETERS.forEach { (blockSize, systematicIndex, _, _, _) ->
        if (blockSize >= sourceBlockSymbols) return systematicIndex
    }
    error("No systematic index found")
}

/**
 * Calculates, H(K'), the number of HDPC symbols, for a given number of source block symbols
 * See section 5.6
 */
internal fun numHdpcSymbols(sourceBlockSymbols: Int): Int {
    require(sourceBlockSymbols <= MAX_SOURCE_SYMBOLS_PER_BLOCK) { "Source block symbols must be less than $MAX_SOURCE_SYMBOLS_PER_BLOCK" }
    SYSTEMATIC_INDICES_AND_PARAMETERS.forEach { (blockSize, _, _, hdpcSymbols, _) ->
        if (blockSize >= sourceBlockSymbols) return hdpcSymbols
    }
    error("No HDPC symbols found")
}

/**
 * Calculates, S(K'), the number of LDPC symbols, for a given number of source block symbols
 * See section 5.6
 */
internal fun numLdpcSymbols(sourceBlockSymbols: Int): Int {
    require(sourceBlockSymbols <= MAX_SOURCE_SYMBOLS_PER_BLOCK) { "Source block symbols must be less than $MAX_SOURCE_SYMBOLS_PER_BLOCK" }
    SYSTEMATIC_INDICES_AND_PARAMETERS.forEach { (blockSize, _, ldpcSymbols, _, _) ->
        if (blockSize >= sourceBlockSymbols) return ldpcSymbols
    }
    error("No LDPC symbols found")
}

/**
 * Calculates, W(K'), the number of LT symbols, for a given number of source block symbols
 * See section 5.6
 */
internal fun numLtSymbols(sourceBlockSymbols: Int): Int {
    require(sourceBlockSymbols <= MAX_SOURCE_SYMBOLS_PER_BLOCK) { "Source block symbols must be less than $MAX_SOURCE_SYMBOLS_PER_BLOCK" }
    SYSTEMATIC_INDICES_AND_PARAMETERS.forEach { (blockSize, _, _, _, ltSymbols) ->
        if (blockSize >= sourceBlockSymbols) return ltSymbols
    }
    error("No LT symbols found")
}

/**
 * Calculates, L, the number of intermediate symbols, for a given number of source block symbols
 * See section 5.3.3.3
 */
internal fun numIntermediateSymbols(sourceBlockSymbols: Int): Int =
    extendedSourceBlockSymbols(sourceBlockSymbols) +
        numLdpcSymbols(sourceBlockSymbols) +
        numHdpcSymbols(sourceBlockSymbols)

/**
 * Calculates, P, the number of PI symbols, for a given number of source block symbols
 * See section 5.3.3.3
 */
internal fun numPiSymbols(sourceBlockSymbols: Int): Int =
    numIntermediateSymbols(sourceBlockSymbols) - numLtSymbols(sourceBlockSymbols)

internal fun calculateP1(sourceBlockSymbols: Int): Int {
    require(sourceBlockSymbols <= MAX_SOURCE_SYMBOLS_PER_BLOCK) { "Source block symbols must be less than $MAX_SOURCE_SYMBOLS_PER_BLOCK" }
    PI_TABLE.forEach { (blockSize, p1) ->
        if (blockSize >= sourceBlockSymbols) return p1
    }
    error("No P1 found")
}
