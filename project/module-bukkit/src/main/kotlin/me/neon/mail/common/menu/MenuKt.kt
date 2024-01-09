package me.neon.mail.common.menu

import org.bukkit.entity.Player
import taboolib.module.ui.type.Basic
import taboolib.module.ui.type.Linked

/**
 * NeonMail-Premium
 * me.neon.mail.common.menu
 *
 * @author 老廖
 * @since 2024/1/9 0:34
 */

fun Linked<*>.setupNext(player: Player, value: MenuIcon, key: Char) {
    set(key, value.parseItems(player)) {
        value.eval(player)
        if (hasNextPage()) {
            page(page+1)
            player.openInventory(build())
        }
    }
}

fun Linked<*>.setupPrev(player: Player, value: MenuIcon, key: Char) {
    set(key, value.parseItems(player)) {
        value.eval(player)
        if (hasPreviousPage()) {
            page(page-1)
            player.openInventory(build())
        }
    }
}

fun Basic.setupDefaultAction(player: Player, value: MenuIcon, key: Char) {
    if (key != '@') {
        set(key, value.parseItems(player)) {
            value.eval(player)
        }
    }
}