package me.neon.mail.common.cmd


import org.bukkit.command.CommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.pluginVersion
import taboolib.module.chat.Components
import taboolib.module.lang.sendLang

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
    val box = CmdBox.command


    private fun createHelp(sender: CommandSender) {
        val s = adaptCommandSender(sender)
        s.sendMessage("")
        Components.empty()
            .append("  ").append("§bNeon§9Mail§8-§ePremium")
            .hoverText("§7现代化邮件系统 By 老廖")
            .append(" ").append("§f$pluginVersion §e付费版")
            .hoverText("""
                §7插件版本: §f$pluginVersion
            """.trimIndent()).sendTo(s)
        s.sendMessage("")
        s.sendMessage("  §7指令: §f/nmp §8[...]")
        if (sender.hasPermission("neonMail.command.admin")) {
            s.sendLang("CMD-HELP-ADMIN")
        }
        s.sendLang("CMD-HELP-PLAYER")
    }
}