package me.neon.mail.service.sql

import com.zaxxer.hikari.HikariDataSource
import me.neon.mail.service.sql.ConfigSql
import me.neon.mail.service.sql.SqlService
import java.io.File
import java.sql.Connection

/**
 * 作者: 老廖
 * 时间: 2022/10/16
 *
 **/
class Sqlite(
    private val config: ConfigSql
): SqlService() {
    override var isActive: Boolean = false
    private val dataSource by lazy {
        HikariDataSource().apply {
            maximumPoolSize = config.hikari_settings.maximum_pool_size
            minimumIdle = config.hikari_settings.minimum_idle
            maxLifetime = config.hikari_settings.maximum_lifetime.toLong()
            keepaliveTime = config.hikari_settings.keepalive_time.toLong()
            connectionTimeout = config.hikari_settings.connection_timeout.toLong()
            poolName = "NeonMail-SQLITE"
        }
    }

    override fun getConnection(): Connection {
        return this.dataSource.connection
    }

    override fun onStart() {
        val url = "jdbc:sqlite:" + config.sqlite + File.separator + "data.db"
        dataSource.dataSourceClassName = "org.sqlite.SQLiteDataSource"
        dataSource.addDataSourceProperty("url", url)
        isActive = testSql()
    }

    override fun onClose() {
        this.dataSource.close()
    }
}