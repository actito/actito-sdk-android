package com.actito.inbox.ktx

import com.actito.Actito
import com.actito.inbox.ActitoInbox
import com.actito.inbox.internal.ActitoInboxImpl

@Suppress("unused")
public fun Actito.inbox(): ActitoInbox {
    return ActitoInboxImpl
}

internal fun Actito.inboxImplementation(): ActitoInboxImpl {
    return inbox() as ActitoInboxImpl
}
