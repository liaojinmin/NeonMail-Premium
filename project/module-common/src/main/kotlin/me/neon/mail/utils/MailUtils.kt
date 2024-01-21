package me.neon.mail.utils

import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IMailRegister
import me.neon.mail.api.mail.IMail
import me.neon.mail.api.mail.IMailAbstract
import org.bukkit.Bukkit
import org.jetbrains.annotations.NotNull
import java.text.SimpleDateFormat

/**
 * NeonMail-Premium
 * me.neon.mail.utils
 *
 * @author 老廖
 * @since 2024/1/5 19:10
 */
val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

val TYPE by lazy { Regex("(\\{|\\[)(type|种类)(}|])") }

val SENDER by lazy { Regex("(\\{|\\[)(sender|发送者)(}|])") }

val SENDER_TIME by lazy { Regex("(\\{|\\[)(senderTime|发送时间)(}|])") }

val GET_TIME by lazy { Regex("(\\{|\\[)(getTime|领取时间)(}|])") }

val TEXT by lazy { Regex("(\\{|\\[)(text|文本)(}|])") }

val STATE by lazy { Regex("(\\{|\\[)(state|状态)(}|])") }

val ITEM by lazy { Regex("(\\{|\\[)(item|附件)(}|])") }

val EXPIRE by lazy { Regex("(\\{|\\[)(expire|到期时间)(}|])") }


val replacements: Map<Regex, (IMail<*>) -> String> by lazy {
    mapOf(
        TYPE to { NeonMailLoader.typeTranslate[it.mailType] ?: it.mailType },
        SENDER to { if (it.sender == IMailRegister.console) NeonMailLoader.typeTranslate["系统"] ?: "系统" else Bukkit.getOfflinePlayer(it.sender).name!! },
        SENDER_TIME to { format.format(it.senderTimer) },
        GET_TIME to { if (it.collectTimer < 1000) NeonMailLoader.typeTranslate["未提取"] ?: "未提取" else format.format(it.collectTimer) },
        TEXT to { it.context },
        STATE to { NeonMailLoader.typeTranslate[it.state.state] ?: it.state.state },
        ITEM to { (it as IMailAbstract<*>).data.getAppendixInfo() },
        EXPIRE to {
            if (NeonMailLoader.getExpiryTimer() != -1L) { format.format(it.senderTimer + NeonMailLoader.getExpiryTimer()) } else ""
        }
    )
}

fun IMail<*>.parseMailInfo(@NotNull lore: List<String>): List<String> {
    val list = mutableListOf<String>()
    lore.forEach { line ->
        var modifiedLine = line
        for ((regex, replacement) in replacements) {
            modifiedLine = regex.replace(modifiedLine) {
                replacement(this)
            }
        }
        list.addAll(modifiedLine.split(";"))
    }
    return list
}






