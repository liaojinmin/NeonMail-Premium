package me.neon.mail.menu.edit

import me.neon.mail.data.IPlayerData
import me.neon.mail.mail.IDraftBuilder
import me.neon.mail.mail.MailRegister
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
    private val data: IPlayerData,
    private val mailSenderBuilder: IDraftBuilder,
    private val backCall: IDraftEdite,
    override val admin: Boolean = false
): IDraftEdite {

    private val menuData: MenuData = MenuLoader.getMenuData(MenuType.PlayerList)

    override fun getInventory(): Inventory {
        return buildMenu<Linked<Player>>(
            menuData.title.replacePlaceholder(player)
        ) {
            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements { Bukkit.getOnlinePlayers().filter { mailSenderBuilder.getTarget(it.uniqueId) == null } }

            onGenerate { _, element, _, _ ->
                menuData.getCharMenuIcon('@').parseItems(element)
            }

            onClick { _, playerEle ->
                val type = MailRegister.createNewMailData(mailSenderBuilder.type)
                mailSenderBuilder.addTarget(playerEle.uniqueId, type)
                EditeAppendixMenu(player, data, mailSenderBuilder, playerEle.uniqueId, type, backCall, admin).openMenu()
            }

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            backCall.openMenu()
                        }
                    }
                    'A' -> {
                        if (admin) {
                            set(key, value.parseItems(player)) {
                                EditeAppendixMenu(
                                    player, data,
                                    mailSenderBuilder,
                                    MailRegister.console,
                                    mailSenderBuilder.changeGlobalModel(),
                                    backCall,
                                    true
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