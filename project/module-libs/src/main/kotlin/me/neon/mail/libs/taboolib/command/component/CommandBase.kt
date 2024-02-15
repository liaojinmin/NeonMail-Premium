package me.neon.mail.libs.taboolib.command.component

import me.neon.mail.libs.taboolib.command.CommandContext
import me.neon.mail.libs.utils.subList
import org.bukkit.command.CommandSender


@Suppress("DuplicatedCode")
class CommandBase : CommandComponent(-1, false) {

    internal var result = true

    internal var commandIncorrectSender: CommandUnknownNotify =
        CommandUnknownNotify(CommandSender::class.java) { sender, _, _, _ ->
            sender.sendMessage("§cIncorrect sender for command")
        }

    internal var commandIncorrectCommand: CommandUnknownNotify =
        CommandUnknownNotify(CommandSender::class.java) { _, context, index, state ->
            val args = subList(context.realArgs.toList(), 0, index)
            var str = context.name
            if (args.size > 1) {
                str += " "
                str += subList(args, 0, args.size - 1).joinToString(" ").trim()
            }
            if (str.length > 10) {
                str = "...${str.substring(str.length - 10, str.length)}"
            }
            if (args.isNotEmpty()) {
                str += " "
                str += "§c§n${args.last()}"
            }

            when (state) {
                1 -> context.sender.sendMessage("§cUnknown or incomplete command, see below for error")
                2 -> context.sender.sendMessage("§cIncorrect argument for command")
            }
            context.sender.sendMessage("§7$str§r§c§o<--[未知命令]")
        }

    fun execute(context: CommandContext): Boolean {
        result = true
        // 空参数是一种特殊的状态，指的是玩家输入根命令且不附带任何参数，例如 [/test] 而不是 [/test ]
        if (context.realArgs.isEmpty()) {
            // 获取下级节点
            val children = findChildren(context)
            // 下级节点为空 || 下级节点存在可选（optional）|| 当前节点存在执行器
            return if (children.isEmpty() || children.any { it.optional } || commandExecutor != null) {
                context.index = 0
                // 缺少 execute 代码块
                if (commandExecutor == null) {
                    context.sender.sendMessage("§cEmpty command (no executor).")
                } else {
                    commandExecutor!!.exec(this, context, "")
                }
                result
            } else {
                commandIncorrectCommand.exec(context, -1, 1)
                false
            }
        }
        fun process(cur: Int, component: CommandComponent): Boolean {
            // 更新参数内容
            context.index = cur
            context.currentComponent = component
            // 检索节点
            val find = component.findChildren(context, context.realArgs[cur])
            return if (find != null) {
                // 获取下级节点
                val children = find.findChildren(context)
                // 存在下级输入参数 && 下级节点有效
                if (cur + 1 < context.realArgs.size && children.isNotEmpty()) {
                    process(cur + 1, find)
                } else {
                    // 下级节点为空 || 下级节点存在可选（optional）|| 当前节点存在执行器
                    if (children.isEmpty() || children.any { it.optional } || find.commandExecutor != null) {
                        context.currentComponent = find
                        // 缺少 execute 代码块
                        if (find.commandExecutor == null) {
                            context.sender.sendMessage("§cEmpty command (no executor).")
                        } else {
                            find.commandExecutor!!.exec(this, context, context.self())
                        }
                        result
                    } else {
                        commandIncorrectCommand.exec(context, cur + 1, 1)
                        false
                    }
                }
            } else {
                commandIncorrectCommand.exec(context, cur + 1, 2)
                false
            }
        }
        return process(0, this)
    }

    fun suggest(context: CommandContext): List<String>? {
        // 空参数不需要触发补全机制
        if (context.realArgs.isEmpty()) {
            return null
        }
        fun process(cur: Int, component: CommandComponent): List<String>? {
            context.index = cur
            context.currentComponent = component
            // 获取当前输入参数
            val args = context.realArgs[cur]
            // 检索节点
            val find = component.findChildren(context, args)
            if (find != null) {
                context.currentComponent = find
            }
            return when {
                find != null && cur + 1 < context.realArgs.size -> {
                    process(cur + 1, find)
                }
                cur + 1 == context.realArgs.size -> {
                    val suggest = component.findChildren(context).flatMap {
                        when (it) {
                            is CommandComponentLiteral -> if (it.hidden) emptyList() else it.aliases.toList()
                            is CommandComponentDynamic -> it.commandSuggestion?.exec(context) ?: emptyList()
                            else -> emptyList()
                        }
                    }
                    suggest.filter { args.isEmpty() || it.startsWith(args, ignoreCase = true) }.ifEmpty { null }
                }
                else -> null
            }
        }
        return process(0, this)
    }

    fun incorrectSender(function: (sender: CommandSender, context: CommandContext) -> Unit) {
        this.commandIncorrectSender = CommandUnknownNotify(CommandSender::class.java) { sender, context, _, _ -> function(sender, context) }
    }

    fun incorrectCommand(function: (sender: CommandSender, context: CommandContext, index: Int, state: Int) -> Unit) {
        this.commandIncorrectCommand = CommandUnknownNotify(CommandSender::class.java, function)
    }

    fun setResult(value: Boolean) {
        result = value
    }
}