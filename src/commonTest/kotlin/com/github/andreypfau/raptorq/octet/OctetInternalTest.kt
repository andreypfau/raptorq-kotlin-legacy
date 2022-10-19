@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.octet

import kotlin.random.Random
import kotlin.random.nextULong
import kotlin.test.Test
import kotlin.test.assertContentEquals

class OctetInternalTest {
    @Test
    fun mulAssign() {
        val size = 41
        val scalar = Octet(Random.nextInt(1, 255).toUByte())
        val data1 = Random.nextBytes(size)
        val expected = ByteArray(size) {
            (Octet(data1[it].toUByte()) * scalar).value.toByte()
        }
        mulAssignScalar(data1, scalar)
        assertContentEquals(expected, data1)
    }

    @Test
    fun fma() {
        val size = 41
        val scalar = Octet(Random.nextInt(2, 255).toUByte())
        val data1 = Random.nextBytes(size)
        val data2 = Random.nextBytes(size)
        val expected = ByteArray(size) {
            (Octet(data1[it].toUByte()) + Octet(data2[it].toUByte()) * scalar).value.toByte()
        }
        fusedAddAssignMulScalar(data1, data2, scalar)
        assertContentEquals(expected, data1)
    }

    @Test
    fun fmaBinary() {
        val size = 41
        val scalar = Octet(Random.nextInt(2, 255).toUByte())
        val binaryVec = ULongArray((size + 63) / 64) {
            Random.nextULong()
        }
        val binaryOctetVec = BinaryOctetVec(binaryVec, size)
        val data1 = Random.nextBytes(size)
        val data2 = binaryOctetVec.toOctetVec()
        val expected = ByteArray(size) {
            (Octet(data1[it].toUByte()) + Octet(data2[it].toUByte()) * scalar).value.toByte()
        }
        fusedAddAssignMulScalarBinary(data1, binaryOctetVec, scalar)
        assertContentEquals(expected, data1)
    }
}
