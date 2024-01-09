package me.neon.mail.common.menu.impl

import me.neon.mail.api.IMailAbstract
import me.neon.mail.common.PlayerData
import me.neon.mail.common.menu.MenuData
import me.neon.mail.common.menu.MenuLoader
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder

/**
 * NeonMail-Premium
 * me.neon.mail.menu.impl
 *
 * @author 老廖
 * @since 2024/1/6 0:21
 */
class ItemPreviewMenu(
    private val player: Player,
    private val data: PlayerData,
    private val mail: IMailAbstract<*>,
    private val mails: List<ItemStack>
) {

    private val menuData: MenuData = MenuLoader.itemPreviewMenu

    fun open() {
        player.openInventory(inventory)
    }

    private val inventory by lazy {
        buildMenu<Linked<ItemStack>>(
            menuData.title.replacePlaceholder(player)
        ) {
            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements { mails }

            onGenerate { _, element, _, _ ->  element }

            onClick(true)

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            ActionsMenu(player, data, mail).open()
                        }
                    }
                    '>' -> {
                        set(key, value.parseItems(player)) {
                            value.eval(player)
                            if (hasNextPage()) {
                                page(page + 1)
                                player.openInventory(build())
                            }
                        }
                    }

                    '<' -> {
                        set(key, value.parseItems(player)) {
                            value.eval(player)
                            if (hasPreviousPage()) {
                                page(page - 1)
                                player.openInventory(build())
                            }
                        }
                    }

                    else -> {
                        if (key != '@') {
                            set(key, value.parseItems(player)) {
                                value.eval(player)
                            }
                        }
                    }
                }
            }

        }
    }
}