package com.therishka.paninbot.data.models

enum class Command(val command: String, val id: Int) {
    START("/start", 1),
    QUIT("/quit", 2),
    RATING("/my_rating", 3),
    TODAY("/today", 4),
}