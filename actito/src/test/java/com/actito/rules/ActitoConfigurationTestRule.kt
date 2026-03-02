package com.actito.rules

import com.actito.Actito
import com.actito.ActitoEventsComponent
import com.actito.ActitoServicesInfo
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.robolectric.RuntimeEnvironment
import kotlin.reflect.KClass

class ActitoConfigurationTestRule(
    private val workflow: Workflow,
    private val servicesInfo: ActitoServicesInfo? = null,
) : TestWatcher() {

    override fun starting(description: Description) {
        if (description.testClass.kotlin in initializedClasses) return

        setupMocks()

        Actito.configure(
            RuntimeEnvironment.getApplication(),
            loadActitoServices(),
        )

        if (workflow == Workflow.LAUNCH) {
            runBlocking { Actito.launch() }
        }

        initializedClasses.add(description.testClass.kotlin)
    }

    override fun finished(description: Description) {
        val testClass = description.testClass.kotlin
        val finished = finishedTests.getOrPut(testClass) { mutableSetOf() }.apply { add(description.methodName) }
        val total = testClass.java.methods.count { it.isAnnotationPresent(Test::class.java) }

        if (finished.size >= total) {
            if (workflow == Workflow.LAUNCH) {
                runBlocking { Actito.unlaunch() }
            }

            unmockkAll()

            initializedClasses.remove(testClass)
            finishedTests.remove(testClass)
        }
    }

    private fun loadActitoServices(): ActitoServicesInfo {
        if (servicesInfo != null) {
            return servicesInfo
        }

        return ActitoServicesInfo(
            applicationKey = requireNotNull(System.getProperty("applicationKey")),
            applicationSecret = requireNotNull(System.getProperty("applicationSecret")),
            hosts = ActitoServicesInfo.Hosts(
                restApi = requireNotNull(System.getProperty("restApi")),
                shortLinks = requireNotNull(System.getProperty("shortLinks")),
                appLinks = requireNotNull(System.getProperty("appLinks")),
            ),
        )
    }

    private fun setupMocks() {
        val events = spyk(ActitoEventsComponent())
        every { events.scheduleUploadWorker() } returns Unit

        mockkObject(Actito)
        every { Actito.events() } returns events
    }

    companion object {
        private val initializedClasses = mutableSetOf<KClass<*>>()
        private val finishedTests = mutableMapOf<KClass<*>, MutableSet<String>>()
    }

    enum class Workflow {
        CONFIGURATION_ONLY,
        LAUNCH,
    }
}
