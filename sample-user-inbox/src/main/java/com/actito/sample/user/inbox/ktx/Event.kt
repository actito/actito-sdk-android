package com.actito.sample.user.inbox.ktx

sealed class Event {
    data class ShowSnackBar(val text: String) : com.actito.sample.user.inbox.ktx.Event()
}
