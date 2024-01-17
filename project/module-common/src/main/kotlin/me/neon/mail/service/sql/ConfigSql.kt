package me.neon.mail.service.sql

import taboolib.common.platform.function.getDataFolder
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
) {
    var sqlite: File = getDataFolder()
}
