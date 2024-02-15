package me.neon.mail.libs.taboolib.command.component

import me.neon.mail.libs.taboolib.command.CommandContext
import org.bukkit.command.CommandSender

class CommandUnknownNotify(bind: Class<CommandSender>, val function: (sender: CommandSender, context: CommandContext, index: Int, state: Int) -> Unit) : CommandBinder(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(context: CommandContext, index: Int, state: Int) {
        val sender = cast(context)
        if (sender != null) {
            function.invoke(sender, context.copy(sender = sender), index, state)
        }
    }
}