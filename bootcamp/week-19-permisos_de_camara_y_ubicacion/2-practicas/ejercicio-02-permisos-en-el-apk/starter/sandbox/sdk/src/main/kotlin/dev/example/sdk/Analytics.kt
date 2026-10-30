package dev.example.sdk

object Analytics {
    val events = mutableListOf<String>()

    fun track(event: String) {
        events += event
    }
}
