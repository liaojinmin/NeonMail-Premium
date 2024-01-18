package me.neon.mail.service.channel

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
)