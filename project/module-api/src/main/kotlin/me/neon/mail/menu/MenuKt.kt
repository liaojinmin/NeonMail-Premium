package me.neon.mail.menu

import me.neon.mail.libs.taboolib.ui.type.Basic
import me.neon.mail.libs.taboolib.ui.type.Linked
import org.bukkit.entity.Player

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


