package me.neon.mail.libs.taboolib.lang

import me.neon.mail.libs.NeonLibsLoader
import me.neon.mail.libs.lang.type.*
import me.neon.mail.libs.taboolib.chat.HexColor.colored
import me.neon.mail.libs.taboolib.lang.type.*
import me.neon.mail.libs.utils.io.forFile
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * TabooLib
 * me.neon.mail.libs.lang.Language
 *
 * @author sky
 * @since 2021/6/18 10:43 下午
 */
object LangLoader {

    private var firstLoaded = false

    /** 语言文件路径 */
    var path = "lang"

    /** 默认语言文件 */
    var default = "zh_CN"

    /** 文本转换 */
    val textTransfer = ArrayList<TextTransfer>()

    /** 语言文件缓存 */
    val languageFile = HashMap<String, LanguageFile>()

    /** 语言文件代码 */
    val languageCode = HashSet<String>()

    /** 语言文件类型 */
    @Suppress("SpellCheckingInspection")
    val languageType = hashMapOf(
        "text" to TypeText::class.java,
        "raw" to TypeJson::class.java,
        "json" to TypeJson::class.java,
        "title" to TypeTitle::class.java,
        "sound" to TypeSound::class.java,
        "command" to TypeCommand::class.java,
        "actionbar" to TypeActionBar::class.java,
        "action_bar" to TypeActionBar::class.java,
    )
    /** 语言文件代码转换 */
    val languageCodeTransfer = hashMapOf(
        "zh_hans_cn" to "zh_CN",
        "zh_hant_cn" to "zh_TW",
        "en_ca" to "en_US",
        "en_au" to "en_US",
        "en_gb" to "en_US",
        "en_nz" to "en_US"
    )

    fun onLoader() {
        // 加载语言文件类型
        val file = File(NeonLibsLoader.pluginId.dataFolder, path)
        file.forFile("yml").forEach {
            languageCode += it.name.substringBeforeLast('.')
        }
        // 加载颜色字符模块
        try {
            textTransfer += object : TextTransfer {
                override fun translate(sender: CommandSender, source: String, vararg args: Any): String {
                    return source.colored()
                }
            }
        } catch (_: NoClassDefFoundError) {
        }
        // 加载语言文件
        firstLoaded = true
        languageFile.clear()
        languageFile.putAll(ResourceReader().files)
    }
    /** 获取玩家语言 */
    fun getLocale(player: Player): String {
        return languageCodeTransfer[player.locale.lowercase()] ?: player.locale
    }

    fun getLocale(): String {
        return Locale.getDefault().toLanguageTag().replace("-", "_").lowercase()
    }
}