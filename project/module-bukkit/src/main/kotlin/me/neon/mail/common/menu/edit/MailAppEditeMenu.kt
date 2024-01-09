package me.neon.mail.common.menu.edit

import me.neon.mail.ServiceManager.updateToSql
import me.neon.mail.api.IMailDataType
import me.neon.mail.common.PlayerData
import me.neon.mail.common.menu.MenuData
import me.neon.mail.common.menu.MenuLoader
import me.neon.mail.common.MailDraftBuilder
import me.neon.mail.api.IMailAbstract
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
    private val dataKey: UUID,
    private val type: IMailDataType,
    private val backCall: DraftEdite?
): DraftEdite {

    private val menuData: MenuData = MenuLoader.playerListMenu


    override fun getInventory(): Inventory {
        return buildMenu<Basic>(
            menuData.title.replacePlaceholder(player)
        ) {
            map(*menuData.layout)

            rows(menuData.layout.size)

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    'B' -> {
                        backCall?.openMenu() ?: DraftMailEditeMenu(player, data, mail).openMenu()
                    }
                    'P' -> {
                        val target = Bukkit.getOfflinePlayer(dataKey)
                        val item = value.parseItems(player, "[player]" to target.name)
                        val meta = item.itemMeta
                        if (meta is SkullMeta) {
                            meta.owningPlayer = target
                            item.itemMeta = meta
                        }
                        set(key, item)
                    }
                    else -> {
                        val back = mail.getMailSource<IMailAbstract<*>>()
                            .parseDataUpdateCallBack(value, player, type) {
                                mail.updateToSql()
                                openMenu()
                        }
                        set(key, back.first, back.second)
                    }
                }
            }
        }
    }
}