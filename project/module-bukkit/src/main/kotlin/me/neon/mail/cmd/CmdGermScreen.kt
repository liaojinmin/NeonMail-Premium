package me.neon.mail.cmd


import me.neon.mail.germ.GermPluginLoader
import me.neon.mail.germ.impl.EditeScreen
import me.neon.mail.germ.impl.ReceiveScreen
import me.neon.mail.germ.impl.SenderScreen
import me.neon.mail.utils.syncRunner
import me.neon.mail.service.ServiceManager
import me.neon.mail.service.ServiceManager.selectAllDraft
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.subCommand
import me.neon.mail.Settings.sendLang

/**
 * NeonMail-Premium
 * me.neon.mail.common.cmd
 *
 * @author 老廖
 * @since 2024/1/9 16:18
 */
object CmdGermScreen {

    val command = subCommand {
        dynamic("种类") {
            suggestion<CommandSender>(false) { _, _ -> listOf("senderBox", "receiveBOX", "editeBox", "reload") }
            execute<CommandSender> { sender, context, _ ->
                if (sender is Player) {
                    openMenu(sender, context["种类"])
                }
            }
            dynamic("目标玩家", true, "neonMail.command.box.at") {
                suggestion<CommandSender>(false) { _, _ -> Bukkit.getOnlinePlayers().map { it.displayName } }
                execute<CommandSender> { sender, context, _ ->
                    Bukkit.getPlayer(context["目标玩家"])?.let {
                        openMenu(it, context["种类"])
                    } ?: sender.sendMessage("玩家不存在...")
                }
            }
        }
    }


    private fun openMenu(player: Player, type: String) {
        if (type.equals("reload", true)) {
            GermPluginLoader.loader()
            return
        }
        ServiceManager.getPlayerData(player.uniqueId)?.let {
            when (type) {
                "senderBox" -> {
                    SenderScreen(player, it, GermPluginLoader.senderSection)
                    return
                }
                "receiveBOX" -> {
                    ReceiveScreen(player, it, GermPluginLoader.receiveSection)
                    return
                }
                "editeBox" -> {
                    // 先检查草稿箱是否为空，如果为空可能没加载草稿数据，需要尝试一次加载
                    if (it.draftIsLoad) {
                        EditeScreen(player, it, GermPluginLoader.editeSection)
                    } else {
                        player.sendLang("玩家-草稿邮件-查询中")
                        it.selectAllDraft { data ->
                            it.applyDraft(data)
                            syncRunner {
                                EditeScreen(player, it, GermPluginLoader.editeSection)
                            }
                        }
                    }
                }
                else -> {
                    error("未知界面 $type")
                }
            }
        }

    }
}