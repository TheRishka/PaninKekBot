package com.therishka.paninbot.actions

import com.therishka.paninbot.chatId
import com.therishka.paninbot.data.CurrencyRepository
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import kotlin.math.truncate

@Component
class CurrencyConvertAction(private val currencyRepository: CurrencyRepository) : Action {
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
            if (!update.hasMessage()) {
                it.execute(SendMessage(update.chatId(), "Опять пытаешься пустышку конвертировать?"))
            } else {
                val message = update.message
                val convertText = extractConvertTextFromMessage(message)
                val result = if (convertText.isNotBlank()) {
                    val amount = convertText.filter { text -> text.isDigit() }
                    val baseCurrency = convertText
                            .drop(amount.length)
                            .trim()
                            .substring(0, 3)
                            .toUpperCase()
                    val targetCurrency = convertText
                            .substring(convertText.length - 3, convertText.length)
                            .toUpperCase()

                    it.execute(
                            SendMessage(update.chatId(),
                                    "Так над посчитать $amount из $baseCurrency в $targetCurrency"))
                    delay(500)
                    it.execute(
                            SendMessage(update.chatId(),
                                    "Так, ща, думаю... это умножить... пажи йобана... а это сложить..."))
                    delay(500)
                    executeApiRequest(baseCurrency, targetCurrency, amount)
                } else "Shit! Не получается понять че там надо конвертировать"
                it.execute(SendMessage(update.chatId(), result))
            }
        }
    }

    private fun extractConvertTextFromMessage(message: Message) =
            regexToSupport.toLowerCase().toRegex().find(message.text.toLowerCase())?.let {
                message.text.substring(it.range)
            } ?: ""

    private suspend fun executeApiRequest(
            baseCurrency: String,
            targetCurrency: String,
            amount: String
    ) = try {
        val rates = currencyRepository.getRates(baseCurrency = baseCurrency)
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

    @Suppress("SimpleRedundantLet")
    override fun canFire(message: Message) = message.text?.let {
        it.toLowerCase().contains(regexToSupport.toLowerCase().toRegex())
    } ?: false
}