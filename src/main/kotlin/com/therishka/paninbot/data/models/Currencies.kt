package com.therishka.paninbot.data.models

import org.json.JSONException
import org.json.JSONObject

data class Rate(
        val shortName: String,
        val value: Double
)

object JsonMapper {
    fun mapCurrencyRatesFromJson(inputJson: String?): List<Rate> {
        val reader = try {
            JSONObject(inputJson)
        } catch (exception: JSONException) {
            null
        }
        return reader?.let {
            val rates = it.getJSONObject("rates")
            val parsedRates = mutableListOf<Rate>()
            rates.keys().forEach { key ->
                parsedRates.add(Rate(key, rates.getDouble(key)))
            }
            parsedRates
        } ?: emptyList()
    }
}