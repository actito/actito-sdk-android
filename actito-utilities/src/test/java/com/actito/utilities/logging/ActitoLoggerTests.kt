package com.actito.utilities.logging

import android.util.Log
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations
import java.io.PrintWriter
import java.io.StringWriter

public class ActitoLoggerTests {
    private lateinit var logger: ActitoLogger
    private lateinit var mockLog: MockedStatic<Log>

    @Before
    public fun setup() {
        logger = ActitoLogger("Actito")
        logger.hasDebugLoggingEnabled = true

        MockitoAnnotations.openMocks(this)
        mockLog = mockStatic(Log::class.java)
    }

    @After
    public fun tearDown() {
        logger.labelClassIgnoreList = listOf()
        mockLog.close()
    }

    @Test
    public fun testDebugLogging() {
        val message = "Debug message"

        logger.debug(message)

        mockLog.verify {
            Log.println(Log.DEBUG, "Actito", "[ActitoLoggerTests] $message")
        }
    }

    @Test
    public fun testInfoLogging() {
        val message = "Info message"

        logger.info(message)

        mockLog.verify { Log.println(Log.INFO, "Actito", "[ActitoLoggerTests] $message") }
    }

    @Test
    public fun testErrorLogging() {
        val throwable = RuntimeException("Test exception")
        val message = "Error message"
        val expectedStackTrace = getStackTraceString(throwable)

        logger.error(message, throwable)

        val expectedMessage = "$message\n$expectedStackTrace"

        mockLog.verify { Log.println(Log.ERROR, "Actito", "[ActitoLoggerTests] $expectedMessage") }
    }

    @Test
    public fun testDebugLoggingDisabled() {
        val message = "[ActitoLoggerTest] Message that should not be logged"
        logger.hasDebugLoggingEnabled = false
        logger.debug(message)

        mockLog.verify({ Log.println(Log.DEBUG, "Actito", message) }, times(0))
    }

    @Test
    public fun testDebugLoggingWithLabelIgnore() {
        val message = "Debug message"
        logger.labelClassIgnoreList = listOf(
            ActitoLoggerTests::class,
        )
        logger.debug(message)

        mockLog.verify {
            Log.println(Log.DEBUG, "Actito", message)
        }
    }

    private fun getStackTraceString(t: Throwable): String {
        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }
}
