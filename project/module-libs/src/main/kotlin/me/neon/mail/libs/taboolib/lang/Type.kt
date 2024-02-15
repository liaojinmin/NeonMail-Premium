package me.neon.mail.libs.taboolib.lang

import org.bukkit.command.CommandSender

/**
 * node:
 * - type: text
 *   text: hello world!
 * - type: title
 *   title: hello world!
 *   subtitle: sub
 *   fadein: 1
 *   stay: 1
 *   fadeout: 1
 * - type: sound
 *   sound: block_stone_break
 *   volume: 1
 *   pitch: 1
 * - type: json
 *   text:
 *   - [hello] [world!]
 *   args:
 *   - hover: hello
 *     command: say hello
 *   - hover: world!
 *     command: say world
 *
 * TabooLib
 * me.neon.mail.libs.lang.type.Type
 *
 * @author sky
 * @since 2021/6/20 10:53 下午
 */
interface Type {

    fun init(source: Map<String, Any>)

    fun send(sender: CommandSender, vararg args: Any)

    fun String.translate(sender: CommandSender, vararg args: Any): String {
        var s = this
        LangLoader.textTransfer.forEach { s = it.translate(sender, s, *args) }
        return s
    }
}