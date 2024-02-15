package me.neon.mail.libs.taboolib.command.component

import me.neon.mail.libs.taboolib.command.CommandContext
import org.bukkit.command.CommandSender

class CommandExecutor(bind: Class<CommandSender>,
    val function: (sender: CommandSender, context: CommandContext, argument: String) -> Unit
) : CommandBinder(bind) {

    @Suppress("UNCHECKED_CAST")
    fun exec(commandBase: CommandBase, context: CommandContext, argument: String) {
        val sender = cast(context)
        if (sender != null) {
            function.invoke(sender, context.copy(sender = sender), argument)
        } else {
            commandBase.commandIncorrectSender.exec(context, 0, 0)
        }
    }
}