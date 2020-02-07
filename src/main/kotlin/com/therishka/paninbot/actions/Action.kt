package com.therishka.paninbot.actions

import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

/**
 * Priorities:
 *  WordToss == 1
 *  Chalotra == 2
 *  Rating Change = 100
 *  Default Command == 999
 */
interface Action {

    val priority: Int

    fun fire(update: Update): suspend (AbsSender) -> Unit

    fun canFire(message: Message): Boolean
}