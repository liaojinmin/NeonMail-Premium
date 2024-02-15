package me.neon.mail.libs.taboolib.command.component

import me.neon.mail.libs.taboolib.command.CommandContext
import org.bukkit.command.CommandSender

abstract class CommandBinder(val bind: Class<CommandSender>) {

    @Suppress("UNCHECKED_CAST")
    fun cast(context: CommandContext): CommandSender? {
        val sender = context.sender
        return when {
            bind.isInstance(sender) -> sender
            else -> null
        }
    }
}