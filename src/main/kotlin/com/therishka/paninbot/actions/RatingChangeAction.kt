package com.therishka.paninbot.actions

import com.therishka.paninbot.chatId
import com.therishka.paninbot.data.UsersRepository
import com.therishka.paninbot.data.models.RatingChange
import com.therishka.paninbot.data.models.User
import com.therishka.paninbot.toChat
import com.therishka.paninbot.toUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
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
        private const val howOftenUserCanVoteInMs = 1000 * 60 * 60 * 2
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
                val lastUpdateTime = GlobalScope.async {
                    return@async usersRepository.getLastChangeUserRatingDate(
                            userAuthor = update.toUser(),
                            userTarget = userToChangeRatingFor
                    )
                }.await()

                val message = lastUpdateTime?.let {
                    val ratingPeriod = Period().withMillis(howOftenUserCanVoteInMs)
                    when (it.plus(ratingPeriod).isBeforeNow) {
                        true -> createRatingChangeMessage(update, userToChangeRatingFor, ratingAction)
                        false -> createYouCantChangeMessage(it)
                    }
                } ?: createRatingChangeMessage(update, userToChangeRatingFor, ratingAction)
                it.execute(SendMessage(update.chatId(), message))
            }
        }
    }

    private fun createYouCantChangeMessage(lastRatingUpdateTime: DateTime): String {
        val period = Period(DateTime.now(), lastRatingUpdateTime.plus(howOftenUserCanVoteInMs.toLong()))
        val seconds = period.seconds.toPlural("секунду", "секунды", "секунд")
        val minutes = period.minutes.toPlural("минуту", "минуты", "минут")
        val hours = period.hours.toPlural("час", "часа", "часов")
        val formatter = PeriodFormatterBuilder()
                .appendHours().appendSuffix(" $hours\n")
                .appendMinutes().appendSuffix(" $minutes\n")
                .appendSeconds().appendSuffix(" $seconds\n")
                .printZeroNever()
                .toFormatter()
        return "Вы сможете изменить рейтинг через:\n${formatter.print(period)}"
    }

    private suspend fun createRatingChangeMessage(update: Update,
                                                  userToChangeRatingFor: User,
                                                  ratingAction: RatingChange): String {
        val newRating = GlobalScope.async {
            return@async usersRepository.changeUserRating(
                    userAuthor = update.toUser(),
                    userTarget = userToChangeRatingFor,
                    ratingChange = ratingAction
            )
        }.await()
        return when (ratingAction) {
            RatingChange.UNKNOWN -> {
                logger.error("Unknown rating action! Never should have happened")
                "Я не знаю что делать! Как так получилось?"
            }
            else -> "Теперь рейтинг ${userToChangeRatingFor.name} составляет $newRating"
        }
    }

    override fun canFire(message: Message) = if (message.isReply) {
        when {
            message.replyToMessage?.from?.id?.toLong() == message.from?.id?.toLong() -> {
                // if user is trying to increase own rating
                true
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

    private fun Int.toPlural(one: String, few: String, many: String) = when {
        this % 10 == 1 && this % 100 != 11 -> one
        this % 10 in 2..4 && (this % 100 < 10 || this % 100 >= 20) -> few
        else -> many
    }
}