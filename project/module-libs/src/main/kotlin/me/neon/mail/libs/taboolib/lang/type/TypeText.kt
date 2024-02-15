package me.neon.mail.libs.taboolib.lang.type

import me.neon.mail.libs.taboolib.lang.Type
import me.neon.mail.libs.utils.replaceWithOrder
import org.bukkit.command.CommandSender

/**
 * TabooLib
 * me.neon.mail.libs.lang.type.TypeText
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
class TypeText : Type {

    var text: String? = null

    constructor()
    constructor(text: String) {
        if (text.isNotEmpty()) {
            this.text = text
        }
    }

    fun asText(sender: CommandSender, vararg args: Any): String? {
        return text?.translate(sender, *args)?.replaceWithOrder(*args)
    }

    override fun init(source: Map<String, Any>) {
        text = source["text"]?.toString()
        // if blank, set null
        if (text?.isEmpty() == true) {
            text = null
        }
    }

    override fun send(sender: CommandSender, vararg args: Any) {
        if (text != null) {
            val newText = text!!.translate(sender, *args).replaceWithOrder(*args)
            sender.sendMessage(newText)
        }
    }

    override fun toString(): String {
        return "NodeText(text=$text)"
    }
}