package me.neon.mail.common.menu.edit

import me.neon.mail.api.IMailDataType
import me.neon.mail.common.menu.MenuData
import me.neon.mail.common.menu.MenuLoader
import me.neon.mail.common.DataTypeNormal
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
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
class ItemEditMenu(
    override val player: Player,
    private val uiBack: () -> Unit,
    private val type: IMailDataType,
): DraftEdite {

    private val menuData: MenuData = MenuLoader.itemEditeMenu


    override fun getInventory(): Inventory {
        return buildMenu<Linked<ItemStack>>(
            menuData.title.replacePlaceholder(player)
        ) {
            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements {
                // TODO("标记，未验证玩家取走物品后列表是否更新")
                if (type is DataTypeNormal) {
                    type.itemStacks
                } else emptyList()
            }

            onGenerate { _, element, _, _ ->  element }

            onClick(false)

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    'B' -> {
                        set(key, value.parseItems(player)) {
                            isCancelled = true
                            uiBack.invoke()
                        }
                    }
                    else -> {
                        if (key != '@') {
                            set(key, value.parseItems(player)) {
                                isCancelled = true
                                value.eval(player)
                            }
                        }
                    }
                }
            }

        }
    }
}