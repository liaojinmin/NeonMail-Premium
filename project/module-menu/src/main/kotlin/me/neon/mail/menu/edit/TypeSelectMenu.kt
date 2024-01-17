package me.neon.mail.menu.edit

import me.neon.mail.api.mail.IMailRegister
import me.neon.mail.common.MailDraftBuilder
import me.neon.mail.menu.MenuLoader
import me.neon.mail.menu.setupDefaultAction
import me.neon.mail.menu.setupNext
import me.neon.mail.menu.setupPrev
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.ItemBuilder

/**
 * NeonMail-Premium
 * me.neon.mail.common.menu.edit
 *
 * @author 老廖
 * @since 2024/1/9 16:38
 */
class TypeSelectMenu(
    override val player: Player,
    private val callBack: (MailDraftBuilder) -> Unit
): DraftEdite {

    private val menuData = MenuLoader.typeSelectMenu

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
                    .replace("[type]", element)
                itemBuilder.customModelData = icon.model
                itemBuilder.build()
            }

            onClick { _, element ->
                IMailRegister.getRegisterMail(element)?.let {
                    callBack.invoke(MailDraftBuilder(player.uniqueId, element))
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