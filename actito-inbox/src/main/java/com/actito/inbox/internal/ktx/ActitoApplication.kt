package com.actito.inbox.internal.ktx

import com.actito.models.ActitoApplication

internal val ActitoApplication.hasInboxServiceEnabled: Boolean
    get() = services[ActitoApplication.ServiceKeys.INBOX] == true && inboxConfig?.useInbox == true
