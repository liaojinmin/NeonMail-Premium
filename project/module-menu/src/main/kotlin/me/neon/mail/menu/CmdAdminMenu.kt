package me.neon.mail.menu

import me.neon.mail.cmd.ICmd
import me.neon.mail.service.ServiceManager
import me.neon.mail.service.ServiceManager.selectAllDraft
import me.neon.mail.menu.edit.DraftBoxMenu
import me.neon.mail.menu.impl.ReceiveMenu
import me.neon.mail.menu.impl.SenderMenu
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.command.suggest
import taboolib.common.platform.function.submit
import taboolib.platform.util.sendLang

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