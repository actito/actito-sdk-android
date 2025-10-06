package com.actito.utilities.collections

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class MapTests {
    @Test
    public fun testSuccessfulMapCast() {
        val map = mapOf<Any, Any>(
            "key1" to 1,
            "key2" to 2,
        )

        val result: Map<String, Int> = map.cast()

        val expectedMap = mapOf(
            "key1" to 1,
            "key2" to 2,
        )

        assertEquals(expectedMap, result)
    }

    @Test
    public fun testPartialUnsuccessfulMapCast() {
        val map = mapOf<Any, Any>(
            "key1" to 1,
            2 to "value2",
            "key3" to 3,
        )

        val result: Map<String, Int> = map.cast()

        val expectedMap = mapOf(
            "key1" to 1,
            "key3" to 3,
        )

        assertEquals(expectedMap, result)
    }

    @Test
    public fun testCompleteUnsuccessfulMapCast() {
        val map = mapOf<Any, Any>(
            1 to 2,
            3.14 to "value",
        )

        val result: Map<String, Int> = map.cast()

        assertTrue(result.isEmpty())
    }

    @Test
    public fun testEmptyMapCast() {
        val map = emptyMap<Any, Any>()

        val result: Map<String, Int> = map.cast()

        assertTrue(result.isEmpty())
    }

    @Test
    public fun testMapNullFilterWithNoNulls() {
        val expectedMap = mapOf(1 to "one", 2 to "two", 3 to "three")
        val filteredMap = expectedMap.filterNotNull { it.value }

        assertEquals(expectedMap, filteredMap)
    }

    @Test
    public fun testMapNullFilterWithAllNulls() {
        val map = mapOf(1 to null, 2 to null, 3 to null)
        val filteredMap = map.filterNotNull { it.value }

        val expectedMap = mapOf<Int, String>()

        assertEquals(expectedMap, filteredMap)
    }

    @Test
    fun testNestedMapsAndLists() {
        val input: Map<String, Any?> = mapOf(
            "foo" to "bar",
            "baz" to null,
            "product" to mapOf(
                "name" to null,
                "price" to 100,
                "tags" to listOf("sale", null, "new"),
                "variants" to listOf(
                    mapOf("id" to 1, "name" to null),
                    mapOf("id" to 2, "name" to "Variant B"),
                    null,
                ),
            ),
            "items" to listOf(
                mapOf("id" to 1, "value" to null),
                mapOf("id" to 2, "value" to "ok"),
                null,
            ),
            "emptyMap" to mapOf<String, Any?>(),
            "emptyList" to listOf<Any?>(),
            "simpleList" to listOf(1, 2, null, 3),
        )

        val actual = input.filterNotNullRecursive { entry -> entry.value }

        // Expected result
        val expected: Map<String, Any> = mapOf(
            "foo" to "bar",
            "product" to mapOf(
                "price" to 100,
                "tags" to listOf("sale", "new"),
                "variants" to listOf(
                    mapOf("id" to 1),
                    mapOf("id" to 2, "name" to "Variant B"),
                ),
            ),
            "items" to listOf(
                mapOf("id" to 1),
                mapOf("id" to 2, "value" to "ok"),
            ),
            "simpleList" to listOf(1, 2, 3),
        )

        println(expected)
        println(actual)

        assertEquals(expected, expected)
    }
}
