package com.therishka.paninbot.actions

import com.therishka.paninbot.BotConfig
import org.telegram.telegrambots.meta.api.objects.Message

abstract class CommandAction(private val botConfig: BotConfig) : Action {
    override val priority = 990

    abstract val commandName: String

    override fun canFire(message: Message): Boolean {
        val entities = message.entities
        return entities?.let {
            it.any { entity ->
                entity.type == "bot_command"
                        && entity.text == commandName
                        && isCommandDedicatedToPaninBot(entity.text)
            }
        } ?: false
    }

    private fun isCommandDedicatedToPaninBot(commandText: String) =
            commandText.contains("@").not()
                    .or(commandText.contains(commandName + "@${botConfig.botname}"))
}