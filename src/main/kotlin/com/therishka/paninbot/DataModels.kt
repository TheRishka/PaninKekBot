package com.therishka.paninbot

data class User(val id: Long, val chat: Chat, val name: String?, val nickname: String?) {

    fun getGeneralName(mention: Boolean = true): String {
        return if (mention) {
            if (nickname != null) {
                "@$nickname"
            } else {
                "<a href=\"tg://user?id=$id\">$name</a>"
            }
        } else {
            name ?: "хуй знает кто"
        }
    }
}

data class Chat(val id: Long, val name: String?)