package com.actito

public class ActitoNotConfiguredException :
    Exception("Actito hasn't been configured. Call Actito.configure() or enable the configuration provider.")

public class ActitoNotReadyException :
    Exception("Actito is not ready. Call Actito.launch() and wait for the 'ready' event.")

public class ActitoDeviceUnavailableException :
    Exception("Actito device unavailable at the moment. It becomes available after the first ready event.")

public class ActitoApplicationUnavailableException :
    Exception("Actito application unavailable at the moment. It becomes available after the first ready event.")

public class ActitoServiceUnavailableException(
    public val service: String,
) : Exception("Actito '$service' service is not available. Check the dashboard and documentation to enable it.")

public class ActitoGoogleServicesUnavailableException :
    Exception("Google services are not available on this device. Ensure they are installed and up to date.")

public class ActitoContentTooLargeException(
    override val message: String?,
) : Exception(message)
