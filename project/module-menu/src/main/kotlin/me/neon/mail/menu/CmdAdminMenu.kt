package me.neon.mail.menu

import me.neon.mail.cmd.ICmd
import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest

/**
 * NeonMail-Premium
 * me.neon.mail.common.cmd
 *
 * @author 老廖
 * @since 2024/1/9 16:18
 */
object CmdAdminMenu: ICmd {

    /**
     * 超级权限打开操作菜单，将忽略附件扣除
     */
    override val command = subCommand {
        dynamic("种类") {
            suggest { listOf("senderBox", "receiveBOX", "editeBox") }
            execute<Player> { sender, context, _ ->
                CmdMenu.openMenu(sender, context["种类"], true)
            }
        }
    }



}