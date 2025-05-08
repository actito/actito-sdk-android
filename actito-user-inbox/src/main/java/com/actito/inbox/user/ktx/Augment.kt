package com.actito.inbox.user.ktx

import com.actito.Actito
import com.actito.inbox.user.ActitoUserInbox
import com.actito.inbox.user.internal.ActitoUserInboxImpl

@Suppress("unused")
public fun Actito.userInbox(): ActitoUserInbox {
    return ActitoUserInboxImpl
}
