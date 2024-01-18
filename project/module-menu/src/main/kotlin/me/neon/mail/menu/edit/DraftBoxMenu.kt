package me.neon.mail.menu.edit

import me.neon.mail.common.MailDraftBuilder
import me.neon.mail.common.PlayerData
import me.neon.mail.menu.*
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.module.ui.buildMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.ItemBuilder
import taboolib.platform.util.sendLang

/**
 * NeonMail-Premium
 * me.neon.mail.common.menu.edit
 *
 * @author 老廖
 * @since 2024/1/9 16:38
 */
class DraftBoxMenu(
    override val player: Player,
    private val data: PlayerData,
    override val admin: Boolean = false
): IDraftEdite {

    private val menuData = MenuLoader.draftBoxMenu

    override fun getInventory(): Inventory {
        return buildMenu<Linked<MailDraftBuilder>>(menuData.title.replacePlaceholder(player)) {

            map(*menuData.layout)

            rows(menuData.layout.size)

            slots(menuData.getCharSlotIndex('@'))

            elements { data.getAllDraft() }

            onGenerate { _, element, _, _ ->
                val icon = menuData.getCharMenuIcon('@')
                val itemBuilder = ItemBuilder(icon.mats)
                itemBuilder.name = icon.name
                    .replacePlaceholder(player)
                    .replace("[title]", element.title)
                itemBuilder.customModelData = icon.model
                icon.lore.forEach {
                    if (it.contains("[text]")) {
                        itemBuilder.lore.addAll(
                            element.context
                        )
                    } else itemBuilder.lore.add(
                        it.replace("[title]", element.title)
                    )
                }
                itemBuilder.build()
            }

            onClick { _, element ->
                DraftMailEditeMenu(player, data, element, admin).openMenu()
            }

            menuData.icon.forEach { (key, value) ->
                when (key) {
                    'C' -> {
                        set(key, value.parseItems(player)) {
                            if (data.getAllDraft().size >= 20) {
                                player.sendLang("玩家-草稿邮件-已满")
                            } else {
                                TypeSelectMenu(player, admin) {
                                    data.addDraft(it)
                                    DraftMailEditeMenu(player, data, it, admin).openMenu()
                                }.openMenu()
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