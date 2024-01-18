package me.neon.mail.menu.edit

import me.neon.mail.api.mail.IMailDataType
import me.neon.mail.common.PlayerData
import me.neon.mail.menu.MenuLoader
import me.neon.mail.common.MailDraftBuilder
import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.api.mail.IMailRegister
import me.neon.mail.menu.IDraftEdite
import me.neon.mail.menu.MenuData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.meta.SkullMeta
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Basic
import taboolib.platform.compat.replacePlaceholder
import java.util.UUID

/**
 * NeonMail-Premium
 * me.neon.mail.menu.edit
 *
 * @author 老廖
 * @since 2024/1/6 18:23
 */
class MailAppEditeMenu(
    override val player: Player,
    private val data: PlayerData,
    private val mail: MailDraftBuilder,
    private val playerUUID: UUID,
    private val type: IMailDataType,
    private val backCall: IDraftEdite?,
    override val admin: Boolean = false
): IDraftEdite {

    private val menuData: MenuData = MenuLoader.mailAppEditeMenu


    override fun getInventory(): Inventory {
        return buildMenu<Basic>(
            menuData.title.replacePlaceholder(player)
        ) {
            map(*menuData.layout)

            rows(menuData.layout.size)

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            backCall?.openMenu() ?: DraftMailEditeMenu(player, data, mail, admin).openMenu()
                        }
                    }
                    'P' -> {
                        if (playerUUID == IMailRegister.console) {
                            val item = value.parseItems(player, "[player]" to "all")
                            set(key, item)
                        } else {
                            val target = Bukkit.getOfflinePlayer(playerUUID)
                            val item = value.parseItems(player, "[player]" to target.name)
                            val meta = item.itemMeta
                            if (meta is SkullMeta) {
                                meta.owningPlayer = target
                                item.itemMeta = meta
                            }
                            set(key, item)
                        }
                    }
                    else -> {
                        val back = mail.getMailSource<IMailAbstract<*>>()
                            .parseDataUpdateCallBack(value, player, type, mail, this@MailAppEditeMenu)
                        set(key, back.first, back.second)
                    }
                }
            }
        }
    }
}