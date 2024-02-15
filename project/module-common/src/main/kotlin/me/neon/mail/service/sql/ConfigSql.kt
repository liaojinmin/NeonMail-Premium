package me.neon.mail.service.sql

import me.neon.mail.libs.NeonLibsLoader
import org.bukkit.configuration.ConfigurationSection
import java.io.File

/**
 * 作者: 老廖
 * 时间: 2022/10/16
 *
 **/
data class ConfigSql(
    val use_type: String = "sqlite",
    val mysql: ConfigMysql = ConfigMysql(),
    val hikari_settings: ConfigHikari = ConfigHikari(),
    val sqlite: File
) {
    companion object {
        fun loader(section: ConfigurationSection): ConfigSql {
            val type = section.getString("use_type") ?: "sqlite"
            if (type.equals("mysql", true)) {
                val mysql = ConfigMysql(
                    section.getString("mysql.host") ?: "127.0.0.1",
                    section.getInt("mysql.port", 3306),
                    section.getString("mysql.database") ?: throw RuntimeException("缺少 mysql 库名设置"),
                    section.getString("mysql.username") ?: throw RuntimeException("缺少 mysql 用户名设置"),
                    section.getString("mysql.password") ?: throw RuntimeException("缺少 mysql 用户密码设置"),
                    section.getString("mysql.params") ?: "",
                )
                val hikari = ConfigHikari(
                    section.getInt("hikari_settings.maximum_pool_size", 10),
                    section.getInt("hikari_settings.minimum_idle", 10),
                    section.getInt("hikari_settings.maximum_lifetime", 1800000),
                    section.getInt("hikari_settings.keepalive_time", 0),
                    section.getInt("hikari_settings.connection_timeout", 5000),
                )
                return ConfigSql(type, mysql, hikari, NeonLibsLoader.pluginId.dataFolder)
            }
            return ConfigSql(type, sqlite = NeonLibsLoader.pluginId.dataFolder)
        }

    }
}
