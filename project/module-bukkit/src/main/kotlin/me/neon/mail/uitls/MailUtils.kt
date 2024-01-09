package me.neon.mail.uitls

import me.neon.mail.api.IMail
import me.neon.mail.IMailRegister
import me.neon.mail.SetTings
import me.neon.mail.api.IMailAbstract
import org.bukkit.Bukkit
import org.jetbrains.annotations.NotNull
import java.text.SimpleDateFormat

/**
 * NeonMail-Premium
 * me.neon.mail.uitls
 *
 * @author 老廖
 * @since 2024/1/5 19:10
 */
val format = SimpleDateFormat("yyyy年 MM月 dd日 HH:mm:ss")

val TYPE by lazy { Regex("(\\{|\\[)(type|种类)(}|])") }

val SENDER by lazy { Regex("(\\{|\\[)(sender|发送者)(}|])") }

val SENDER_TIME by lazy { Regex("(\\{|\\[)(senderTime|发送时间)(}|])") }

val GET_TIME by lazy { Regex("(\\{|\\[)(getTime|领取时间)(}|])") }

val TEXT by lazy { Regex("(\\{|\\[)(text|文本)(}|])") }

val STATE by lazy { Regex("(\\{|\\[)(state|状态)(}|])") }

val ITEM by lazy { Regex("(\\{|\\[)(item|附件)(}|]) }") }

val EXPIRE by lazy { Regex("(\\{|\\[)(expire|到期时间)(}|])") }


val replacements: Map<Regex, (IMail<*>) -> String> by lazy {
    mapOf(
        TYPE to { mail -> mail.mailType },
        SENDER to { mail -> if (mail.sender == IMailRegister.console) "系统" else Bukkit.getOfflinePlayer(mail.sender).name!! },
        SENDER_TIME to { mail -> format.format(mail.senderTimer) },
        GET_TIME to { mail -> if (mail.collectTimer < 1000) "未领取" else format.format(mail.collectTimer) },
        TEXT to { mail -> mail.context },
        STATE to { mail -> mail.state.state },
        ITEM to { mail -> (mail as IMailAbstract<*>).getAppendixInfo() },
        EXPIRE to { mail ->
            if (SetTings.getExpiryTimer() != -1L) { format.format(mail.senderTimer + SetTings.getExpiryTimer()) } else ""
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


