package me.neon.mail.libs.utils

import me.clip.placeholderapi.PlaceholderAPI
import me.neon.mail.libs.NeonLibsLoader
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable

/**
 * NeonMail
 * me.neon.mail.libs.utils
 *
 * @author 老廖
 * @since 2024/2/14 15:50
 */


fun String.replaceWithOrder(vararg args: Any): String {
    if (args.isEmpty() || isEmpty()) {
        return this
    }
    val chars = toCharArray()
    val builder = StringBuilder(length)
    var i = 0
    while (i < chars.size) {
        val mark = i
        if (chars[i] == '{') {
            var num = 0
            val alias = StringBuilder()
            while (i + 1 < chars.size && chars[i + 1] != '}') {
                i++
                if (Character.isDigit(chars[i]) && alias.isEmpty()) {
                    num *= 10
                    num += chars[i] - '0'
                } else {
                    alias.append(chars[i])
                }
            }
            if (i != mark && i + 1 < chars.size && chars[i + 1] == '}') {
                i++
                if (alias.isNotEmpty()) {
                    val str = alias.toString()
                    builder.append((args.firstOrNull { it is Pair<*, *> && it.second == str } as? Pair<*, *>)?.first ?: "{$str}")
                } else {
                    builder.append(args.getOrNull(num) ?: "{$num}")
                }
            } else {
                i = mark
            }
        }
        if (mark == i) {
            builder.append(chars[i])
        }
        i++
    }
    return builder.toString()
}

fun String.replacePlaceholder(player: Player): String {
    return PlaceholderAPI.setPlaceholders(player, this)
}



fun Collection<String>.replacePlaceholder(player: Player): List<String> {
    return this.map { PlaceholderAPI.setPlaceholders(player, it) }
}

fun Any.asList(): List<String> {
    return when (this) {
        is Collection<*> -> map { it.toString() }
        is Array<*> -> map { it.toString() }
        else -> listOf(toString())
    }
}

fun <T> subList(list: List<T>, start: Int = 0, end: Int = list.size): List<T> {
    return list.filterIndexed { index, _ -> index in start until end }
}





