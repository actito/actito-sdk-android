package com.actito.e2e.device

import com.actito.Actito
import com.actito.rules.ActitoConfigurationTestRule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ActitoDeviceTagsTest {
    private val sampleTags = listOf("android", "testing", "remove-me")
    private val sampleTagToRemove = "remove-me"

    @get:Rule
    val configurationRule = ActitoConfigurationTestRule(
        workflow = ActitoConfigurationTestRule.Workflow.CONFIGURATION_ONLY,
    )

    @Before
    fun setUp() {
        runBlocking {
            Actito.launch()
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            Actito.unlaunch()
        }
    }

    @Test
    fun `test invalid formats`() {
        val invalidTags = listOf(
            "te",
            "test.",
            "test&",
            ".test&",
            "test_test_test_test_test_test_test_test_test_test_test_test_test_test",
        )

        for (tag in invalidTags) {
            Assert.assertThrows(IllegalArgumentException::class.java) {
                runTest {
                    Actito.device().addTag(tag)
                }
            }
        }
    }

    @Test
    fun `ensure initially tags are empty`() = runTest {
        val currentTags = Actito.device().fetchTags()

        assert(currentTags.isEmpty())
    }

    @Test
    fun `add tags`() = runTest {
        Actito.device().addTags(sampleTags)

        val tags = Actito.device().fetchTags()

        assertEquals(sampleTags.size, tags.size)
        assert(tags.containsAll(sampleTags))
    }

    @Test
    fun `remove one tag`() = runTest {
        Actito.device().addTags(sampleTags)
        Actito.device().removeTag(sampleTagToRemove)

        val tags = Actito.device().fetchTags()
        val expectedTags = sampleTags.filter { it != sampleTagToRemove }

        assert(sampleTags.size > tags.size)
        assert(tags.containsAll(expectedTags))
        assert(!tags.contains(sampleTagToRemove))
    }

    @Test
    fun `clear one tags`() = runTest {
        Actito.device().addTags(sampleTags)
        Actito.device().clearTags()

        val tags = Actito.device().fetchTags()

        assert(tags.isEmpty())
    }
}
