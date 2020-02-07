package com.therishka.paninbot.data

import com.therishka.paninbot.data.models.Chat
import com.therishka.paninbot.data.models.RatingChange
import com.therishka.paninbot.data.models.User
import org.joda.time.DateTime

interface UsersRepository {

    fun addUser(user: User)

    fun getUsers(chat: Chat, activeOnly: Boolean = false): List<User>

    fun addChat(chat: Chat)

    /**
     *
     */
    fun updateUserStatus(user: User, isActive: Boolean): Boolean

    fun changeUserRating(userAuthor: User, userTarget: User, ratingChange: RatingChange): Int

    fun getLastChangeUserRatingDate(userAuthor: User, userTarget: User): DateTime?
}