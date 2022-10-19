package com.github.andreypfau.raptorq.encoder.array

import com.github.andreypfau.raptorq.Partition
import com.github.andreypfau.raptorq.encoder.SourceBlockEncoder
import com.github.andreypfau.raptorq.encoder.iterator.EncodingPacketIteratorBuilder
import com.github.andreypfau.raptorq.packet.EncodingPacket
import com.github.andreypfau.raptorq.packet.RepairPacket
import com.github.andreypfau.raptorq.packet.SourcePacket
import com.github.andreypfau.raptorq.parameters.FECParameters
import com.github.andreypfau.raptorq.parameters.ParameterChecker
import com.github.andreypfau.raptorq.symbol.ArraySourceSymbol
import com.github.andreypfau.raptorq.symbol.RepairSymbol
import com.github.andreypfau.raptorq.symbol.SourceSymbol
import com.github.andreypfau.raptorq.util.ISDManager
import com.github.andreypfau.raptorq.util.LinearSystem
import com.github.andreypfau.raptorq.util.SystematicIndices
import com.github.andreypfau.raptorq.util.Tuple
import io.ktor.utils.io.bits.*

class ArraySourceBlockEncoder(
    val dataEncoder: ArrayDataEncoder,
    val sbn: Int,
    val sourceSymbols: List<SourceSymbol>
) : SourceBlockEncoder {
    constructor(
        dataEncoder: ArrayDataEncoder,
        array: ByteArray,
        arrayOffset: Int,
        fecParameters: FECParameters,
        sbn: Int
    ) : this(dataEncoder, sbn, Partition.partitionSourceBlock(sbn, fecParameters, arrayOffset) { off, esi, T ->
        ArraySourceSymbol(array, off, T)
    })

    private val K
        get() = sourceSymbols.size

    override val numberOfSourceSymbols: Int
        get() = K

    private val fecParameters
        get() = dataEncoder.fecParameters

    private val kPrime = SystematicIndices.ceil(K)

    private val intermidiateSymbols: Array<ByteArray> by lazy {
        generateIntermediateSymbols()
    }

    override fun <R> encodingPacket(esi: Int, packet: (EncodingPacket) -> R): R {
        checkGenericEncodingSymbolESI(esi)
        return if (esi < K) {
            getSourceSymbol(esi).transportData { memory ->
                packet(SourcePacket(sbn, esi, memory, 1))
            }
        } else {
            getRepairSymbol(esi).readOnlyData { memory ->
                packet(RepairPacket(sbn, esi, memory, 1))
            }
        }
    }

    fun getSourceSymbol(esi: Int): SourceSymbol = sourceSymbols[esi]

    fun getRepairSymbol(esi: Int): RepairSymbol {
        val isi = SystematicIndices.getISI(esi, K, kPrime)
        val T = fecParameters.symbolSize
        val encData = LinearSystem.enc(kPrime, getIntermediateSymbols(), Tuple(kPrime, isi.toLong()), T)
        return RepairSymbol(encData)
    }

    override fun sourceIterator(): Iterator<EncodingPacket> =
        EncodingPacketIteratorBuilder(this).startAtInitialSourceSymbol().endAtFinalSourceSymbol().build()

    override fun repairIterator(numRepairPackets: Int): Iterator<EncodingPacket> {
        require(!(numRepairPackets < 1 || numRepairPackets > ParameterChecker.numRepairSymbolsPerBlock(K))) {
            "invalid number of repair packets"
        }
        return EncodingPacketIteratorBuilder(
            this,
            endingESI = numberOfSourceSymbols + numRepairPackets - 1
        ).startAtInitalRepairSymbol().build()
    }

    // use only this method for access to the intermediate symbols
    private fun generateIntermediateSymbols(): Array<ByteArray> {
        // initialize the vector D with source data
        val D = initVectorD()
        // first try to obtain an optimized decoder that supports Kprime
        val isd = ISDManager[kPrime]
        if (isd != null) {
            TODO()
        } else {
            // if no optimized decoder is available, fall back to the
            // standard decoding process
            val constraintMatrix = LinearSystem.generateConstraintMatrix(kPrime)
        }

        TODO()
    }

    private fun initVectorD(): Array<ByteArray> {
        // source block's parameters
        val Ki = SystematicIndices.getKIndex(kPrime)
        val S = SystematicIndices.S(Ki)
        val H = SystematicIndices.H(Ki)
        val L = kPrime + S + H
        val T = fecParameters.symbolSize

        // allocate and initialize vector D
        val D = Array(L) { ByteArray(T) }
        var row = S + H
        var esi = 0
        while (row < K + S + H) {
            getSourceSymbol(esi).codeData {
                it.loadByteArray(0, D[row], 0, T)
            }
            row++
            esi++
        }
        return D
    }

    private inline fun checkGenericEncodingSymbolESI(esi: Int) =
        require(!(esi < 0 || esi > ParameterChecker.maxEncodingSymbolID())) { "invalid encoding symbol ID" }

    private inline fun checkSourceSymbolESI(esi: Int) =
        require(!(esi < 0 || esi >= K)) { "invalid source symbol ID" }

    // requires valid ESI
    private inline fun checkNumSourceSymbols(esi: Int, numSymbols: Int) =
        require(!(numSymbols < 1 || numSymbols > K - esi)) { "invalid number of source symbols" }

    private inline fun checkRepairSymbolESI(esi: Int) =
        require(!(esi < K || esi > ParameterChecker.maxEncodingSymbolID())) { "invalid repair symbol ID" }

    // requires valid ESI
    private inline fun checkNumRepairSymbols(esi: Int, numSymbols: Int) = require(
        !(numSymbols < 1 || numSymbols > ParameterChecker.numRepairSymbolsPerBlock(K, esi))
    ) { "invalid number of repair symbols" }
}
