package com.therishka.paninbot

import com.therishka.paninbot.actions.Action
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class CommandsExecutor(
        val actions: List<Action>
) {
    private final val logger = LoggerFactory.getLogger(CommandsExecutor::class.java)

    fun processUpdate(update: Update): suspend (AbsSender) -> Unit {

        val message = update.message
                ?: update.editedMessage
                ?: update.callbackQuery.message

        val chat = message.chat

        val action = selectAction(update)
        return action?.fire(update) ?: {
            logger.info("No Action has been found for this text: ${message.text}")
        }
    }

    private fun selectAction(update: Update): Action? = actions
            .sortedByDescending { it.priority }
            .find {
                it.canFire(update.message ?: update.editedMessage ?: update.callbackQuery.message)
            }
}