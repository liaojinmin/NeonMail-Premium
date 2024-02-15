package me.neon.mail.libs.taboolib.chat

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.chat.ComponentSerializer
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

/**
 * TabooLib
 * me.neon.mail.libs.chat.Components
 *
 * @author 坏黑
 * @since 2023/2/9 20:16
 */

object ChatLoader {

    val inputs = ConcurrentHashMap<String, (String) -> Unit>()

    /** 创建空白块 */
    fun empty(): ComponentText = DefaultComponent()

    /** 创建文本块 */
    fun text(text: String): ComponentText = DefaultComponent().append(text)

    /** 创建分数文本块 */
    fun score(name: String, objective: String): ComponentText = DefaultComponent().appendScore(name, objective)

    /** 创建按键文本块 */
    fun keybind(key: String): ComponentText = DefaultComponent().appendKeybind(key)

    /** 创建选择器文本块 */
    fun selector(selector: String): ComponentText = DefaultComponent().appendSelector(selector)

    /** 创建翻译文本块 */
    fun translation(text: String, vararg obj: Any): ComponentText = DefaultComponent().appendTranslation(text, *obj)

    /** 创建翻译文本块 */
    fun translation(text: String, obj: List<Any>): ComponentText = DefaultComponent().appendTranslation(text, obj)

    /** 从原始信息中读取 */
    fun parseRaw(text: String): ComponentText = DefaultComponent(ComponentSerializer.parse(text).toList())

    fun toLegacyString(vararg components: BaseComponent): String {
        val builder = StringBuilder()
        val newArray: Array<out BaseComponent> = components
        val size = components.size
        for (i in 0 until size) {
            builder.append(toLegacyString(newArray[i]))
        }
        return builder.toString()
    }

    private fun toLegacyString(component: BaseComponent): String {
        val builder = StringBuilder()
        toLegacyString1(component, builder)
        return builder.toString()
    }

    private fun toLegacyString1(component: BaseComponent, builder: StringBuilder): String {
        if (component is TranslatableComponent) {
            val method = BaseComponent::class.memberFunctions
                .find { it.name == "toLegacyText" && it.parameters.size == 1 && it.parameters[0] == java.lang.StringBuilder::class.java}
            if (method != null) {
                method.isAccessible = true
                method.call(component, builder)
            }
            //  component.invokeMethod("toLegacyText", builder)
        } else {
            addFormat(component, builder)
            when (component) {
                is TextComponent -> builder.append(component.text)
                is KeybindComponent -> builder.append(component.keybind)
                is ScoreComponent -> builder.append(component.value)
                is SelectorComponent -> builder.append(component.selector)
            }
        }
        component.extra?.forEach { toLegacyString1(it, builder) }
        return builder.toString()
    }

    private fun addFormat(component: BaseComponent, builder: StringBuilder) {
        if (component.colorRaw != null) {
            builder.append(component.color)
        }
        if (component.isBold) {
            builder.append(ChatColor.BOLD)
        }
        if (component.isItalic) {
            builder.append(ChatColor.ITALIC)
        }
        if (component.isUnderlined) {
            builder.append(ChatColor.UNDERLINE)
        }
        if (component.isStrikethrough) {
            builder.append(ChatColor.STRIKETHROUGH)
        }
        if (component.isObfuscated) {
            builder.append(ChatColor.MAGIC)
        }
    }

}