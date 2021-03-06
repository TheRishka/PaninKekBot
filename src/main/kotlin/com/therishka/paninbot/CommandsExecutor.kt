package com.therishka.paninbot

import com.therishka.paninbot.actions.Action
import com.therishka.paninbot.actions.CommandAction
import com.therishka.paninbot.data.UsersRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class CommandsExecutor(
        val botConfig: BotConfig,
        val actions: List<Action>,
        val usersRepository: UsersRepository
) {
    private final val logger = LoggerFactory.getLogger(CommandsExecutor::class.java)

    fun processUpdate(update: Update): suspend (AbsSender) -> Unit {
        val message = update.message
                ?: update.editedMessage
                ?: update.callbackQuery.message
        logger.info("Handling message from: ${message.from.userName}")

        registerChatAndUser(message)

        val action = if (botConfig.enabled == true) {
            selectAction(update)
        } else {
            logger.info("The bot has been disabled. Handling only commands now!")
            selectCommands(update)
        }
        return action?.fire(update) ?: {
            logger.info("No Action has been found for this text: ${message.text}")
        }
    }

    private fun selectAction(update: Update): Action? = actions
            .sortedByDescending { it.priority }
            .find {
                it.canFire(update.message ?: update.editedMessage ?: update.callbackQuery.message)
            }

    private fun selectCommands(update: Update): Action? = actions
            .filterIsInstance<CommandAction>()
            .sortedByDescending { it.priority }
            .find {
                it.canFire(update.message ?: update.editedMessage ?: update.callbackQuery.message)
            }

    private fun registerChatAndUser(message: Message) {
        val messageChat = message.chat.toChat()
        val user = message.from.toUser(messageChat)

        usersRepository.addChat(messageChat)

        val userWhichLeftChat = message.leftChatMember

        when {
            !message.from.bot -> {
                logger.info("Adding ${user.nickname} to base!")
                usersRepository.addUser(user)
            }
        }
    }
}