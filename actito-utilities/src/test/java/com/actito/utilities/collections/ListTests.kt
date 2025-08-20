package com.actito.utilities.collections

import org.junit.Assert.assertEquals
import org.junit.Test

public class ListTests {
    @Test
    public fun testN0() {
        val result = emptyList<Int>().takeEvenlySpaced(0)
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    public fun testSizeLessThanOrEqualToN() {
        val list = listOf(1, 2, 3)
        assertEquals(list, list.takeEvenlySpaced(3))
        assertEquals(list, list.takeEvenlySpaced(5))
    }

    @Test(expected = IllegalArgumentException::class)
    public fun testNLessThanZero() {
        val list = listOf(1, 2, 3)
        list.takeEvenlySpaced(-1)
    }

    @Test
    public fun testN1() {
        val list = (1..10).toList()
        val result = list.takeEvenlySpaced(1)
        assertEquals(listOf(1), result)
    }

    @Test
    public fun testN2() {
        val list = (1..10).toList()
        val result = list.takeEvenlySpaced(2)
        assertEquals(listOf(1, 10), result)
    }

    @Test
    public fun testNSmallerThanListSize() {
        val list = (1..10).toList()
        val result = list.takeEvenlySpaced(5)
        assertEquals(listOf(1, 3, 5, 8, 10), result)
    }

    @Test
    public fun testNonNumericTypes() {
        val list = listOf("a", "b", "c", "d", "e", "f")
        val result = list.takeEvenlySpaced(3)
        assertEquals(listOf("a", "c", "f"), result)
    }
}
