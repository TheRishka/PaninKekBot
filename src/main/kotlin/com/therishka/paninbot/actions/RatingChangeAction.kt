package com.therishka.paninbot.actions

import com.therishka.paninbot.chatId
import com.therishka.paninbot.data.UsersRepository
import com.therishka.paninbot.data.models.RatingChange
import com.therishka.paninbot.toUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class RatingChangeAction(private val usersRepository: UsersRepository) : Action {

    private final val logger = LoggerFactory.getLogger(RatingChangeAction::class.java)

    companion object {
        private val ratingIncreaseTriggers = listOf(
                "+",
                "спс",
                "спасиб",
                "thx",
                "thank",
                "сяпки",
                "дякую",
                "плюс"
        )
        private val ratingDecreaseTriggers = listOf(
                "-",
                "минус"
        )
    }

    override val priority = 100

    override fun fire(update: Update): suspend (AbsSender) -> Unit {
        return {
            val text = update.message.text
            val user = update.toUser()
            val ratingAction = determineRatingAction(text)
            val newRating = GlobalScope.async {
                return@async usersRepository.changeUserRating(user, ratingAction)
            }.await()
            val message = when (ratingAction) {
                RatingChange.DECREMENT -> {
                    "Я уменьшил рейтинг! Теперь рейтинг ${user.name} составляет $newRating"
                }
                RatingChange.INCREMENT -> {
                    "Я увеличил рейтинг! Теперь рейтинг ${user.name} составляет $newRating"
                }
                RatingChange.UNKNOWN -> {
                    logger.error("Unknown rating action! Never should happen")
                    "Я не знаю что делать! Как так получилось?"
                }
            }
            it.execute(SendMessage(update.chatId(), message))
        }
    }

    override fun canFire(message: Message) = if (message.isReply) {
        when {
            message.replyToMessage?.from?.bot ?: false -> {
                false
            }
            message.hasText() -> {
                val textToCheck = getFirstLettersOfMessage(message.text)
                (ratingDecreaseTriggers + ratingIncreaseTriggers).any { triggerWord ->
                    textToCheck.contains(triggerWord)
                }
            }
            else -> false
        }
    } else {
        false
    }

    private fun determineRatingAction(messageText: String) = when {
        ratingDecreaseTriggers.any { messageText.contains(it) } -> RatingChange.DECREMENT
        ratingIncreaseTriggers.any { messageText.contains(it) } -> RatingChange.INCREMENT
        else -> RatingChange.UNKNOWN
    }

    private fun getFirstLettersOfMessage(message: String) = if (message.length > 6) {
        message.substring(0, 6)
    } else {
        message
    }
}