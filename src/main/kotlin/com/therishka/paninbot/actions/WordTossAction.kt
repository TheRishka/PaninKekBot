package com.therishka.paninbot.actions

import com.therishka.paninbot.chatId
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import kotlin.random.Random

@Component
class WordTossAction : Action {

    companion object {
        private val justReverseAction = 5
        private val putRandomWordsIntoAction = 7
        private val randomWords = arrayOf("ну",
                "йобана",
                "как",
                "так",
                "однако, ",
                "блиааааа",
                "хочу",
                "Плотва")
    }

    override fun fire(update: Update): suspend (AbsSender) -> Unit {
        val message = update.message
        val text = message.text ?: return {}

        val result = when (getRandomNumber()) {
            5 -> tossTheSentence(text, 5)
            7 -> tossTheSentence(text, 7)
            3 -> tossTheSentence(text, 5)
            1 -> tossTheSentence(text, 7)
            else -> ""
        }
        return when (result.isNotEmpty()) {
            true -> { it -> it.execute(SendMessage(update.chatId(), result)) }
            false -> {
                {}
            }
        }
    }

    private fun tossTheSentence(messageToToss: String, action: Int) = when (action) {
        justReverseAction -> {
            reverseSentence(messageToToss)
        }
        putRandomWordsIntoAction -> {
            reverseSentence(messageToToss)
        }
        else -> ""
    }

    private fun reverseSentence(sentenceToReverse: String): String {
        val splitted = sentenceToReverse.split(" ")
        val reversed = StringBuilder()
        return if (splitted.size > 1) {
            splitted.asReversed().forEach {
                reversed.append(it).append(" ")
            }
            reversed.toString()
        } else {
            sentenceToReverse
        }
    }

    private fun getRandomNumber() = Random.nextInt(until = 10)

    override fun canFire(message: Message) = true

}