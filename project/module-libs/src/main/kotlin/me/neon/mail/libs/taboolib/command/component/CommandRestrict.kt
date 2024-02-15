package me.neon.mail.libs.taboolib.command.component

import me.neon.mail.libs.taboolib.command.CommandContext
import org.bukkit.command.CommandSender

class CommandRestrict(bind: Class<CommandSender>, val function: (sender: CommandSender, context: CommandContext, argument: String) -> Boolean) : CommandBinder(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(context: CommandContext, argument: String): Boolean? {
        val sender = cast(context)
        return if (sender != null) {
            function.invoke(sender, context.copy(sender = sender), argument)
        } else {
            null
        }
    }
}