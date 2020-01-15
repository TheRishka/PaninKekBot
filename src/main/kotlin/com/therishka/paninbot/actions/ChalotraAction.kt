package com.therishka.paninbot.actions

import com.therishka.paninbot.chatId
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class ChalotraAction : Action {

    companion object {
        private val possibleChalotraNames = listOf("чалотра",
                "чарлотра",
                "чалота",
                "чарлота",
                "чирлота",
                "чалота",
                "чалорта",
                "йени",
                "йен",
                "йеннифер",
                "йенифер",
                "йеннифэр"
        )
    }

    override val priority = 2

    override fun fire(update: Update): suspend (AbsSender) -> Unit {
        return { it -> it.execute(SendMessage(update.chatId(), "Чалотра багиня!")) }
    }

    override fun canFire(message: Message): Boolean {
        return message.hasText() && possibleChalotraNames.any {
            message.text.contains(it, ignoreCase = true)
        }
    }
}