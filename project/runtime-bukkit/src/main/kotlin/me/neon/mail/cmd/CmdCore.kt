package me.neon.mail.cmd

import me.neon.mail.NeonMailLoader
import me.neon.mail.ServiceManager
import me.neon.mail.ServiceManager.getPlayerData
import me.neon.mail.menu.MenuLoader
import me.neon.mail.scheduler.SchedulerLoader
import me.neon.mail.template.TemplateManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.getProxyPlayer
import taboolib.module.chat.ComponentText
import taboolib.platform.util.sendLang
import java.util.*


@CommandHeader(name = "NeonMail", aliases = ["nm", "nmp"], permissionDefault = PermissionDefault.TRUE )
object CmdCore {

    @CommandBody
    val main = mainCommand {
        execute { sender, _, _ ->
            createHelp(sender)
        }
    }

    @CommandBody(permission = "neonMail.command.admin")
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            NeonMailLoader.loaderSettings()
            TemplateManager.loaderTemplate()
            MenuLoader.loaderMenu()
            SchedulerLoader.loader()
            sender.sendMessage("已重新加载...")
        }
    }

    @CommandBody(permission = "neonMail.command.box")
    val box = CmdMenu.command

    @CommandBody //(permission = "neonMail.command.bind")
    val bind = subCommand {
        dynamic("验证码") {
            execute<CommandSender> { sender, context, _ ->
                val code = context["验证码"]
                if (sender is Player) {
                    ServiceManager.bindCode[sender.uniqueId]?.let {
                        val text = it.split(";")
                        if (code == text[0]) {
                            sender.getPlayerData()?.let {  data ->
                                data.mail = text[1]
                                ServiceManager.bindCode.remove(sender.uniqueId)
                                sender.sendLang("玩家-邮箱绑定-成功")
                                ServiceManager.savePlayerData(sender.uniqueId, false)
                            }
                        }
                    }
                }
            }
        }
    }

    @CommandBody(permission = "neonMail.command.template")
    val template = subCommand {
        dynamic("uniqueId") {
            suggestion<CommandSender>(false) { _, _ -> TemplateManager.getTemplatePackKeys().toList() }

            dynamic("player") {
                suggestion<CommandSender>(uncheck = true) {_, _ ->
                    Bukkit.getOnlinePlayers().map { it.name }
                }
                execute<CommandSender> { sender, context, _ ->
                    TemplateManager.getTemplatePack(context["uniqueId"])?.let {
                        it.sendToPlayer(Bukkit.getOfflinePlayer(context["player"]))
                    } ?: sender.sendMessage("找不到这个模板...")
                }
            }
        }
    }

    @CommandBody(permission = "neonMail.command.box.admin")
    val adminBox = subCommand {
        dynamic("种类") {
            suggestion<CommandSender>(false) { _, _ -> listOf("senderBox", "receiveBOX", "editeBox") }
            execute<CommandSender> { sender, context, _ ->
                if (sender is Player) {
                    CmdMenu.openMenu(sender, context["种类"], true)
                }
            }
        }
    }


    private fun createHelp(sender: CommandSender) {
        sender.sendMessage("")
        ComponentText.empty()
            .append("  ").append("§bNeon§9Mail§8-§ePremium")
            .hoverText("§7现代化邮件系统 By 老廖")
            .append(" ").append("§f1.0.1 §e高级版")
            .hoverText("""
                §7插件版本: §f1.0.1
            """.trimIndent()).sendTo(adaptCommandSender(sender))
        sender.sendMessage("")
        sender.sendMessage("  §7指令: §f/nmp §8[...]")
        if (sender.hasPermission("neonMail.command.admin")) {
            sender.sendLang("CMD-HELP-ADMIN")
        }
        sender.sendLang("CMD-HELP-PLAYER")
    }
}