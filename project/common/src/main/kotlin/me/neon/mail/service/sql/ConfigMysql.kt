package me.neon.mail.service.sql

/**
 * 作者: 老廖
 * 时间: 2022/10/12
 *
 **/
data class ConfigMysql(
    val host: String = "127.0.0.1",
    val port: Int = 3306,
    val database: String = "server_mail",
    val username: String = "root",
    val password: String = "123456",
    val params: String = "?autoReconnect=true&useSSL=false"
)