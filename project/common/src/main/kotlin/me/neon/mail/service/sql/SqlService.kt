package me.neon.mail.service.sql

import java.sql.Connection
import java.sql.SQLException

/**
 * 作者: 老廖
 * 时间: 2022/10/16
 *
 **/
abstract class SqlService {

    abstract var isActive: Boolean


    abstract fun getConnection(): Connection

    abstract fun onStart()

    abstract fun onClose()


    fun createTab(func: SqlService.() -> Unit) {
        try {
            func(this)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
    fun testSql(): Boolean {
        return try {
            getConnection()
            true
        } catch (_: SQLException) {
            false
        }
    }
}