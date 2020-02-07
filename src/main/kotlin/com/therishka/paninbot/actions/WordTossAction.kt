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
        private val randomWords = arrayOf("ну",
                "йобана",
                "как",
                "так",
                "однако, ",
                "блиааааа",
                "хочу",
                "Плотва")
        private val triggerWords = arrayOf("какать",
                "плотва",
                "панин",
                "весело")
        private val shouldFireRandomNumbers = listOf(5, 11)
    }

    override val priority = 1

    override fun fire(update: Update): suspend (AbsSender) -> Unit {
        val message = update.message
        val text = message.text ?: return {}

        val result = if (triggerWords.any { text.contains(it) }) {
            putRandomWordsIntoSentence(text)
        } else {
            reverseSentence(text)
        }
        return when (result.isNotEmpty()) {
            true -> { it -> it.execute(SendMessage(update.chatId(), result)) }
            false -> {
                {}
            }
        }
    }

    private fun putRandomWordsIntoSentence(sentenceToEdit: String): String {
        val splitted = sentenceToEdit.split(" ")
        val result = StringBuilder()
        return if (splitted.size > 1) {
            splitted.forEachIndexed { index, word ->
                run {
                    if (index % 2 == 0) {
                        result.append(word.toLowerCase())
                                .append(" ")
                                .append(randomWords[getRandomNumber(until = randomWords.size)])
                                .append(" ")
                    }
                }
            }
            result.toString().capitalize()
        } else {
            sentenceToEdit
        }
    }

    private fun reverseSentence(sentenceToReverse: String): String {
        val splitted = sentenceToReverse.split(" ")
        val reversed = StringBuilder()
        return if (splitted.size > 1) {
            splitted.asReversed().forEach {
                reversed.append(it.toLowerCase()).append(" ")
            }
            reversed.toString().capitalize()
        } else {
            sentenceToReverse
        }
    }

    private fun getRandomNumber(until: Int = 30) = Random.nextInt(until)

    override fun canFire(message: Message): Boolean = shouldFireRandomNumbers.contains(getRandomNumber())
}