package me.neon.mail.mail

import me.neon.mail.Settings
import org.bukkit.Bukkit
import org.jetbrains.annotations.NotNull
import taboolib.module.chat.colored
import java.text.SimpleDateFormat

/**
 * NeonMail-Premium
 * me.neon.mail.utils
 *
 * @author 老廖
 * @since 2024/1/5 19:10
 */

object MailUtils {

    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    val TYPE = Regex("(\\{|\\[)(type|种类)(}|])")

    val SENDER = Regex("(\\{|\\[)(sender|发送者)(}|])")

    val SENDER_TIME = Regex("(\\{|\\[)(senderTime|发送时间)(}|])")

    val GET_TIME = Regex("(\\{|\\[)(getTime|领取时间)(}|])")

    val TEXT = Regex("(\\{|\\[)(text|文本)(}|])")

    val STATE = Regex("(\\{|\\[)(state|状态)(}|])")

    val ITEM = Regex("(\\{|\\[)(item|附件)(}|])")

    val EXPIRE = Regex("(\\{|\\[)(expire|到期时间)(}|])")

    val replacements: Map<Regex, (IMail<*>) -> String> by lazy {
        mapOf(
            TYPE to { Settings.typeTranslate[it.translateType]?.colored() ?: it.translateType },
            SENDER to { if (it.sender == MailRegister.console) Settings.typeTranslate["系统"]?.colored() ?: "系统" else Bukkit.getOfflinePlayer(it.sender).name!! },
            SENDER_TIME to { format.format(it.senderTimer) },
            GET_TIME to {
                if (it.collectTimer < 1000) {
                    "0"
                } else {
                    format.format(it.collectTimer)
                }
            },
            TEXT to { it.context },
            STATE to {
                Settings.typeTranslate[it.state.type]?.colored() ?: it.state.type

            },
            ITEM to { it.data.getAllAppendixInfo() },
            EXPIRE to {
                if (Settings.getExpiryTimer() != -1L) { format.format(it.senderTimer + Settings.getExpiryTimer()) } else ""
            }
        )
    }
}


fun IMail<*>.parseMailSenderTimer(): String {
    return MailUtils.format.format(senderTimer)
}

fun IMail<*>.parseDataName(): String {
    return data::class.java.simpleName
}
fun IMailData.parseDataName(): String {
    return this::class.java.simpleName
}

fun IMail<*>.parseMailInfo(@NotNull lore: List<String>): List<String> {
    val list = mutableListOf<String>()
    lore.forEach { line ->
        var modifiedLine = line
        for ((regex, replacement) in MailUtils.replacements) {
            modifiedLine = regex.replace(modifiedLine) {
                replacement(this)
            }
        }
        list.addAll(modifiedLine.split(";"))
    }
    return list
}






