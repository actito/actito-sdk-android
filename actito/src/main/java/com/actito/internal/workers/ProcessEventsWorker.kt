package com.actito.internal.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.actito.Actito
import com.actito.internal.logger
import com.actito.internal.network.NetworkException
import com.actito.internal.network.request.ActitoRequest
import com.actito.internal.storage.database.entities.ActitoEventEntity
import com.actito.internal.storage.database.ktx.toPayload
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import kotlin.coroutines.cancellation.CancellationException

private const val MAX_RETRIES = 5

internal class ProcessEventsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Actito.database.events().find().forEach { processEvent(it) }

            if (Actito.database.events().find().isEmpty()) {
                logger.debug("Finished processing all the events.")
                Result.success()
            } else {
                logger.debug("Some events are still pending. Retrying later.")
                Result.retry()
            }
        } catch (e: Exception) {
            logger.warning("Failed to process the stored events.", e)
            return Result.failure()
        }
    }

    private suspend fun processEvent(entity: ActitoEventEntity) {
        logger.debug("Processing event #${entity.id}")

        val now = Date()
        val createdAt = Date(entity.timestamp)
        val expiresAt = GregorianCalendar()
            .let { calendar ->
                calendar.time = createdAt
                calendar.add(Calendar.SECOND, entity.ttl)
                calendar.time
            }

        if (now.after(expiresAt)) {
            logger.debug("Event expired. Removing...")
            Actito.database.events().delete(entity)
            return
        }

        try {
            ActitoRequest.Builder()
                .post("/event", entity.toPayload())
                .response()

            logger.debug("Event processed. Removing from storage...")
            Actito.database.events().delete(entity)
        } catch (e: CancellationException) {
            throw e
        } catch (_: NetworkException.InaccessibleServiceException) {
            logger.debug("Failed to process event.")

            // Increase the attempts counter.
            entity.retries++

            if (entity.retries < MAX_RETRIES) {
                // Persist the attempts counter.
                Actito.database.events().update(entity)
            } else {
                logger.debug("Event was retried too many times. Removing...")
                Actito.database.events().delete(entity)
            }
        } catch (_: Exception) {
            logger.debug("Failed to process event due to an unrecoverable error. Discarding it...")
            Actito.database.events().delete(entity)
        }
    }
}
