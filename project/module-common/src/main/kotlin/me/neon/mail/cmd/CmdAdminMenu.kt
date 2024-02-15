package me.neon.mail.cmd

import org.bukkit.entity.Player
import me.neon.mail.libs.taboolib.command.subCommand

/**
 * NeonMail-Premium
 * me.neon.mail.common.cmd
 *
 * @author 老廖
 * @since 2024/1/9 16:18
 */
object CmdAdminMenu {

    /**
     * 超级权限打开操作菜单，将忽略附件扣除
     */
    val command = subCommand {
        dynamic("种类") {
            suggestion(false) { _, _ -> listOf("senderBox", "receiveBOX", "editeBox") }
            execute { sender, context, _ ->
                if (sender is Player) {
                    CmdMenu.openMenu(sender, context["种类"], true)
                }
            }
        }
    }





}