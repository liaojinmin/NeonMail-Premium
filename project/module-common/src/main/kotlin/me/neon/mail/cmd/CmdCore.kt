package me.neon.mail.cmd

import me.neon.mail.libs.taboolib.chat.ChatLoader
import me.neon.mail.libs.taboolib.command.*
import me.neon.mail.libs.taboolib.lang.sendLang
import org.bukkit.command.CommandSender


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
        execute { sender, _, _ ->
           createHelp(sender)
        }
    }

    @CommandBody(permission = "neonMail.command.box")
    val box = CmdMenu.command

    @CommandBody(permission = "neonMail.command.box.admin")
    val adminBox = CmdAdminMenu.command


    private fun createHelp(sender: CommandSender) {
        sender.sendMessage("")
        ChatLoader.empty()
            .append("  ").append("§bNeon§9Mail§8-§ePremium")
            .hoverText("§7现代化邮件系统 By 老廖")
            .append(" ").append("§f1.0.1 §e高级版")
            .hoverText("""
                §7插件版本: §f1.0.1
            """.trimIndent()).sendTo(sender)
        sender.sendMessage("")
        sender.sendMessage("  §7指令: §f/nmp §8[...]")
        if (sender.hasPermission("neonMail.command.admin")) {
            sender.sendLang("CMD-HELP-ADMIN")
        }
        sender.sendLang("CMD-HELP-PLAYER")
    }
}