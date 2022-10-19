package com.github.andreypfau.raptorq.symbol

import io.ktor.utils.io.bits.*

class RepairSymbol(
    private val data: ByteArray
) {
    fun <R> readOnlyData(memory: (Memory) -> R): R = data.useMemory(0, data.size, memory)
}
