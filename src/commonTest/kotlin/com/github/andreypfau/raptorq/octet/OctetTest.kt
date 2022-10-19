@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.octet

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OctetTest {
    @Test
    fun addition() {
        repeat(256) {
            val octet = it.asOctet()
            val actual = octet + octet
            assertEquals(Octet.ZERO, actual)
        }
    }

    @Test
    fun multiplicationIdentity() {
        repeat(255) {
            val octet = it.asOctet()
            val actual = octet * Octet.ONE
            assertEquals(octet, actual)
        }
    }

    @Test
    fun multiplicationInverse() {
        for (i in 1..255) {
            val octet = i.asOctet()
            val actual = octet * (Octet.ONE / octet)
            assertEquals(Octet.ONE, actual)
        }
    }

    @Test
    fun division() {
        for (i in 1..255) {
            val octet = i.asOctet()
            val actual = octet / octet
            assertEquals(Octet.ONE, actual)
        }
    }

    @Test
    fun unsafeMulGuarantess() {
        val maxValue = OCTET_LOG.max().toUByte()
        assertTrue(2u * maxValue < OCTET_EXP.size.toUInt())
    }

    @Test
    fun fma() {
        var result = Octet.ZERO
        var fmaResult = Octet.ZERO
        for (i in 0 until 255) {
            for (j in 0 until 255) {
                result += i.asOctet() * j.asOctet()
                fmaResult = fmaResult.fma(i.asOctet(), j.asOctet())
                assertEquals(result, fmaResult)
            }
        }
    }
}
