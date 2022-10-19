package com.github.andreypfau.raptorq.util.linear.vector

import com.github.andreypfau.raptorq.util.linear.factory.Factory

interface ByteVector : Iterable<Byte> {
    val length: Int

    operator fun get(i: Int): Byte

    operator fun set(i: Int, value: Byte)

    fun clear()

    fun assign(value: Byte)

    fun add(value: Byte): ByteVector

    fun add(value: Byte, factory: Factory): ByteVector

    fun addInPlace(value: Byte)

    fun addInPlace(i: Int, value: Byte)

    fun add(vector: ByteVector): ByteVector

    fun add(vector: ByteVector, factory: Factory): ByteVector

    fun addInPlace(vector: ByteVector)

    fun addInPlace(vector: ByteVector, fromIndex: Int, toIndex: Int)

    fun addInPlace(multiplier: Byte, vector: ByteVector)

    fun addInPlace(multiplier: Byte, vector: ByteVector, fromIndex: Int, toIndex: Int)

    fun multiply(value: Byte): ByteVector

    fun multiply(value: Byte, factory: Factory): ByteVector

    fun multiplyInPlace(value: Byte)

    fun hadamardProduct(vector: ByteVector): ByteVector
}
