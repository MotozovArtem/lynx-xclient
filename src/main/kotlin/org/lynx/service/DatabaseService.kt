package org.lynx.service

import com.google.inject.Singleton
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.lynx.domain.AbonentKeys
import org.lynx.domain.Keys
import org.lynx.domain.Message
import org.lynx.domain.Messages
import org.lynx.domain.User
import org.lynx.domain.Users
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.time.LocalDateTime

interface DatabaseService {

    fun generateDatabase(debug: Boolean)

}

@Singleton
class DatabaseServiceImpl : DatabaseService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(DatabaseServiceImpl::class.java)
    }

    init {
        Database.connect("jdbc:sqlite:lynx.db", driver = "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        generateDatabase(System.getProperty("lynx.debug").toBoolean())
    }

    override fun generateDatabase(debug: Boolean) {
        transaction {
            addLogger(Slf4jSqlDebugLogger)
            SchemaUtils.createMissingTablesAndColumns(Users, Messages, Keys, AbonentKeys)
        }
        log.info("Generated lynx.db file")
    }

}