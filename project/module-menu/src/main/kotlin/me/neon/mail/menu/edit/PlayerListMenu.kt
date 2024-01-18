package me.neon.mail.menu.edit

import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.api.mail.IMailRegister
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
    private val backCall: IDraftEdite,
    override val admin: Boolean = false
): IDraftEdite {

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
                menuData.getCharMenuIcon('@').parseItems(element)
            }

            onClick { _, playerEle ->
                val type = mailSenderBuilder.getMailSource<IMailAbstract<*>>().createData()
                mailSenderBuilder.addTarget(playerEle.uniqueId, type)
                MailAppEditeMenu(player, data, mailSenderBuilder, playerEle.uniqueId, type, backCall, admin).openMenu()
            }

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    'B' -> {
                        NeonMailLoader.debug("B playerlist")
                        set(key, value.parseItems(player)) {
                            backCall.openMenu()
                        }
                    }
                    'A' -> {
                        NeonMailLoader.debug("A playerlist")
                        if (admin) {
                            set(key, value.parseItems(player)) {
                                MailAppEditeMenu(player, data,
                                    mailSenderBuilder, IMailRegister.console, mailSenderBuilder.changeGlobalModel(), backCall, admin
                                ).openMenu()
                            }
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