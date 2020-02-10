package com.therishka.paninbot.data

import com.therishka.paninbot.data.models.JsonMapper
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate

@Repository
class CurrencyExchangeRateRepository(
        val restTemplate: RestTemplate,
        val baseUrl: String
) : CurrencyRepository {
    override suspend fun getRates(baseCurrency: String) = restTemplate.getForObject<String>(
            "$baseUrl/latest?base=$baseCurrency", String::class.java
    )?.let {
        JsonMapper.mapCurrencyRatesFromJson(it)
    } ?: emptyList()
}