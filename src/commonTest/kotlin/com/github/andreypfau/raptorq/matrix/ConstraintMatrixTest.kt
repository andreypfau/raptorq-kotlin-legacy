package com.github.andreypfau.raptorq.matrix

import com.github.andreypfau.raptorq.core.RaptorQ.rand
import com.github.andreypfau.raptorq.core.extendedSourceBlockSymbols
import com.github.andreypfau.raptorq.core.generateHdpcRows
import com.github.andreypfau.raptorq.core.numHdpcSymbols
import com.github.andreypfau.raptorq.core.numLdpcSymbols
import com.github.andreypfau.raptorq.octet.Octet
import com.github.andreypfau.raptorq.octet.addAssign
import com.github.andreypfau.raptorq.octet.asOctet
import com.github.andreypfau.raptorq.octet.fusedAddAssignMulScalar
import kotlin.random.Random
import kotlin.test.Test

class ConstraintMatrixTest {
    @Test
    fun fastHdpc() {
        val sourceBlockSymbols = Random.nextInt(1, 50000)
        val kPrime = extendedSourceBlockSymbols(sourceBlockSymbols)
        val S = numLdpcSymbols(sourceBlockSymbols)
        val H = numHdpcSymbols(sourceBlockSymbols)
        val expected = referenceGenerateHdpcRows(kPrime, S, H)
        val generated = generateHdpcRows(kPrime, S, H)
        assertMatricesEquals(expected, generated)
    }
}

fun referenceGenerateHdpcRows(kPrime: Int, S: Int, H: Int): DenseOctetMatrix {
    val matrix = DenseOctetMatrix(H, kPrime + S + H)

    // Generates the MT matrix
    // See section 5.3.3.3
    val mt = Array(H) { ByteArray(kPrime + S) }
    for (i in 0 until H) {
        for (j in 0..kPrime + S - 2) {
            val rand6 = rand((j + 1).toUInt(), 6u, H.toUInt()).toInt()
            val rand7 = rand((j + 1).toUInt(), 7u, (H - 1).toUInt()).toInt()
            if (i == rand6 || i == (rand6 + rand7 + 1) % H) {
                mt[i][j] = 1
            }
        }
        mt[i][kPrime + S - 1] = Octet.alpha(i).toByte()
    }

    // Multiply by the GAMMA matrix
    // See section 5.3.3.3
    val gammaRow = ByteArray(kPrime + S)
    for (j in 0 until kPrime + S) {
        // The spec says "alpha ^^ (i-j)". However, this clearly can overflow since alpha() is
        // only defined up to input < 256. Since alpha() only has 255 unique values, we must
        // take the input mod 255. Without this the constraint matrix ends up being singular
        // for 1698 and 8837 source symbols.
        gammaRow[j] = Octet.alpha((kPrime + S - 1 - j) % 255).toByte()
    }
    for (i in 0 until H) {
        val resultRow = ByteArray(kPrime + S)
        for (j in 0 until kPrime + S) {
            val scalar = mt[i][j].asOctet()
            if (scalar == Octet.ZERO) continue
            else if (scalar == Octet.ONE) {
                addAssign(
                    octets = resultRow,
                    octetsOffset = 0,
                    octetsLen = j + 1,
                    other = gammaRow,
                    otherOffset = kPrime + S - j - 1,
                    otherLen = (kPrime + S) - (kPrime + S - j - 1)
                )
            } else {
                fusedAddAssignMulScalar(
                    octets = resultRow,
                    octetsOffset = 0,
                    octetsLen = j + 1,
                    other = gammaRow,
                    otherOffset = kPrime + S - j - 1,
                    otherLen = (kPrime + S) - (kPrime + S - j - 1),
                    scalar = scalar
                )
            }
        }
        for (j in 0 until kPrime + S) {
            if (resultRow[j] != 0.toByte()) {
                matrix[i, j] = resultRow[j].asOctet()
            }
        }
    }
    // I_H
    for (i in 0 until H) {
        matrix[i, i + kPrime + S] = Octet.ONE
    }
    return matrix
}
