package com.actito

import com.actito.models.ActitoEventData

public interface ActitoEventsModule {

    /**
     * Logs in Actito an application exception for diagnostic tracking.
     *
     * This method logs in Actito exceptions within the application, helping capture and record critical issues
     * that may affect user experience.
     *
     * @param throwable The exception instance to be logged.
     */
    public suspend fun logApplicationException(throwable: Throwable)

    /**
     * Logs in Actito an application exception, with a callback.
     *
     * This method logs in Actito exceptions within the application, helping capture and record critical issues
     * that may affect user experience.
     *
     * @param throwable The exception instance to be logged.
     * @param callback The callback invoked upon completion of the logging operation.
     */
    public fun logApplicationException(throwable: Throwable, callback: ActitoCallback<Unit>)

    /**
     * Logs in Actito when a notification has been opened by the user.
     *
     * This function logs in Actito the opening of a notification, enabling insight into user engagement with
     * specific notifications.
     *
     * @param id The unique identifier of the opened notification.
     */
    public suspend fun logNotificationOpen(id: String)

    /**
     * Logs in Actito when a notification has been opened by the user, with a callback.
     *
     * This function logs in Actito the opening of a notification, enabling insight into user engagement with
     * specific notifications.
     *
     * @param id The unique identifier of the opened notification.
     * @param callback The callback invoked upon completion of the logging operation.
     */
    public fun logNotificationOpen(id: String, callback: ActitoCallback<Unit>)

    /**
     * Logs in Actito a custom event in the application.
     *
     * This function allows logging, in Actito, of application-specific events, optionally associating structured
     * data for more detailed event tracking and analysis.
     *
     * @param event The name of the custom event to log.
     * @param data Optional structured event data for further details.
     */
    public suspend fun logCustom(event: String, data: ActitoEventData? = null)

    /**
     * Logs a custom event in the application, with a callback.
     *
     * This function allows logging, in Actito, of application-specific events, optionally associating structured
     * data for more detailed event tracking and analysis.
     *
     * @param event The name of the custom event to log.
     * @param data Optional structured event data for further details.
     * @param callback The callback invoked upon completion of the logging operation.
     */
    public fun logCustom(event: String, data: ActitoEventData? = null, callback: ActitoCallback<Unit>)
}

public interface ActitoInternalEventsModule {

    @InternalActitoApi
    public suspend fun log(
        event: String,
        data: ActitoEventData? = null,
        sessionId: String? = null,
        notificationId: String? = null,
    )
}
