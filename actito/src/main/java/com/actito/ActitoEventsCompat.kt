package com.actito

import com.actito.ktx.events
import com.actito.models.ActitoEventData

public object ActitoEventsCompat {

    /**
     * Logs in Actito an application exception, with a callback.
     *
     * This method logs in Actito exceptions within the application, helping capture and record critical issues that
     * may affect user experience.
     *
     * @param throwable The exception instance to be logged.
     * @param callback The callback invoked upon completion of the logging operation.
     */
    @JvmStatic
    public fun logApplicationException(throwable: Throwable, callback: ActitoCallback<Unit>) {
        Actito.events().logApplicationException(throwable, callback)
    }

    /**
     * Logs in Actito when a notification has been opened by the user, with a callback.
     *
     * This function logs in Actito the opening of a notification, enabling insight into user engagement with
     * specific notifications.
     *
     * @param id The unique identifier of the opened notification.
     * @param callback The callback invoked upon completion of the logging operation.
     */
    @JvmStatic
    public fun logNotificationOpen(id: String, callback: ActitoCallback<Unit>) {
        Actito.events().logNotificationOpen(id, callback)
    }

    /**
     * Logs in Actito a custom event in the application, with a callback.
     *
     * This function allows logging, in Actito,  of application-specific events.
     *
     * @param event The name of the custom event to log.
     * @param callback The callback invoked upon completion of the logging operation.
     */
    @JvmStatic
    public fun logCustom(event: String, callback: ActitoCallback<Unit>) {
        Actito.events().logCustom(
            event = event,
            callback = callback,
        )
    }

    /**
     * Logs in Actito a custom event in the application, with a callback.
     *
     * This function allows logging, in Actito, of application-specific events, optionally associating structured
     * data for more detailed event tracking and analysis.
     *
     * @param event The name of the custom event to log.
     * @param data Optional structured event data for further details.
     * @param callback The callback invoked upon completion of the logging operation.
     */
    @JvmStatic
    public fun logCustom(event: String, data: ActitoEventData?, callback: ActitoCallback<Unit>) {
        Actito.events().logCustom(event, data, callback)
    }
}
