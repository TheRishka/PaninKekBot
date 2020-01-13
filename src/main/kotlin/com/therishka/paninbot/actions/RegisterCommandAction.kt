package com.therishka.paninbot.actions

import com.therishka.paninbot.BotConfig
import com.therishka.paninbot.chatId
import com.therishka.paninbot.data.UsersRepository
import com.therishka.paninbot.data.models.Command
import com.therishka.paninbot.toUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class RegisterCommandAction(val botConfig: BotConfig,
                            private val usersRepository: UsersRepository) : CommandAction(botConfig) {
    override val commandName = Command.START.command

    override fun fire(update: Update): suspend (AbsSender) -> Unit {
        return { sender ->
            val didUpdateTheStatus = GlobalScope.async {
                val user = update.toUser()
                val result = usersRepository.updateUserStatus(user, true)
                return@async result
            }.await()
            when (didUpdateTheStatus) {
                true -> {
                    sender.execute(SendMessage(update.chatId(), "Ты теперь зарегистрирован!"))
                }
                false -> {
                    sender.execute(SendMessage(update.chatId(), "Ты же уже зарегистрирован! Может" +
                            " ты хочешь выйти? Крикни /${Command.QUIT.name} I DARE YOU! I DOUBLE " +
                            "DARE " +
                            "YOU!!!"))
                }
            }
        }
    }
}

@Component
class QuitCommandAction(val botConfig: BotConfig,
                        private val usersRepository: UsersRepository) : CommandAction(botConfig) {
    override val commandName = Command.QUIT.command

    override fun fire(update: Update): suspend (AbsSender) -> Unit {
        return { sender ->
            val didUpdateTheStatus = GlobalScope.async {
                val user = update.toUser()
                val result = usersRepository.updateUserStatus(user, true)
                return@async result
            }.await()
            when (didUpdateTheStatus) {
                true -> {
                    sender.execute(SendMessage(update.chatId(), "Ты теперь вышел! Войди назад " +
                            "немедленно!"))
                }
                false -> {
                    sender.execute(SendMessage(update.chatId(), "Ты же уже вышел! Может" +
                            " ты хочешь зарегаться? Крикни /${Command.START.name} I DARE YOU! I " +
                            "DOUBLE" +
                            " DARE YOU!!!"))
                }
            }
        }
    }
}