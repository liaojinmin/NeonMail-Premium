package me.neon.mail.menu.edit

import me.neon.mail.data.IPlayerData
import me.neon.mail.mail.IDraftBuilder
import me.neon.mail.mail.IMailData
import me.neon.mail.menu.MenuLoader
import me.neon.mail.mail.MailRegister
import me.neon.mail.menu.IDraftEdite
import me.neon.mail.menu.MenuData
import me.neon.mail.menu.MenuType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.meta.SkullMeta
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.compat.replacePlaceholder
import java.util.UUID

/**
 * NeonMail-Premium
 * me.neon.mail.menu.edit
 *
 * @author 老廖
 * @since 2024/1/6 18:23
 */
class EditeAppendixMenu(
    override val player: Player,
    private val data: IPlayerData,
    private val mail: IDraftBuilder,
    private val playerUUID: UUID,
    private val type: IMailData,
    private val backCall: IDraftEdite?,
    override val admin: Boolean = false
): IDraftEdite {

    private val menuData: MenuData = MenuLoader.getMenuData(MenuType.MailAppEdite)

    override fun getInventory(): Inventory {
        return buildMenu<Chest>(
            menuData.title.replacePlaceholder(player)
        ) {
            map(*menuData.layout)

            rows(menuData.layout.size)

            onClick(true)

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            backCall?.openMenu() ?: DraftMailEditeMenu(player, data, mail, admin).openMenu()
                        }
                    }
                    'P' -> {
                        if (playerUUID == MailRegister.console) {
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
                        val back = type.parseCallBack(value, player, mail, this@EditeAppendixMenu)
                        set(key, back.first, back.second)
                    }
                }
            }
        }
    }
}