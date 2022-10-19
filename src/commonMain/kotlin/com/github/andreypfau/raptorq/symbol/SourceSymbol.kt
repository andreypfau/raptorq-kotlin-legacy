package com.github.andreypfau.raptorq.symbol

import io.ktor.utils.io.bits.*

interface SourceSymbol {

    val codeSize: Int

    fun <R> transportData(memory: (Memory) -> R): R
    fun <R> codeData(function: (Memory) -> R): R
}
