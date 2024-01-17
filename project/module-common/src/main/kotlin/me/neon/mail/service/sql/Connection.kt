package me.neon.mail.service.sql

import java.sql.*
import java.util.UUID


/**
 * 作者: 老廖
 * 时间: 2022/9/14
 *
 **/
fun <T: ResultSet, R> T.get(func: (T) -> R): R {
    return try {
        func(this)
    } catch (ex: Exception) {
        throw ex
    } finally {
        close()
    }
}

fun <T> Connection.use(func: Connection.() -> T): T {
    return try {
        func(this)
    } catch (ex: Exception) {
        throw ex
    } finally {
        close()
    }
}

fun <T: Statement, R> T.action(func: (T) -> R) {
    try {
        func(this)
    } catch (ex: Exception) {
        throw ex
    } finally {
        close()
    }
}

fun <T: PreparedStatement, R> T.action(func: (T) -> R) {
    try {
        func(this)
    } catch (ex: Exception) {
        throw ex
    } finally {
        close()
    }
}

fun PreparedStatement.setUUID(index: Int, uuid: UUID) {
    setString(index, uuid.toString())
}

fun PreparedStatement.setStringList(index: Int, list: List<String>, separator: String = ";") {
    setString(index, list.joinToString(separator, "", ""))
}

fun ResultSet.getUUID(key: String): UUID {
    return UUID.fromString(getString(key))
}

fun ResultSet.getStringList(key: String, separator: String = ";"): List<String> {
    return getString(key).split(separator)
}