package com.actito.internal.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.junit.Assert.assertEquals
import org.junit.Test

class ExtrasAdapterTest {

    private val moshi = Moshi.Builder()
        .add(ExtrasAdapter())
        .build()

    private val type = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        Any::class.java,
    )

    private val adapter: JsonAdapter<Map<String, Any>> = moshi.adapter(type)

    @Test
    fun removeTopLevelNullsTest() {
        val json = """{"a":"x","b":null}"""
        val result = adapter.fromJson(json)
        assertEquals(mapOf("a" to "x"), result)
    }

    @Test
    fun removeNestedMapNullsTest() {
        val json = """{"nested":{"keep":"a","drop":null}}"""
        val result = adapter.fromJson(json)
        assertEquals(mapOf("nested" to mapOf("keep" to "a")), result)
    }

    @Test
    fun removeListNullsTest() {
        val json = """{"list":["a",null,"b",null]}"""
        val result = adapter.fromJson(json)
        assertEquals(mapOf("list" to listOf("a", "b")), result)
    }

    @Test
    fun removeNestedListAndMapNullsTest() {
        val json = """
            {
                "list": [
                    {"keep": "a", "drop": null},
                    null,
                    {"keep": "b"}
                ]
            }
        """.trimIndent()
        val result = adapter.fromJson(json)
        assertEquals(
            mapOf(
                "list" to listOf(
                    mapOf("keep" to "a"),
                    mapOf("keep" to "b"),
                ),
            ),
            result,
        )
    }

    @Test
    fun removeEmptyMapsAndListTest() {
        val json = """{"map": {"onlyNull": null}, "list": [null]}"""
        val result = adapter.fromJson(json)
        assertEquals(emptyMap<String, Any>(), result)
    }
}
