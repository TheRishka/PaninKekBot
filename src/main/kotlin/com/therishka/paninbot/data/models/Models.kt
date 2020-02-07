package com.therishka.paninbot.data.models

data class User(val id: Long, val chat: Chat, val name: String?, val nickname: String?)

data class Chat(val id: Long, val name: String?)