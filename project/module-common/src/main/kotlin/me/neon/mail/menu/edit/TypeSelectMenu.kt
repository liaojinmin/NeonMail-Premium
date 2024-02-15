package me.neon.mail.menu.edit

import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IDraftBuilder
import me.neon.mail.api.mail.IMailRegister
import me.neon.mail.common.DraftBuilderImpl
import me.neon.mail.libs.taboolib.chat.HexColor.colored
import me.neon.mail.libs.taboolib.ui.buildMenu
import me.neon.mail.menu.*
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import me.neon.mail.libs.taboolib.ui.type.Linked
import me.neon.mail.libs.utils.ItemBuilder
import me.neon.mail.libs.utils.replacePlaceholder

/**
 * NeonMail-Premium
 * me.neon.mail.common.menu.edit
 *
 * @author 老廖
 * @since 2024/1/9 16:38
 */
class TypeSelectMenu(
    override val player: Player,
    override val admin: Boolean = false,
    private val callBack: (IDraftBuilder) -> Unit
): IDraftEdite {

    private val menuData = MenuLoader.getMenuData(MenuType.TypeSelect)

    override fun getInventory(): Inventory {
        return buildMenu<Linked<String>>(menuData.title.replacePlaceholder(player)) {

            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements { IMailRegister.getRegisterKeys() }

            onGenerate { _, element, _, _ ->
                val icon = menuData.getCharMenuIcon('@')
                val itemBuilder = ItemBuilder(icon.mats)
                itemBuilder.name = icon.name
                    .replacePlaceholder(player)
                    // 使用翻译种类展示
                    .replace("[type]", NeonMailLoader.typeTranslate[element]?.colored() ?: element)
                itemBuilder.customModelData = icon.model
                itemBuilder.build()
            }

            onClick { _, element ->
                IMailRegister.getRegisterMail(element)?.let {
                    callBack.invoke(DraftBuilderImpl(player.uniqueId, element))
                }
            }

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    '>' -> setupNext(player, value, key)
                    '<' -> setupPrev(player, value, key)
                    else -> setupDefaultAction(player, value, key)
                }
            }
        }
    }
}