package com.therishka.paninbot

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException

@Suppress("UNNECESSARY_SAFE_CALL")
@Component
class PaninBot(
        val config: BotConfig,
        val executor: CommandsExecutor
) : TelegramLongPollingBot() {

    private final val log = LoggerFactory.getLogger(PaninBot::class.java)
    private final val channels = HashMap<Long, Channel<Update>>()

    override fun getBotToken(): String {
        return config.token ?: throw InternalException("Expression 'config.token' must not be null")
    }

    override fun onUpdateReceived(update: Update?) {

        val chat = update?.toChat() ?: throw InternalException("Update should not be null")

        val chatChannel = channels.computeIfAbsent(chat.id) {
            Channel<Update>().also {
                GlobalScope.launch {
                    for (newUpdate in it) {
                        fireAction(newUpdate)
                    }
                }
            }
        }
        GlobalScope.launch { chatChannel.send(update) }
        log.info("Damn update received! chat = ${chat.name} and chatId is ${chat.id}")
        log.info("User said: ${update?.message.text}")
    }

    override fun getBotUsername(): String {
        return config.botname
                ?: throw InternalException("Expression 'config.botname' must not be null")
    }

    private suspend fun fireAction(update: Update) {
        val user = update.toUser()
        log.info("Handling update from ${user.name}")
        try {
            executor.processUpdate(update).invoke(this)
        } catch (telegramException: TelegramApiRequestException) {
            log.error("Error happened! ${telegramException.localizedMessage}")
        } catch (unknownException: Exception) {
            log.error("Unknown shit happened! ${unknownException.localizedMessage}")
        }
    }

    class InternalException(override val message: String?) : RuntimeException(message)
}