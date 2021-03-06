package com.therishka.paninbot

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("settings")
class BotConfig {
    var token: String? = null
    var botname: String? = null
    var enabled: Boolean? = true
}
