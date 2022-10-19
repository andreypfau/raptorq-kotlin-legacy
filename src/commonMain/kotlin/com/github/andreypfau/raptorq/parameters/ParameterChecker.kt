/*
 * Copyright 2014 OpenRQ Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.andreypfau.raptorq.parameters

import com.github.andreypfau.raptorq.parameters.FECParameters.Companion.getTotalSymbols
import com.github.andreypfau.raptorq.util.math.ceilDiv
import kotlin.math.max
import kotlin.math.min

/**
 * This class provides methods for checking bounds and validating FEC and encoding packet parameters.
 *
 *
 * <a name="fec-parameters-bounds">
</a> * <h5>FEC parameters bounds</h5>
 *
 *
 * FEC parameters have well defined bounds, and any parameter that is outside its bounds is considered invalid. It is
 * important to know these bounds in order to be able to create instances of the class [FECParameters], which
 * contain only valid values.
 *
 *
 * By default, every FEC parameter has a minimum and a maximum value. For example, the method
 * [.maxNumSourceBlocks] returns the theoretical upper bound on the number of source blocks. However, these
 * minimum and maximum values are not sufficient to limit the possible (valid) parameter values. When multiple
 * parameters are defined, they are also constrained in a way that is conditioned on the combination of the used values.
 * For example, if a very large source data length is defined, then a relatively large symbol size must be defined as
 * well, because there is a limit on the number of symbols into which a source data can be divided.
 *
 *
 * Methods are provided for obtaining these conditional bounds. They are summarized below for ease of access.
 *
 *
 * Given a **source data length** it is possible to obtain the **minimum allowed symbol size**; conversely, given
 * a **symbol size** it is possible to obtain the **maximum allowed source data length**. Refer to methods for
 * each:
 *
 *
 *
 *  * [.minAllowedSymbolSize]  * [.maxAllowedDataLength]
 *
 *
 * Given a **source data length** and a **symbol size**, which are valid in respect to each other, it is possible
 * to obtain a **minimum and a maximum allowed number of source blocks**. Refer to methods for each:
 *
 *  * [.minAllowedNumSourceBlocks]  * [.maxAllowedNumSourceBlocks]
 *
 *
 * Given a **symbol size**, it is possible to obtain a **maximum allowed interleaver length**. Refer to the
 * method:
 *
 *  * [.maxAllowedInterleaverLength]
 *
 *
 * <a name="deriver-parameters-bounds">
</a> * <h5>Deriver parameters bounds</h5>
 *
 *
 * Parameters that derive FEC parameters, or "deriver parameters", include a *source data length*, a
 * *payload length*, and a *maximum size for a block decodable in working memory*. The payload length
 * parameter is equivalent to the "symbol size" FEC parameter.
 *
 *
 * Deriver parameters have well defined bounds, much like the FEC parameters. The source data length has the same bounds
 * as before, as well as the payload length since it is equivalent to the symbol size.
 *
 *
 * By default, the maximum decoding block size has a lower bound defined in method [.minDecodingBlockSize], but
 * has no upper bound.
 *
 *
 * There are also conditional bounds, much like in the FEC parameters. Methods are provided for obtaining these bounds.
 * They are summarized below for ease of access.
 *
 *
 * Given a **source data length** it is possible to obtain the **minimum allowed payload length**; conversely,
 * given a **payload length** it is possible to obtain the **maximum allowed source data length**. Refer to
 * methods for each:
 *
 *
 *
 *  * [.minAllowedPayloadLength]  * [.maxAllowedDataLength]
 *
 *
 * Given a **source data length** and a **payload length**, which are valid in respect to each other, it is
 * possible to obtain a **lower bound for the maximum decoding block size**. Refer to the method:
 *
 *  * [.minAllowedDecodingBlockSize]
 *
 *
 * Given a **payload length**, within bounds, and a **maximum decoding block size**, within bounds and no less
 * than the payload length, it is possible to obtain a **maximum allowed source data length**. Refer to the method:
 *
 *  * [.maxAllowedDataLength]
 */
object ParameterChecker {
    /**
     * Returns the minimum source data length, in number of bytes (1).
     *
     * @return the minimum source data length, in number of bytes
     */
    fun minDataLength(): Long = F_min

    /**
     * Returns the maximum source data length, in number of bytes (946_270_874_880L).
     *
     * @return the maximum source data length, in number of bytes
     */
    fun maxDataLength(): Long = F_max

    /**
     * Returns `false` iff [minDataLen][.minDataLength]  `dataLen`
     * [maxDataLen][.maxDataLength].
     *
     * @param dataLen A source data length, in number of bytes
     * @return `false` iff [minDataLen][.minDataLength]  `dataLen`
     * [maxDataLen][.maxDataLength]
     */
    fun isDataLengthOutOfBounds(dataLen: Long): Boolean =
        !(minDataLength() <= dataLen && dataLen <= maxDataLength())

    // =========== T -> "symbol size, in octets" ========== //
    /**
     * Returns the maximum allowed data length given a symbol size.
     *
     *
     * The provided parameter may also be a payload length, since it is equivalent to the symbol size, therefore the
     * same bounds apply.
     *
     * @param symbSize A symbol size, in number of bytes
     * @return the maximum allowed data length given a symbol size
     * @throws IllegalArgumentException If the symbol size is [out of bounds][.isSymbolSizeOutOfBounds]
     */
    fun maxAllowedDataLength(symbSize: Int): Long {
        _checkSymbolSizeOutOfBounds(symbSize)
        return _maxAllowedDataLength(symbSize)
    }

    /**
     * Returns the minimum symbol size, in number of bytes (1).
     *
     * @return the minimum symbol size, in number of bytes
     */
    fun minSymbolSize(): Int = T_min

    /**
     * Returns the maximum symbol size, in number of bytes (65535).
     *
     * @return the maximum symbol size, in number of bytes
     */
    fun maxSymbolSize(): Int = T_max

    /**
     * Returns `false` iff [minSymbSize][.minSymbolSize]  `symbSize`
     * [maxSymbSize][.maxSymbolSize].
     *
     * @param symbSize A symbol size, in number of bytes
     * @return `false` iff [minSymbSize][.minSymbolSize]  `symbSize`
     * [maxSymbSize][.maxSymbolSize]
     */
    fun isSymbolSizeOutOfBounds(symbSize: Int): Boolean = !(minSymbolSize() <= symbSize && symbSize <= maxSymbolSize())

    // =========== Z -> "number of source blocks" ========== //
    /**
     * Returns the minimum allowed symbol size given a source data length.
     *
     * @param dataLen A source data length, in number of bytes
     * @return the minimum allowed symbol size given a source data length.
     * @throws IllegalArgumentException If the source data length is [out of bounds][.isDataLengthOutOfBounds]
     */
    fun minAllowedSymbolSize(dataLen: Long): Int {
        _checkDataLengthOutOfBounds(dataLen)
        return _minAllowedSymbolSize(dataLen)
    }

    /**
     * Returns the minimum number of source blocks into which a source data is divided (1).
     *
     * @return the minimum number of source blocks into which a source data is divided
     */
    fun minNumSourceBlocks(): Int = Z_min

    /**
     * Returns the maximum number of source blocks into which a source data is divided (256).
     *
     * @return the maximum number of source blocks into which a source data is divided
     */
    fun maxNumSourceBlocks(): Int = Z_max

    /**
     * Returns `false` iff [minSrcBs][.minNumSourceBlocks]  `numSrcBs`
     * [maxSrcBs][.maxNumSourceBlocks].
     *
     * @param numSrcBs A number of source blocks into which a source data is divided
     * @return `false` iff [minSrcBs][.minNumSourceBlocks]  `numSrcBs`
     * [maxSrcBs][.maxNumSourceBlocks]
     */
    fun isNumSourceBlocksOutOfBounds(numSrcBs: Int): Boolean =
        !(minNumSourceBlocks() <= numSrcBs && numSrcBs <= maxNumSourceBlocks())


    /**
     * Returns the minimum allowed number of source blocks given a source data length and symbol size.
     *
     *
     * *Note that besides being within their own bounds, both arguments must also be valid together, that is, either
     * the source data length must be [upper bounded given the symbol size][.maxAllowedDataLength], or
     * the symbol size must be [lower bounded given the source data length][.minAllowedSymbolSize].*
     *
     * @param dataLen  A source data length, in number of bytes
     * @param symbSize A symbol size, in number of bytes
     * @return the minimum allowed number of source blocks given a source data length and symbol size
     * @throws IllegalArgumentException If either argument is individually out of bounds, or if they are out of bounds in unison
     */
    fun minAllowedNumSourceBlocks(dataLen: Long, symbSize: Int): Int {
        _checkDataLengthOutOfBounds(dataLen)
        _checkSymbolSizeOutOfBounds(symbSize)
        _checkDataLengthAndSymbolSizeOutOfBounds(dataLen, symbSize)
        val Kt: Int = getTotalSymbols(dataLen, symbSize)
        return _minAllowedNumSourceBlocks(Kt)
    }
    // =========== N -> "interleaver length, in number of sub-blocks" ========== //
    /**
     * Returns the maximum allowed number of source blocks given a source data length and symbol size.
     *
     *
     * *Note that besides being within their own bounds, both arguments must also be valid together, that is, either
     * the source data length must be [upper bounded given the symbol size][.maxAllowedDataLength], or
     * the symbol size must be [lower bounded given the source data length][.minAllowedSymbolSize].*
     *
     * @param dataLen  A source data length, in number of bytes
     * @param symbSize A symbol size, in number of bytes
     * @return the maximum allowed number of source blocks given a source data length and symbol size
     * @throws IllegalArgumentException If either argument is individually out of bounds, or if they are out of bounds in unison
     */
    fun maxAllowedNumSourceBlocks(dataLen: Long, symbSize: Int): Int {
        _checkDataLengthOutOfBounds(dataLen)
        _checkSymbolSizeOutOfBounds(symbSize)
        _checkDataLengthAndSymbolSizeOutOfBounds(dataLen, symbSize)
        val Kt: Int = getTotalSymbols(dataLen, symbSize)
        return _maxAllowedNumSourceBlocks(Kt)
    }

    /**
     * Returns the minimum interleaver length, in number of sub-blocks per source block (1).
     *
     * @return the interleaver length, in number of sub-blocks per source block
     */
    fun minInterleaverLength(): Int = N_min

    /**
     * Returns the maximum interleaver length, in number of sub-blocks per source block (1).
     *
     *
     * **Note:** *For now, interleaving is disabled.*
     *
     * @return the maximum interleaver length, in number of sub-blocks per source block
     */
    fun maxInterleaverLength(): Int = N_max

    /**
     * Returns `false` iff [minInterLen][.minInterleaverLength]  `interLen`
     * [maxInterLen][.maxInterleaverLength].
     *
     * @param interLen An interleaver length, in number of sub-blocks per source block
     * @return `false` iff [minInterLen][.minInterleaverLength]  `interLen`
     * [maxInterLen][.maxInterleaverLength]
     */
    fun isInterleaverLengthOutOfBounds(interLen: Int): Boolean =
        !(minInterleaverLength() <= interLen && interLen <= maxInterleaverLength())

    // =========== Al -> "symbol alignment parameter" ========== //
    /**
     * Returns the maximum allowed interleaver length given a symbol size.
     *
     * @param symbSize A symbol size, in number of bytes
     * @return the maximum allowed interleaver length given a symbol size
     * @throws IllegalArgumentException If the symbol size is [out of bounds][.isSymbolSizeOutOfBounds]
     */
    fun maxAllowedInterleaverLength(symbSize: Int): Int {
        _checkSymbolSizeOutOfBounds(symbSize)
        return _maxAllowedInterleaverLength(symbSize)
    }
    // =========== F, T, Z, N =========== //
    /**
     * Returns the symbol alignment parameter (1).
     *
     *
     * **Note:** *This value is fixed in this implementation of RaptorQ.*
     *
     * @return the symbol alignment parameter
     */
    fun symbolAlignmentValue(): Int = Al

    /**
     * Returns `true` if, and only if, the provided FEC parameters are valid, that is, if they fall within certain
     * bounds.
     *
     *
     * This method shouldn't be called directly. It is mainly used to test the validity of parameters when creating
     * [FECParameters] instances. If this method returns `false` then an exception is thrown by the creator
     * method.
     *
     *
     * It is possible, a priori, to obtain lower and upper bounds for valid parameter values. If the parameters fall
     * within these bounds, then this method always returns `true`. For information on how to obtain these bounds,
     * refer to the section on [*FEC parameters bounds*](#fec-parameters-bounds) in the class header.
     *
     * @param dataLen  A source data length, in number of bytes
     * @param symbSize A symbol size, in number of bytes
     * @param numSrcBs A number of source blocks into which a source data is divided
     * @param interLen An interleaver length, in number of sub-blocks per source block
     * @return `true` if, and only if, the provided FEC parameters are within certain bounds
     */
    fun areValidFECParameters(dataLen: Long, symbSize: Int, numSrcBs: Int, interLen: Int): Boolean =
        getFECParamsErrorString(
            dataLen,
            symbSize,
            numSrcBs,
            interLen
        ) == null // empty string means parameters are all valid

    // =========== F, P', WS =========== //
    /**
     * Tests if the FEC parameters are valid according to [.areValidFECParameters], and if so
     * the method returns an empty string, otherwise it returns an error string indicating which parameters are invalid.
     *
     * @param dataLen  A source data length, in number of bytes
     * @param symbSize A symbol size, in number of bytes
     * @param numSrcBs A number of source blocks into which a source data is divided
     * @param interLen An interleaver length, in number of sub-blocks per source block
     * @return an error string if some parameter is invalid or an empty string if all parameters are valid
     */
    fun getFECParamsErrorString(dataLen: Long, symbSize: Int, numSrcBs: Int, interLen: Int): String? {

        // domain restrictions
        if (isDataLengthOutOfBounds(dataLen)) {
            return "by default, the data length ($dataLen) must be within [$F_min, $F_max] bytes"
        }
        if (isSymbolSizeOutOfBounds(symbSize)) {
            return "by default, the symbol size ($symbSize) must be within [$T_min, $T_max] bytes"
        }
        if (isNumSourceBlocksOutOfBounds(numSrcBs)) {
            return "by default, the number of source blocks ($numSrcBs) must be within [$Z_min, $Z_max]"
        }
        if (isInterleaverLengthOutOfBounds(interLen)) {
            return "by default, the interleaver length ($interLen) must be within [$N_min, $N_max]"
        }

        // T must be a multiple of Al
        // if (T % Al != 0) {
        // return String.format(
        // "the symbol size (%d) must be a multiple of the symbol alignment value %d",
        // T, Al);
        // }

        // the number of symbols cannot exceed Kt_max
        if (_areDataLengthAndSymbolSizeOutOfBounds(
                dataLen,
                symbSize
            )
        ) {
            return "$dataLen byte(s) of data length only support symbol size values of at least ${
                _minAllowedSymbolSize(
                    dataLen
                )
            } byte(s); " +
                "alternatively, $symbSize bytes(s) of symbol size only support data length values of at most ${
                    _maxAllowedDataLength(
                        symbSize
                    )
                } byte(s)"
        }

        // number of symbols
        val Kt: Int = getTotalSymbols(dataLen, symbSize)
        val minAllowedZ = _minAllowedNumSourceBlocks(Kt)
        val maxAllowedZ = _maxAllowedNumSourceBlocks(Kt)

        // at least one symbol, and at most K_max symbols in each source block
        if (numSrcBs < minAllowedZ || numSrcBs > maxAllowedZ) {
            return "$dataLen byte(s) of data length and $symbSize byte(s) of symbol size only support " +
                "a number of source blocks ($numSrcBs) within [$minAllowedZ, $maxAllowedZ]"
        }
        val maxAllowedN = _maxAllowedInterleaverLength(
            symbSize
        )

        // interleaver length must be bounded as well
        return if (interLen > maxAllowedN) {
            "$symbSize byte(s) of symbol size only supports an interleaver length ($interLen) of at most $maxAllowedN"
        } else null

        // empty string means parameters are all valid
    }

    /**
     * Returns the minimum payload length, in number of bytes. This value is equivalent to the
     * [minimum symbol size][.minSymbolSize].
     *
     * @return the minimum payload length, in number of bytes
     */
    fun minPayloadLength(): Int = minSymbolSize()

    /**
     * Returns the maximum payload length, in number of bytes. This value is equivalent to the
     * [maximum symbol size][.maxSymbolSize].
     *
     * @return the maximum payload length, in number of bytes
     */
    fun maxPayloadLength(): Int = maxSymbolSize()

    /**
     * Returns `false` iff [minPayLen][.minPayloadLength]  `payLen`
     * [maxPayLen][.maxPayloadLength].
     *
     * @param payLen A payload length, in number of bytes (equivalent to the "symbol size" FEC parameter)
     * @return `false` iff [minPayLen][.minPayloadLength]  `payLen`
     * [maxPayLen][.maxPayloadLength]
     */
    fun isPayloadLengthOutOfBounds(payLen: Int): Boolean =
        !(minPayloadLength() <= payLen && payLen <= maxPayloadLength())

    /**
     * Returns the minimum allowed payload length given a source data length. This method is equivalent to method
     * [.minAllowedSymbolSize].
     *
     * @param dataLen A source data length, in number of bytes
     * @return the minimum allowed payload length given a source data length.
     * @throws IllegalArgumentException If the source data length is [out of bounds][.isDataLengthOutOfBounds]
     */
    fun minAllowedPayloadLength(dataLen: Long): Int =
        minAllowedSymbolSize(dataLen)

    /**
     * Returns the lowest possible bound on the maximum size for a block decodable in working memory.
     *
     * @return the lowest possible bound on the maximum size for a block decodable in working memory
     */
    fun minDecodingBlockSize(): Long =
        _minAllowedDecodingBlockSize(minDataLength(), minSymbolSize())

    /**
     * Returns a lower bound on the maximum size for a block decodable in working memory, given a source data length and
     * payload length.
     *
     *
     * *Note that besides being within their own bounds, both arguments must also be valid together, that is, either
     * the source data length must be [upper bounded given the payload length][.maxAllowedDataLength], or
     * the payload length must be [lower bounded given the source data length][.minAllowedPayloadLength]
     * .*
     *
     * @param dataLen A source data length, in number of bytes
     * @param payLen  A payload length, in number of bytes (equivalent to the "symbol size" FEC parameter)
     * @return a lower bound on the maximum size for a block decodable in working memory
     * @throws IllegalArgumentException If either argument is individually out of bounds, or if they are out of bounds in unison
     */
    fun minAllowedDecodingBlockSize(dataLen: Long, payLen: Int): Long {
        _checkDataLengthOutOfBounds(dataLen)
        _checkPayloadLengthOutOfBounds(payLen)
        _checkDataLengthAndPayloadLengthOutOfBounds(dataLen, payLen)
        return _minAllowedDecodingBlockSize(dataLen, payLen)
    }

    /**
     * Returns the maximum allowed data length given a payload length and a maximum size for a block decodable in
     * working memory.
     *
     *
     * ***Bounds checking*** - The following must be true, otherwise an `IllegalArgumentException` is
     * thrown:
     *
     *  * [isPayloadLengthOutOfBounds(payLen)][.isPayloadLengthOutOfBounds] `== false`  *
     * `maxDBMem >=` [.minDecodingBlockSize]  * `maxDBMem >= payLen`
     *
     * @param payLen   A payload length, in number of bytes (equivalent to the "symbol size" FEC parameter)
     * @param maxDBMem A maximum size, in number of bytes, for a block decodable in working memory
     * @return the maximum allowed data length given a payload length and a maximum size for a block decodable in
     * working memory
     * @throws IllegalArgumentException If any argument is out of bounds (see method description)
     */
    fun maxAllowedDataLength(payLen: Int, maxDBMem: Long): Long {
        _checkPayloadLengthOutOfBounds(payLen)
        _checkDecodingBlockSizeOutOfBounds(maxDBMem)
        require(maxDBMem >= payLen) { "maximum decoding block size must be at least equal to the payload length" }
        return _maxAllowedDataLength(payLen, maxDBMem)
    }

    /**
     * Returns `true` if, and only if, the provided deriver parameters are valid, that is, if they fall within
     * certain bounds.
     *
     *
     * This method shouldn't be called directly. It is mainly used to test the validity of parameters when deriving
     * [FECParameters] instances. If this method returns `false` then an exception is thrown by the deriver
     * method.
     *
     *
     * It is possible, a priori, to obtain lower and upper bounds for valid parameter values. If the parameters fall
     * within these bounds, then this method always returns `true`. For information on how to obtain these bounds,
     * refer to the section on [*Deriver parameters bounds*](#deriver-parameters-bounds) in the class
     * header.
     *
     * @param dataLen  A source data length, in number of bytes
     * @param payLen   A payload length, in number of bytes (equivalent to the "symbol size" FEC parameter)
     * @param maxDBMem A maximum size, in number of bytes, for a block decodable in working memory
     * @return `true` if, and only if, the provided deriver parameters are within certain bounds
     */
    fun areValidDeriverParameters(dataLen: Long, payLen: Int, maxDBMem: Long): Boolean =
        getDeriverParamsErrorString(dataLen, payLen, maxDBMem) == null

    // =========== source block number - SBN ========== //
    /**
     * Tests if the deriver parameters are valid according to [.areValidDeriverParameters], and
     * if so the method returns an empty string, otherwise it returns an error string indicating which parameters are
     * invalid.
     *
     * @param dataLen  A source data length, in number of bytes
     * @param payLen   A payload length, in number of bytes (equivalent to the "symbol size" FEC parameter)
     * @param maxDBMem A maximum size, in number of bytes, for a block decodable in working memory
     * @return an error string if some parameter is invalid or an empty string if all parameters are valid
     */
    fun getDeriverParamsErrorString(dataLen: Long, payLen: Int, maxDBMem: Long): String? {
        // domain restrictions
        if (isDataLengthOutOfBounds(dataLen)) {
            return "by default, the data length ($dataLen) must be within [$F_min, $F_max] bytes"
        }
        if (isSymbolSizeOutOfBounds(payLen)) {
            return "$T_min default, the payload length ($T_max) must be within [%d, %d] bytes"
        }

        // T must be a multiple of Al
        // if (T % Al != 0) {
        // return String.format(
        // "the symbol size (%d) must be a multiple of the symbol alignment value %d",
        // T, Al);
        // }
        val absolMinWS = minDecodingBlockSize()
        if (maxDBMem < absolMinWS) {
            return "by default, the max decoding block size ($maxDBMem) must be at least $absolMinWS byte(s)"
        }
        val minT = _minAllowedSymbolSize(dataLen)
        if (payLen < minT) {
            return "$dataLen byte(s) of data length only supports a payload length ($payLen) of at least $minT byte(s)"
        }
        val minWS = _minAllowedDecodingBlockSize(
            dataLen,
            payLen
        )
        return if (maxDBMem < minWS) {
            "$dataLen byte(s) of data length and $payLen byte(s) of symbol size only support " +
                "a max decoding block size ($maxDBMem) of at least $minWS byte(s)"
        } else null
    }

    /**
     * Returns the minimum source block number (0).
     *
     * @return the minimum source block number
     */
    fun minSourceBlockNumber(): Int = SBN_min

    /**
     * Returns the maximum source block number (255).
     *
     * @return the maximum source block number
     */
    fun maxSourceBlockNumber(): Int = SBN_max

    // =========== encoding symbol identifier - ESI ========== //
    /**
     * Returns `false` iff [minSBN][.minSourceBlockNumber]  `sbn`
     * [maxSBN][.maxSourceBlockNumber].
     *
     * @param sbn A source block number
     * @return `false` iff [minSBN][.minSourceBlockNumber]  `sbn`
     * [maxSBN][.maxSourceBlockNumber]
     */
    fun isSourceBlockNumberOutOfBounds(sbn: Int): Boolean =
        !(minSourceBlockNumber() <= sbn && sbn <= maxSourceBlockNumber())

    /**
     * Returns the minimum encoding symbol identifier (0).
     *
     * @return the minimum encoding symbol identifier
     */
    fun minEncodingSymbolID(): Int = ESI_min

    /**
     * Returns the maximum encoding symbol identifier (16_777_215).
     *
     * @return the maximum encoding symbol identifier
     */
    fun maxEncodingSymbolID(): Int = ESI_max
    // =========== SBN, ESI =========== //
    /**
     * Returns `false` iff [minESI][.minEncodingSymbolID]  `esi`
     * [maxESI][.maxEncodingSymbolID].
     *
     * @param esi An encoding symbol ID
     * @return `false` iff [minESI][.minEncodingSymbolID]  `esi`
     * [maxESI][.maxEncodingSymbolID]
     */
    fun isEncodingSymbolIDOutOfBounds(esi: Int): Boolean =
        !(minEncodingSymbolID() <= esi && esi <= maxEncodingSymbolID())

    /**
     * Returns `true` if, and only if, the FEC payload ID parameters are valid, that is, if they fall within
     * certain bounds.
     *
     *
     * The source block number (SBN) must be greater than or equal to its minimum value, given by the method
     * [.minSourceBlockNumber]. The SBN must also be less than the number of source blocks.
     *
     *
     * The encoding symbol identifier (ESI) must not be out of bounds, meaning that the following method should return
     * `false` when passing the ESI as parameter: [.isEncodingSymbolIDOutOfBounds].
     *
     * @param sbn      A source block number
     * @param esi      The encoding symbol identifier of the first symbol in an encoding packet
     * @param numSrcBs A number of source blocks into which a source data is divided
     * @return `true` if, and only if, the provided FEC payload ID parameters are within certain bounds
     * @throws IllegalArgumentException If the number of source blocks is [out of bounds][.isNumSourceBlocksOutOfBounds]
     */
    fun isValidFECPayloadID(sbn: Int, esi: Int, numSrcBs: Int): Boolean =
        getFECPayloadIDErrorString(sbn, esi, numSrcBs) == null

    // =========== number of source symbols - K =========== //
    /**
     * Tests if the FEC Payload ID parameters are valid according to [.isValidFECPayloadID], and if
     * so the method returns an empty string, otherwise it returns an error string indicating which parameters are
     * invalid.
     *
     * @param sbn    A source block number
     * @param esi    The encoding symbol identifier of the first symbol in an encoding packet
     * @param numSBs A number of source blocks into which a source data is divided
     * @return an error string if some parameter is invalid or an empty string if all parameters are valid
     * @throws IllegalArgumentException If the number of source blocks is [out of bounds][.isNumSourceBlocksOutOfBounds]
     */
    fun getFECPayloadIDErrorString(sbn: Int, esi: Int, numSBs: Int): String? {
        _checkNumSourceBlocksOutOfBounds(numSBs)
        if (sbn < SBN_min || sbn >= numSBs) {
            return "source block number ($sbn) must be within [$SBN_min, ${numSBs - 1}]"
        }
        return if (isEncodingSymbolIDOutOfBounds(esi)) {
            "encoding symbol identifier ($esi) must be within [$ESI_min, $ESI_max]"
        } else null
    }

    /**
     * Returns the minimum number of source symbols in a source block (1).
     *
     * @return the minimum number of source symbols in a source block
     */
    fun minNumSourceSymbolsPerBlock(): Int = K_min

    /**
     * Returns the maximum number of source symbols in a source block (56_403).
     *
     * @return the maximum number of source symbols in a source block
     */
    fun maxNumSourceSymbolsPerBlock(): Int = K_max

    /**
     * Returns `false` iff [minNumSrcSymbs][.minNumSourceSymbolsPerBlock]  `numSrcSymbs`
     *  [maxNumSrcSymbs][.maxNumSourceSymbolsPerBlock].
     *
     * @param numSrcSymbs The number of source symbols in a source block
     * @return `false` iff [minNumSrcSymbs][.minNumSourceSymbolsPerBlock]  `numSrcSymbs`
     *  [maxNumSrcSymbs][.maxNumSourceSymbolsPerBlock]
     */
    fun isNumSourceSymbolsPerBlockOutOfBounds(numSrcSymbs: Int): Boolean =
        !(minNumSourceSymbolsPerBlock() <= numSrcSymbs && numSrcSymbs <= maxNumSourceSymbolsPerBlock())

    /**
     * Returns the total number of possible repair symbols in a source block, given the number of source symbols in the
     * block, starting at the initial repair symbol identifier.
     *
     * @param numSrcSymbs The number of source symbols in a source block
     * @return the total number of possible repair symbols in a source block, given the number of source symbols in the
     * block, starting at the initial repair symbol identifier
     * @throws IllegalArgumentException If the number of source symbols is [out of][.isNumSourceSymbolsPerBlockOutOfBounds]
     */
    fun numRepairSymbolsPerBlock(numSrcSymbs: Int): Int =
        numRepairSymbolsPerBlock(numSrcSymbs, numSrcSymbs)

    // =========== private helper methods =========== //
    /**
     * Returns the total number of possible repair symbols in a source block, given the number of source symbols in the
     * block and the encoding symbol identifier of the first repair symbol.
     *
     * @param numSrcSymbs The number of source symbols in a source block
     * @param firstESI    The first repair symbol identifier
     * @return the total number of possible repair symbols in a source block, given the number of source symbols in the
     * block, starting at a specified repair symbol identifier
     * @throws IllegalArgumentException If the number of source symbols is [out of][.isNumSourceSymbolsPerBlockOutOfBounds], or if the first repair symbol identifier is
     * [out of bounds][.isEncodingSymbolIDOutOfBounds] or is less than the number of
     * source symbols
     */
    fun numRepairSymbolsPerBlock(numSrcSymbs: Int, firstESI: Int): Int {
        _checkNumSourceSymbolsPerBlockOutOfBounds(numSrcSymbs)
        val maxESI = maxEncodingSymbolID()
        if (firstESI < numSrcSymbs || firstESI > maxESI) {
            throw IllegalArgumentException(
                "first repair symbol identifier must be within [$numSrcSymbs, $maxESI]"
            )
        }
        val totalSymbs = 1 + maxESI - firstESI
        return totalSymbs - numSrcSymbs
    }

    // requires bounded argument
    private inline fun _maxAllowedDataLength(T: Int): Long =
        min(F_max, T.toLong() * Kt_max)

    // requires bounded argument
    private inline fun _minAllowedSymbolSize(F: Long): Int = max(
        T_min,
        ceilDiv(F, Kt_max.toLong()).toInt()
    ) // downcast never overflows since dataLen is upper bounded

    // requires bounded argument
    private inline fun _minAllowedNumSourceBlocks(Kt: Int): Int = max(Z_min, ceilDiv(Kt, K_max))

    // requires bounded argument
    private inline fun _maxAllowedNumSourceBlocks(Kt: Int): Int = min(Z_max, Kt)

    // requires individually bounded arguments
    private inline fun _maxAllowedInterleaverLength(T: Int): Int = min(N_max, T / Al)

    // requires individually and in unison bounded arguments
    private fun _minAllowedDecodingBlockSize(F: Long, T: Int): Long {
        // total number of symbols
        val Kt: Int = getTotalSymbols(F, T)

        // the theoretical minimum number of source symbols in an extended source block
        val Kprime: Int = max(K_prime_min, ceilDiv(Kt, Z_max))

        // minimum WS is the inverse of the function KL
        return minWS(Kprime, T, Al, topInterleaverLength(T))
    }

    // requires individually and in unison bounded arguments
    private fun _maxAllowedDataLength(T: Int, WS: Long): Long {
        val boundFromT = _maxAllowedDataLength(T)

        // Kt = ceil(F / T)
        // Z = ceil(Kt / KL)
        // ceil(Kt / KL) <= Z_max
        // Kt / KL <= Z_max
        // Kt <= Z_max * KL
        // ceil(F / T) <= Z_max * KL
        // F / T <= Z_max * KL
        // F <= Z_max * KL * T
        val KL = KL(WS, T, Al, topInterleaverLength(T)).toLong()
        val boundFromWS = Z_max.toLong() * KL * T
        return min(boundFromT, boundFromWS)
    }

    // requires individually bounded arguments
    private inline fun _areDataLengthAndSymbolSizeOutOfBounds(F: Long, T: Int): Boolean =
        getPossibleTotalSymbols(F, T) > Kt_max

    // requires individually bounded arguments
    private inline fun _areDataLengthAndPayloadLengthOutOfBounds(F: Long, P: Int): Boolean =
        _areDataLengthAndSymbolSizeOutOfBounds(F, P)


    private inline fun _checkDataLengthOutOfBounds(F: Long) =
        require(!isDataLengthOutOfBounds(F)) { "source data length is out of bounds" }


    private inline fun _checkSymbolSizeOutOfBounds(T: Int) =
        require(!isSymbolSizeOutOfBounds(T)) { "symbol size is out of bounds" }

    private inline fun _checkDataLengthAndSymbolSizeOutOfBounds(F: Long, T: Int) {
        require(!_areDataLengthAndSymbolSizeOutOfBounds(F, T)) {
            "source data length and symbol size are out of bounds in unison"
        }
    }

    private inline fun _checkNumSourceBlocksOutOfBounds(Z: Int) =
        require(!isNumSourceBlocksOutOfBounds(Z)) { "number of source blocks is out of bounds" }

    private inline fun _checkPayloadLengthOutOfBounds(P: Int) =
        require(!isPayloadLengthOutOfBounds(P)) { "payload length is out of bounds" }

    private inline fun _checkDataLengthAndPayloadLengthOutOfBounds(F: Long, P: Int) =
        require(!_areDataLengthAndPayloadLengthOutOfBounds(F, P)) {
            "source data length and payload length are out of bounds in unison"
        }

    private inline fun _checkDecodingBlockSizeOutOfBounds(WS: Long) =
        require(WS >= minDecodingBlockSize()) { "maximum decoding block size is out of bounds" }

    private inline fun _checkNumSourceSymbolsPerBlockOutOfBounds(K: Int) =
        require(!isNumSourceSymbolsPerBlockOutOfBounds(K)) { "number of source symbols per block is out of bounds" }

}
