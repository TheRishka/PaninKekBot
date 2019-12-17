package com.therishka.paninbot.actions

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

interface Action {

    fun fire(update: Update): suspend (AbsSender) -> Unit

    fun canFire(message: Message): Boolean
}