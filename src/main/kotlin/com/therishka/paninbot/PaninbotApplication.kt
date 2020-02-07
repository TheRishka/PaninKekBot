package com.therishka.paninbot

import org.jetbrains.exposed.sql.Database
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi

@SpringBootApplication
@EnableConfigurationProperties(BotConfig::class)
class PaninbotApplication {

    @Bean
    fun telegramBot(bot: PaninBot): TelegramBotsApi {
        val telegramBotsApi = TelegramBotsApi()
        telegramBotsApi.registerBot(bot)
        return telegramBotsApi
    }

    @Bean
    fun database(): Database {
        return Database.connect(
                "jdbc:postgresql://localhost:5432/paninbotdb",
                driver = "org.postgresql.Driver",
                user = "bot_app",
                password = "m2380103"
        )
    }
}

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    val app = SpringApplication(PaninbotApplication::class.java)
    app.webApplicationType = WebApplicationType.NONE
    app.run(*args)
}
