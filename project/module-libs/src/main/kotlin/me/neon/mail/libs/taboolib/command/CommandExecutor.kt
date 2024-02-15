package me.neon.mail.libs.taboolib.command

import org.bukkit.command.CommandSender

/**
 * TabooLib
 * taboolib.common.CommandExecutor
 *
 * @author sky
 * @since 2021/6/24 11:49 下午
 */
interface CommandExecutor {

    fun execute(sender: CommandSender, command: CommandStructure, name: String, args: Array<String>): Boolean
}