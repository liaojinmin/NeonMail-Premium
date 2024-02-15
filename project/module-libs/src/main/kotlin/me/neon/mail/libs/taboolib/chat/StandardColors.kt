package me.neon.mail.libs.taboolib.chat

import net.md_5.bungee.api.ChatColor
import java.util.*

/**
 * @author sky
 * @since 2021/1/18 2:02 下午
 */
enum class StandardColors(val display: String, val chatColor: ChatColor) {
    BLACK("黑", ChatColor.BLACK), DARK_BLUE("深蓝", ChatColor.DARK_BLUE), DARK_GREEN(
        "深绿",
        ChatColor.DARK_GREEN
    ),
    DARK_AQUA("深青", ChatColor.DARK_AQUA), DARK_RED("深红", ChatColor.DARK_RED), DARK_PURPLE(
        "深紫",
        ChatColor.DARK_PURPLE
    ),
    GOLD("金", ChatColor.GOLD), GRAY("灰", ChatColor.GRAY), DARK_GRAY("深灰", ChatColor.DARK_GRAY), BLUE(
        "蓝",
        ChatColor.BLUE
    ),
    GREEN("绿", ChatColor.GREEN), AQUA("青", ChatColor.AQUA), RED("红", ChatColor.RED), LIGHT_PURPLE(
        "浅紫",
        ChatColor.LIGHT_PURPLE
    ),
    YELLOW("黄", ChatColor.YELLOW), WHITE("白", ChatColor.WHITE), RESET("重置", ChatColor.RESET);

    fun toChatColor(): ChatColor {
        return chatColor
    }

    companion object {
        fun match(`in`: String?): Optional<StandardColors> {
            for (knownColor in values()) {
                if (knownColor.name.equals(`in`, ignoreCase = true) || knownColor.display.equals(
                        `in`,
                        ignoreCase = true
                    )
                ) {
                    return Optional.of(knownColor)
                }
            }
            return Optional.empty()
        }
    }
}