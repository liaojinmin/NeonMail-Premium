package me.neon.mail.libs.taboolib.chat

import net.md_5.bungee.api.ChatColor
import java.awt.Color
import kotlin.math.ceil

/**
 * @author sky
 * @since 2021/1/18 2:02 下午
 */
object HexColor {

    private var isLegacy = false

    init {
        try {
            ChatColor.of(Color.BLACK)
        } catch (ignored: NoSuchMethodError) {
            isLegacy = true
        }
    }

    val Int.red: Int
        get() = (this shr 16) and 0xFF

    val Int.green: Int
        get() = (this shr 8) and 0xFF

    val Int.blue: Int
        get() = this and 0xFF

    fun Int.mix(next: Int, d: Double): Int {
        val r = (this.red * (1 - d) + next.red * d).toInt()
        val g = (this.green * (1 - d) + next.green * d).toInt()
        val b = (this.blue * (1 - d) + next.blue * d).toInt()
        return (r shl 16) or (g shl 8) or b
    }



    /**
     * 对字符串上色
     */
    fun String.colored() = HexColor.translate(this)

    /**
     * 对字符串去色
     */
    fun String.uncolored() = ChatColor.stripColor(this)!!

    /**
     * 对列表上色
     */
    fun List<String>.colored() = map { it.colored() }

    /**
     * 对列表去色
     */
    fun List<String>.uncolored() = map { it.uncolored() }

    /**
     * 获取颜色
     */
    fun String.parseToHexColor(): Int {
        // HEX: #ffffff
        if (startsWith('#')) {
            return substring(1).toIntOrNull(16) ?: 0
        }
        // RGB: 255,255,255
        if (contains(',')) {
            return split(',').map { it.toIntOrNull() ?: 0 }.let { (r, g, b) -> (r shl 16) or (g shl 8) or b }
        }
        // RGB: 255-255-255
        if (contains('-')) {
            return split('-').map { it.toIntOrNull() ?: 0 }.let { (r, g, b) -> (r shl 16) or (g shl 8) or b }
        }
        // NAMED: white
        val knownColor = StandardColors.match(this)
        if (knownColor.isPresent) {
            // 没颜色的
            return if (knownColor.get().chatColor.color == null) {
                0
            } else {
                knownColor.get().chatColor.color.rgb
            }
        }
        return 0
    }

    /**
     * 创建渐变颜色
     */
    fun String.toGradientColor(colors: List<Int>): String {
        // 每个颜色的长度
        val step = ceil(length.toDouble() / (colors.size - 1)).toInt()
        // 生成过渡颜色
        val gradientText = StringBuilder()
        forEachIndexed { index, c ->
            val current = colors[index / step]
            val next = colors[(index / step + 1).coerceAtMost(colors.size - 1)]
            val position = index % step
            val percent = position.toDouble() / step
            val color = current.mix(next, percent)
            gradientText.append("${getColorCode(color)}$c")
        }
        return gradientText.toString()
    }

    /**
     * 对字符串中的特殊颜色表达式进行转换<br></br>
     * 可供转换的格式有：
     *
     *
     * &amp;{255-255-255} —— RGB 代码
     *
     *
     * &amp;{255,255,255} —— RGB 代码
     *
     *
     * &amp;{#FFFFFF}     —— HEX 代码
     *
     *
     * &amp;{BLUE}        —— 已知颜色（英文）
     *
     *
     * &amp;{蓝}          —— 已知颜色（中文）
     *
     * @param in 字符串
     * @return String
     */
    fun translate(`in`: String): String {
        if (isLegacy) {
            return ChatColor.translateAlternateColorCodes('&', `in`)
        }
        val builder = StringBuilder()
        val chars = `in`.toCharArray()
        var i = 0
        while (i < chars.size) {
            if (i + 1 < chars.size && chars[i] == '&' && chars[i + 1] == '{') {
                var chatColor: ChatColor? = null
                var match = CharArray(0)
                var j = i + 2
                while (j < chars.size && chars[j] != '}') {
                    match = arrayAppend(match, chars[j])
                    j++
                }
                if (match.size == 11 && (match[3] == ',' || match[3] == '-') && (match[7] == ',' || match[7] == '-')) {
                    chatColor = ChatColor.of(Color(toInt(match, 0, 3), toInt(match, 4, 7), toInt(match, 8, 11)))
                } else if (match.size == 7 && match[0] == '#') {
                    try {
                        chatColor = ChatColor.of(toString(match))
                    } catch (ignored: IllegalArgumentException) {
                    }
                } else {
                    val knownColor = StandardColors.match(toString(match))
                    if (knownColor.isPresent) {
                        chatColor = knownColor.get().toChatColor()
                    }
                }
                if (chatColor != null) {
                    builder.append(chatColor)
                    i += match.size + 2
                }
            } else {
                builder.append(chars[i])
            }
            i++
        }
        var colorString = builder.toString()
        // 1.20.4 不再支持该写法，该模块无法判断版本，因此全部替换为白色
        // 若需要恢复默认色请使用 SimpleComponent 中的 reset 属性
        colorString = colorString.replace("&r", "&f").replace("§r", "§f")
        return ChatColor.translateAlternateColorCodes('&', colorString)
    }

    fun getColorCode(color: Int): String {
        return ChatColor.of(Color(color)).toString()
    }

    private fun arrayAppend(chars: CharArray, `in`: Char): CharArray {
        val newChars = CharArray(chars.size + 1)
        System.arraycopy(chars, 0, newChars, 0, chars.size)
        newChars[chars.size] = `in`
        return newChars
    }

    private fun toString(chars: CharArray): String {
        val builder = StringBuilder()
        for (c in chars) {
            builder.append(c)
        }
        return builder.toString()
    }

    private fun toInt(chars: CharArray, start: Int, end: Int): Int {
        val builder = StringBuilder()
        for (i in start until end) {
            builder.append(chars[i])
        }
        return builder.toString().toInt()
    }


}