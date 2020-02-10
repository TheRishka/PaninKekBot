package com.therishka.paninbot.data

import com.therishka.paninbot.data.models.Rate

interface CurrencyRepository {
    suspend fun getRates(baseCurrency: String) : List<Rate>
}