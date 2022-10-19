@file:Suppress("OPT_IN_USAGE")

package com.github.andreypfau.raptorq.arraymap

import kotlin.test.Test
import kotlin.test.assertTrue

class ArrayMapTest {
    @Test
    fun listMap() {
        val builder = ImmutableListMapBuilder(10)
        builder.add(0u, 1u)
        builder.add(3u, 1u)
        builder.add(3u, 2u)

        val map = builder.build()
        assertTrue(map[0].contains(1u))
        assertTrue(!map[0].contains(2u))

        assertTrue(map[3].contains(1u))
        assertTrue(map[3].contains(2u))
        assertTrue(!map[3].contains(3u))

        assertTrue(!map[2].contains(1u))
    }
}
