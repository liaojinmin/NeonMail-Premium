package me.neon.mail.libs.taboolib.command.component

import me.neon.mail.libs.taboolib.command.CommandContext
import org.bukkit.command.CommandSender

class CommandComponentDynamic(val comment: String, index: Int, optional: Boolean, permission: String) : CommandComponent(index, optional, permission) {

    internal var commandRestrict: CommandRestrict? = null
    internal var commandSuggestion: CommandSuggestion? = null

    /**
     * 创建当前节点下的命令建议约束（自动取消建议）
     */
    fun restrict(bind: Class<CommandSender>, function: (sender: CommandSender, context: CommandContext, argument: String) -> Boolean): CommandComponentDynamic {
        this.commandRestrict = CommandRestrict(bind, function)
        this.commandSuggestion = null
        return this
    }

    /**
     * 创建当前节点下的命令建议（自动取消约束）
     */
    fun suggestion(bind: Class<CommandSender>, uncheck: Boolean = false, function: (sender: CommandSender, context: CommandContext) -> List<String>?): CommandComponentDynamic {
        this.commandSuggestion = CommandSuggestion(bind, uncheck, function)
        this.commandRestrict = null
        return this
    }

    /**
     * 创建当前节点下的命令建议约束
     */
    fun restrict(function: (sender: CommandSender, context: CommandContext, argument: String) -> Boolean): CommandComponentDynamic {
        return restrict(CommandSender::class.java, function)
    }

    /**
     * 创建当前节点下的命令建议
     */
    fun suggestion(uncheck: Boolean = false, function: (sender: CommandSender, context: CommandContext) -> List<String>?): CommandComponentDynamic {
        return suggestion(CommandSender::class.java, uncheck, function)
    }

    /**
     * 创建当前节点下的不约束命令建议
     */
    fun suggestionUncheck(function: (sender: CommandSender, context: CommandContext) -> List<String>?): CommandComponentDynamic {
        return suggestion(CommandSender::class.java, true, function)
    }

    /**
     * 解除约束
     */
    fun removeRestrict(): CommandComponentDynamic {
        this.commandRestrict = null
        return this
    }

    /**
     * 解除建议
     */
    fun removeSuggestion(): CommandComponentDynamic {
        this.commandSuggestion = null
        return this
    }

    override fun toString(): String {
        return "CommandComponentDynamic(comment='$comment')"
    }
}