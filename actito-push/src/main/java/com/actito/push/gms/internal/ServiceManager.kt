package com.actito.push.gms.internal

import androidx.annotation.Keep
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await
import com.actito.InternalActitoApi
import com.actito.Actito
import com.actito.push.models.ActitoTransport
import com.actito.push.internal.ServiceManager

@Keep
@InternalActitoApi
public class ServiceManager : ServiceManager() {

    override val available: Boolean
        get() = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(Actito.requireContext()) == ConnectionResult.SUCCESS

    override val transport: ActitoTransport
        get() = ActitoTransport.GCM

    override suspend fun getPushToken(): String {
        return Firebase.messaging.token.await()
    }
}
