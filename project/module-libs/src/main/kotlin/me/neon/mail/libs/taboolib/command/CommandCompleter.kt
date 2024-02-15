package me.neon.mail.libs.taboolib.command

import org.bukkit.command.CommandSender


/**
 * TabooLib
 * taboolib.common.CommandTabCompleter
 *
 * @author sky
 * @since 2021/6/24 11:49 下午
 */
interface CommandCompleter {

    fun execute(sender: CommandSender, command: CommandStructure, name: String, args: Array<String>): List<String>?
}