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

@Suppress("RedundantModalityModifier")
@Repository
class PostgresUsersRepository(private final val db: Database) : UsersRepository {

    companion object {
        fun userInChat(userId: Long, chatId: Long) = Users2Chats.user_id eq userId and (Users2Chats
                .chat_id eq chatId)
    }

    private final val logger = LoggerFactory.getLogger(PostgresUsersRepository::class.java)

    init {
        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(Users, Chats, Users2Chats)
        }
    }

    override fun addUser(user: User) {
        transaction(db) {
            addLogger(StdOutSqlLogger)
            val userId = user.id
            val chatId = user.chat.id
            val addedUserId = Users.insertOrUpdate(Users.id) {
                it[name] = user.name ?: "Плотва"
                it[id] = user.id
            } get Users.id
            val users2ChatsValuesCount = Users2Chats.selectUserForSpecificChat(
                    userId = userId,
                    chatId = chatId).count()
            var logMessageForUsers2Chat = "Did not add Users2Chat values"
            if (users2ChatsValuesCount == 0) {
                val addedUsers2ChatsUserId = Users2Chats.insert {
                    it[chat_id] = chatId
                    it[user_id] = userId
                } get Users2Chats.user_id
                logMessageForUsers2Chat = "Added Users2Chat value for $addedUsers2ChatsUserId"
            }
            logger.info("Added userId == $addedUserId, $logMessageForUsers2Chat")
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
            if (currentStatus == isActive) {
                false
            } else {
                val updateResult = Users2Chats.update({
                    Users2Chats.user_id eq user.id and (Users2Chats.chat_id eq user.chat.id)
                }) {
                    it[active] = isActive
                }
                logger.info("UpdateUserStatus: Affected columns: $updateResult")
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
                    .selectUserForSpecificChat(userId = user.id, chatId = user.chat.id)
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
                RatingChange.UNKNOWN -> {
                    logger.info("ChangeUserRating: UNKNOWN RATING CHANGE ACTION!")
                    currentRating
                }
            }
            val updateResult = Users2Chats.update({
                userInChat(user.id, user.chat.id)
            }) {
                it[rating] = newRating
            }
            logger.info("ChangeUserRating: Affected columns: $updateResult")
            logger.info("Updated user (id: ${user.id}) rating," +
                    " did the ${ratingChange.name} new rating is: $newRating")
            newRating
        }
    }

    fun FieldSet.selectUserForSpecificChat(chatId: Long, userId: Long) =
            select(userInChat(userId, chatId))
}