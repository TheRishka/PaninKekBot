package com.therishka.paninbot

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TestsPlayground {

    @Test
    fun randomTest() {
        val text1 = "Привет как дела что делаешь?"
        val text2 = "Привет! как"
        val text3 = "Как Привет?Дела"
        val text4 = "привет"
        val text5 = " как?"
        val text6 = "?? ????? kak??"
        val testCases = listOf(
                text1,
                text2,
                text3,
                text4,
                text5,
                text6
        )
        testCases.forEach {
            val reversed = reverseSentence(it)
            println("The original was $it || and the reversed is: $reversed")
        }
    }

    private fun reverseSentence(sentenceToReverse: String): String {
        val splitted = sentenceToReverse.split(" ")
        val reversed = StringBuilder()
        return if (splitted.size > 1) {
            splitted.asReversed().forEach {
                reversed.append(it).append(" ")
            }
            reversed.toString()
        } else {
            sentenceToReverse
        }
    }
}