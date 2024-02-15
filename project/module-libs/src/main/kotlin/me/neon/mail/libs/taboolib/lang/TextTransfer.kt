package me.neon.mail.libs.taboolib.lang

import org.bukkit.command.CommandSender

/**
 * TabooLib
 * me.neon.mail.libs.lang.TextTransfer
 *
 * @author sky
 * @since 2021/6/20 11:07 下午
 */
interface TextTransfer {

    fun translate(sender: CommandSender, source: String, vararg args: Any): String
}