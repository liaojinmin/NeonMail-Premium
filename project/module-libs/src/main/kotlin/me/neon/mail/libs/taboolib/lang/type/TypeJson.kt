package me.neon.mail.libs.taboolib.lang.type

import me.neon.mail.libs.taboolib.chat.HexColor.parseToHexColor
import me.neon.mail.libs.taboolib.chat.HexColor.toGradientColor
import me.neon.mail.libs.utils.asList
import me.neon.mail.libs.utils.replaceWithOrder
import me.neon.mail.libs.taboolib.chat.RawMessage
import me.neon.mail.libs.taboolib.lang.Type
import me.neon.mail.libs.utils.VariableReader
import org.bukkit.command.CommandSender

/**
 * TabooLib
 * me.neon.mail.libs.lang.type.TypeJson
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
@Suppress("SpellCheckingInspection")
class TypeJson : Type {

    var text: List<String>? = null
    var jsonArgs = ArrayList<Map<String, Any>>()

    override fun init(source: Map<String, Any>) {
        text = source["text"]?.asList()
        try {
            jsonArgs.addAll((source["args"] as List<*>).map { (it as Map<*, *>).map { (k, v) -> k.toString() to v!! }.toMap() })
        } catch (_: ClassCastException) {
        }
    }

    override fun send(sender: CommandSender, vararg args: Any) {
        /** 转换文本 */
        fun String.form(): String {
            return translate(sender, *args).replaceWithOrder(*args)
        }
        /** 构建信息 */
        RawMessage().sendTo(sender) {
            var i = 0
            text?.forEachIndexed { index, line ->
                // 加载变量
                parser.readToFlatten(line).forEach { part ->
                    // 获取文本块类型
                    val extra = if (part.isVariable) jsonArgs.getOrNull(i++) else emptyMap()
                    if (extra == null) {
                        append("§c[RAW OPTION NOT FOUND]")
                        return@forEach
                    }
                    // 显示文字
                    val showText = part.text.form()
                    val showType = extra["type"].toString().form()
                    when {
                        // 快捷键
                        showType == "keybind" -> appendKeybind(showText)
                        // 选择器
                        showType == "selector" -> appendSelector(showText)
                        // 语言
                        // text: '[commands.drop.success.single]'
                        // args:
                        // - type: translate:1:Stone
                        showType == "translate" -> appendTranslatable(showText, *showType.substringAfter(':').split(':').toTypedArray())
                        // 分数
                        showType == "score" -> appendScore(showText.substringBefore(':'), showText.substringAfter(':'))
                        // 渐变颜色文本
                        // text: 'Woo: [||||||||||||||||||||||||]'
                        // args:
                        // - type: gradient:#ff0000:#00ff00:#0000ff:#ff0000
                        showType.startsWith("gradient") -> {
                            append(showText.toGradientColor(showType.substringAfter(':').split(':').map { it.parseToHexColor() }))
                        }
                        // 标准
                        else -> append(showText)
                    }
                    // 附加信息
                    if (extra.containsKey("hover")) {
                        hoverText(extra["hover"].toString().form())
                    }
                    if (extra.containsKey("command")) {
                        runCommand(extra["command"].toString().form())
                    }
                    if (extra.containsKey("suggest")) {
                        suggestCommand(extra["suggest"].toString().form())
                    }
                    if (extra.containsKey("insertion")) {
                        insertion(extra["insertion"].toString().form())
                    }
                    if (extra.containsKey("copy")) {
                        copyToClipboard(extra["copy"].toString().form())
                    }
                    if (extra.containsKey("file")) {
                        openFile(extra["file"].toString().form())
                    }
                    if (extra.containsKey("url")) {
                        openURL(extra["url"].toString().form())
                    }
                    if (extra.containsKey("font")) {
                        font(extra["font"].toString().form())
                    }
                }
                if (index + 1 < text!!.size) {
                    newLine()
                }
            }
        }
    }

    companion object {


        private val parser = VariableReader("[", "]")
    }
}