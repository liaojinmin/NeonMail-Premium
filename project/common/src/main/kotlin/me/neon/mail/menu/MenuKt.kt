package me.neon.mail.menu

import org.bukkit.entity.Player
import taboolib.module.ui.type.Chest
import taboolib.module.ui.type.PageableChest
import taboolib.module.ui.type.impl.PageableChestImpl

/**
 * NeonMail-Premium
 * me.neon.mail.common.menu
 *
 * @author 老廖
 * @since 2024/1/9 0:34
 */

fun PageableChest<*>.setupNext(player: Player, value: MenuIcon, key: Char) {
    val max = (this as PageableChestImpl).elementsCache.size / menuSlots.size
    set(key, value.parseItems(player, "{nowPage}" to page + 1, "{maxPage}" to max + 1)) {
        value.eval(player)
        if (hasNextPage()) {
            page(page+1)
            player.openInventory(build())
        }
    }
}

fun PageableChest<*>.setupPrev(player: Player, value: MenuIcon, key: Char) {
    val max = (this as PageableChestImpl).elementsCache.size / menuSlots.size
    set(key, value.parseItems(player,"{nowPage}" to page + 1, "{maxPage}" to max + 1)) {
        value.eval(player)
        if (hasPreviousPage()) {
            page(page-1)
            player.openInventory(build())
        }
    }
}


fun Chest.setupDefaultAction(player: Player, value: MenuIcon, key: Char) {
    if (key != '@') {
        set(key, value.parseItems(player)) {
            value.eval(player)
        }
    }
}


