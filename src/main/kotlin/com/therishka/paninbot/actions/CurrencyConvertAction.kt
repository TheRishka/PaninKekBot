package com.therishka.paninbot.actions

import com.therishka.paninbot.chatId
import com.therishka.paninbot.data.models.JsonMapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import kotlin.math.truncate

@Component
class CurrencyConvertAction(val restTemplate: RestTemplate,
                            val baseUrl: String) : Action {
    companion object {
        private val supportedCurrencies = listOf(
                "CAD",
                "HKD",
                "ISK",
                "PHP",
                "DKK",
                "HUF",
                "CZK",
                "GBP",
                "RON",
                "SEK",
                "IDR",
                "INR",
                "BRL",
                "RUB",
                "HRK",
                "JPY",
                "THB",
                "CHF",
                "EUR",
                "MYR",
                "BGN",
                "TRY",
                "CNY",
                "NOK",
                "NZD",
                "ZAR",
                "USD",
                "MXN",
                "SGD",
                "AUD",
                "ILS",
                "KRW",
                "PLN"
        )
        private val triggerWords = listOf(
                "to",
                "в"
        )
        // something like [1-90]{1,}\s?(EUR|USD|RUB|HRN)\s?(to|в)\s?(EUR|USD|RUB|HRN)
        val regexToSupport = "[1-90]{1,}\\s?" +
                "(${supportedCurrencies.joinToString(separator = "|")})" +
                "\\s?" +
                "(${triggerWords.joinToString(separator = "|")})" +
                "\\s?" +
                "(${supportedCurrencies.joinToString(separator = "|")})"
    }

    override val priority = 100

    override fun fire(update: Update): suspend (AbsSender) -> Unit {

        return {
            val whatToSay = GlobalScope.async {
                val stringByRegex = if (update.hasMessage()) {
                    val message = update.message
                    val regexMatch = regexToSupport.toLowerCase().toRegex().find(message.text)
                    regexMatch?.let {
                        message.text.substring(it.range)
                    } ?: ""
                } else {
                    ""
                }
                val result = if (stringByRegex.isNotBlank()) {
                    val amount = stringByRegex.filter { text -> text.isDigit() }
                    val baseCurrency = stringByRegex
                            .drop(amount.length)
                            .trim()
                            .substring(0, 3)
                            .toUpperCase()
                    val targetCurrency = stringByRegex
                            .substring(stringByRegex.length - 3, stringByRegex.length)
                            .toUpperCase()

                    it.execute(
                            SendMessage(update.chatId(),
                                    "Так над посчитать $amount из $baseCurrency в $targetCurrency"))
                    delay(500)
                    it.execute(
                            SendMessage(update.chatId(),
                                    "Так, ща, думаю... это умножить... пажи йобана... а это сложить..."))
                    delay(500)
                    val apiResult = try {
                        val response = restTemplate.getForObject("$baseUrl/latest?base=$baseCurrency", String::class.java)
                        val rates = JsonMapper.mapCurrencyRatesFromJson(response)
                        val convertRate = rates.find { rate ->
                            rate.shortName.equals(targetCurrency, ignoreCase = true)
                        }?.value?.times(amount.toInt()) ?: 0.0
                        val convertResult = if (convertRate > 0) {
                            truncate(convertRate).toString()
                        } else "неебу"
                        "Вот что я получил:\n$amount $baseCurrency в $targetCurrency будет $convertResult"
                    } catch (error: Exception) {
                        "Не получилось спросить у интернета!"
                    }
                    apiResult
                } else "Shit! Не получается понять че там надо конвертировать"
                result
            }.await()
            it.execute(SendMessage(update.chatId(), whatToSay))
        }
    }

    @Suppress("SimpleRedundantLet")
    override fun canFire(message: Message) = message.text?.let {
        it.toLowerCase().contains(regexToSupport.toLowerCase().toRegex())
    } ?: false


}