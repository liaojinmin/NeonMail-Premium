package me.neon.mail.menu.impl

import me.neon.mail.api.mail.IMailAbstract
import me.neon.mail.common.PlayerDataImpl
import me.neon.mail.api.menu.MenuData
import me.neon.mail.menu.MenuLoader
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import me.neon.mail.api.menu.MenuType
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.PageableChest
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
    private val data: PlayerDataImpl,
    private val mail: IMailAbstract<*>,
    private val mails: List<ItemStack>
) {

    private val menuData: MenuData = MenuLoader.getMenuData(MenuType.ItemPreview)

    fun open() {
        player.openInventory(inventory)
    }

    private val inventory by lazy {
        buildMenu<PageableChest<ItemStack>>(
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