package me.neon.mail.libs.taboolib.lang

import me.neon.mail.libs.taboolib.lang.type.TypeList
import me.neon.mail.libs.taboolib.lang.type.TypeText
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun CommandSender.sendLang(node: String, vararg args: Any) {
    val file = getLocaleFile()
    if (file == null) {
        sendMessage("{$node}")
    } else {
        val type = file.nodes[node]
        if (type != null) {
            type.send(this, *args)
        } else {
            sendMessage("{$node}")
        }
    }
}

fun CommandSender.asLangText(node: String, vararg args: Any): String {
    return asLangTextOrNull(node, *args) ?: "{$node}"
}

fun CommandSender.asLangTextOrNull(node: String, vararg args: Any): String? {
    val file = getLocaleFile()
    if (file != null) {
        return (file.nodes[node] as? TypeText)?.asText(this, *args)
    }
    return null
}

fun CommandSender.asLangTextList(node: String, vararg args: Any): List<String> {
    val file = getLocaleFile()
    return if (file == null) {
        listOf("{$node}")
    } else {
        when (val type = file.nodes[node]) {
            is TypeText -> {
                val text = type.asText(this, *args)
                if (text != null) listOf(text) else emptyList()
            }
            is TypeList -> {
                type.asTextList(this, *args)
            }
            else -> {
                listOf("{$node}")
            }
        }
    }
}
fun CommandSender.getLocales(): String {
    return if (this is Player) LangLoader.getLocale(this) else LangLoader.getLocale()
}
fun CommandSender.getLocaleFile(): LanguageFile? {
    if (this is Player) {
        val locale = getLocales()
        return LangLoader.languageFile.entries.firstOrNull { it.key.equals(locale, true) }?.value
            ?: LangLoader.languageFile[LangLoader.default]
            ?: LangLoader.languageFile.values.firstOrNull()
    } else {
        return LangLoader.languageFile[LangLoader.default]
    }
}
