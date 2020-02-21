package com.therishka.paninbot.actions

import com.therishka.paninbot.BotConfig
import com.therishka.paninbot.chatId
import com.therishka.paninbot.data.models.Command
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

val admins = listOf(
        "RishMustafaev" to 58462501
)

@Component
class EnableBotCommandAction(val botConfig: BotConfig) : CommandAction(botConfig) {

    override val commandName = Command.ENABLE.command

    override fun fire(update: Update): suspend (AbsSender) -> Unit {
        return {
            val message = if (botConfig.enabled == false) {
                "Включил"
            } else {
                "Уже же включено"
            }
            botConfig.enabled = true
            it.execute(SendMessage(update.chatId(), message))
        }
    }

    override fun checkUser(message: Message): Boolean {
        return admins.any {
            message.from?.userName == it.first || message.from?.id == it.second
        }
    }
}

@Component
class DisableBotCommandAction(val botConfig: BotConfig) : CommandAction(botConfig) {

    override val commandName = Command.DISABLE.command

    override fun fire(update: Update): suspend (AbsSender) -> Unit {
        return {
            val message = if (botConfig.enabled == true) {
                "Выключил"
            } else {
                "Уже же выключено"
            }
            botConfig.enabled = false
            it.execute(SendMessage(update.chatId(), message))
        }
    }

    override fun checkUser(message: Message): Boolean {
        return admins.any {
            message.from?.userName == it.first || message.from?.id == it.second
        }
    }
}