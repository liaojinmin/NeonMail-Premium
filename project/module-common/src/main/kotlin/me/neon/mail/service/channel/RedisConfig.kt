package me.neon.mail.service.channel

import org.bukkit.configuration.ConfigurationSection

/**
 * 作者: 老廖
 * 时间: 2022/10/16
 *
 **/
data class RedisConfig(
    val use: Boolean = false,
    val host: String = "",
    val port: Int = 0,
    val password: String = "",
    val ssl: Boolean = false
) {
    companion object {
        fun loader(section: ConfigurationSection): RedisConfig {
            return RedisConfig(
                section.getBoolean("use"),
                section.getString("host") ?: "127.0.0.1",
                section.getInt("port", 6379),
                section.getString("password") ?: "",
                section.getBoolean("ssl")
            )
        }
    }
}