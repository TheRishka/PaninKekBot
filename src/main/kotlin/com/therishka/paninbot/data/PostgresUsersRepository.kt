package com.therishka.paninbot.data

import com.therishka.paninbot.data.models.Chat
import com.therishka.paninbot.data.models.RatingChange
import com.therishka.paninbot.data.models.User
import com.therishka.paninbot.data.schema.Chats
import com.therishka.paninbot.data.schema.Users
import com.therishka.paninbot.data.schema.Users2Chats
import com.therishka.paninbot.data.schema.insertOrUpdate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PostgresUsersRepository(final val db: Database) : UsersRepository {

    private final val logger = LoggerFactory.getLogger(PostgresUsersRepository::class.java)

    init {
        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(Users, Chats, Users2Chats)
        }
    }

    override fun addUser(user: User) {
        transaction(db) {
//            addLogger(StdOutSqlLogger)
//            val addedUserId = Users.insertOrUpdate(Users.id) {
//                it[name] = user.name ?: "Плотва"
//                it[id] = user.id
//            } get Users.id
//            val addedUsers2Chat = Users2Chats.insertOrUpdate(
//                    Users2Chats.chat_id,
//                    Users2Chats.user_id
//            ) {
//                it[chat_id] = user.chat.id
//                it[user_id] = user.id
//            } get Users2Chats.chat_id
//            print("Added userId == $addedUserId, added Users2Chat Chat_id = $addedUsers2Chat")
        }
    }

    override fun getUsers(chat: Chat, activeOnly: Boolean): List<User> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addChat(chat: Chat) {
        transaction(db) {
            addLogger(StdOutSqlLogger)
            val addedChatId = Chats.insertOrUpdate(
                    Chats.id) {
                it[name] = chat.name ?: "Fuck"
                it[id] = chat.id
            } get Chats.id
            logger.info("Added chatId == $addedChatId")
        }
    }

    override fun updateUserStatus(user: User, isActive: Boolean): Boolean {
        return transaction(db) {
            addLogger(StdOutSqlLogger)
            val currentStatus = Users2Chats
                    .slice(Users2Chats.active)
                    .selectUserForSpecificChat(userId = user.id, chatId = user.chat.id)
                    .withDistinct().map {
                        it[Users2Chats.active]
                    }.first()
            println("DEBUG! CURRENT STATUS = $currentStatus ")
            val debugUsersData = Users2Chats.selectAll().withDistinct().map {
                listOf(
                        it[Users2Chats.user_id],
                        it[Users2Chats.chat_id],
                        it[Users2Chats.active])
            }
            println("DEBUG! $debugUsersData ")
            if (currentStatus == isActive) {
                false
            } else {
                Users2Chats.update({
                    Users2Chats.user_id eq user.id and (Users2Chats.chat_id eq user.chat.id)
                }) {
                    it[active] = isActive
                }
                logger.info("Updated userId: ${user.id}, set to isActive = $isActive")
                true
            }
        }
    }

    override fun changeUserRating(user: User, ratingChange: RatingChange): Int {
        return transaction(db) {
            addLogger(StdOutSqlLogger)
            val currentRating = Users2Chats
                    .slice(Users2Chats.rating)
                    .select(Users2Chats.user_id eq user.id and (Users2Chats.chat_id eq user.chat.id))
                    .withDistinct().map {
                        it[Users2Chats.rating]
                    }.first()
            val newRating = when (ratingChange) {
                RatingChange.INCREMENT -> {
                    currentRating + 1
                }
                RatingChange.DECREMENT -> {
                    currentRating - 1
                }
            }
            val updatedResult = Users2Chats.update({
                Users2Chats.user_id eq user.id and (Users2Chats.chat_id eq user.chat.id)
            }) {
                it[rating] = newRating
            }
            logger.info("Updated user (id: ${user.id}) rating, did the ${ratingChange.name} new " +
                    "rating " +
                    "is: $newRating")
            newRating
        }
    }

    fun FieldSet.selectUserForSpecificChat(chatId: Long, userId: Long) =
            select(Users2Chats.user_id eq userId and (Users2Chats.chat_id eq chatId))
}