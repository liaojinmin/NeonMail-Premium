package me.neon.mail.menu.edit

import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.common.PlayerData
import me.neon.mail.common.MailDraftBuilder
import me.neon.mail.menu.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder

/**
 * NeonMail-Premium
 * me.neon.mail.menu.edit
 *
 * @author 老廖
 * @since 2024/1/6 18:23
 */
class PlayerListMenu(
    override val player: Player,
    private val data: PlayerData,
    private val mailSenderBuilder: MailDraftBuilder,
    private val backCall: DraftEdite
): DraftEdite {

    private val menuData: MenuData = MenuLoader.playerListMenu

    override fun getInventory(): Inventory {
        return buildMenu<Linked<Player>>(
            menuData.title.replacePlaceholder(player)
        ) {
            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements { Bukkit.getOnlinePlayers().toList() }

            onGenerate { _, element, _, _ ->
                NeonMailLoader.debug("构建 -> ${element.name}")
                menuData.getCharMenuIcon('@').parseItems(element)
            }

            onClick { _, element ->
                val type = mailSenderBuilder.getMailSource<IMailAbstract<*>>().createData()
                mailSenderBuilder.targets[element.uniqueId] = type
                MailAppEditeMenu(player, data, mailSenderBuilder, element.uniqueId, type, backCall).openMenu()
            }

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            backCall.openMenu()
                        }
                    }
                    '>' -> setupNext(player, value, key)
                    '<' -> setupPrev(player, value, key)
                    else -> setupDefaultAction(player, value, key)
                }
            }
        }
    }
}