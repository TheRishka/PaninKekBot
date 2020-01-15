package com.therishka.paninbot.actions

import com.therishka.paninbot.chatId
import com.therishka.paninbot.data.UsersRepository
import com.therishka.paninbot.data.models.RatingChange
import com.therishka.paninbot.toChat
import com.therishka.paninbot.toUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
        private const val maxCharsCountOfMessage = 15
        private val ratingIncreaseTriggers = listOf(
                "+",
                "спс",
                "спасиб",
                "thx",
                "thank",
                "сяпки",
                "дякую",
                "плюс",
                "яростно плюсую"
        )
        private val ratingDecreaseTriggers = listOf(
                "-",
                "минус"
        )
    }

    override val priority = 100

    override fun fire(update: Update): suspend (AbsSender) -> Unit {
        return { it ->
            val text = update.message.text
            val userToChangeRating = update.message?.replyToMessage?.let { repliedMessage ->
                if (repliedMessage.from?.bot?.not() == true) {
                    repliedMessage.from.toUser(update.toChat())
                } else {
                    null
                }
            }
            userToChangeRating?.let { userToChangeRatingFor ->
                val ratingAction = determineRatingAction(text)
                val newRating = GlobalScope.async {
                    return@async usersRepository.changeUserRating(userToChangeRatingFor, ratingAction)
                }.await()
                val message = when (ratingAction) {
                    RatingChange.UNKNOWN -> {
                        logger.error("Unknown rating action! Never should happen")
                        "Я не знаю что делать! Как так получилось?"
                    }
                    else -> "Теперь рейтинг ${userToChangeRatingFor.name} составляет $newRating"
                }
                it.execute(SendMessage(update.chatId(), message))
            }
        }
    }

    override fun canFire(message: Message) = if (message.isReply) {
        when {
            message.replyToMessage?.from?.id?.toLong() == message.from?.id?.toLong() -> {
                // if user is trying to increase own rating
                false
            }
            message.replyToMessage?.from?.bot ?: false -> {
                // if user has replied to any bot message
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

    private fun getFirstLettersOfMessage(message: String) =
            if (message.length > maxCharsCountOfMessage) {
                message.substring(0, maxCharsCountOfMessage)
            } else {
                message
            }
}