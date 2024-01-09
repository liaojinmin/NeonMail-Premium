package me.neon.mail.common.menu

import org.bukkit.Bukkit

/**
 * NeonMail-Premium
 * me.neon.mail.menu
 *
 * @author 老廖
 * @since 2024/1/4 13:45
 */
class MenuData(
    val title: String,
    val layout: Array<String>,
    val icon: MutableMap<Char, MenuIcon>
) {
    private val slotCache: MutableMap<String, MutableList<Int>> = mutableMapOf()

    fun getCharMenuIcon(char: Char): MenuIcon {
        return icon[char] ?: error("找不到 $char 字符的图标配置...")
    }


    fun getCharSlotIndex(char: Char): MutableList<Int> {
        return slotCache.computeIfAbsent(hashCode().toString() + "-" + char) {
            mutableListOf<Int>().apply {
                var index = 0
                layout.forEach { d ->
                    d.toCharArray().forEach { c ->
                        if (c == char) add(index)
                        index++
                    }
                }
            }
        }
    }
}
