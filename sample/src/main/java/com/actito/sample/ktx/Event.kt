package com.actito.sample.ktx

sealed class Event {
    data class ShowSnackBar(val text: String) : Event()
}
