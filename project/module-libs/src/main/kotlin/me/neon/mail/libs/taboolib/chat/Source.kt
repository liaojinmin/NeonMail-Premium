package me.neon.mail.libs.taboolib.chat

import org.bukkit.command.CommandSender


/**
 * TabooLib
 * me.neon.mail.libs.chat.Source
 *
 * @author 坏黑
 * @since 2023/2/9 21:10
 */
interface Source {

    /** 转换为原始信息 */
    fun toRawMessage(): String

    /** 转换为带颜色的纯文本 */
    fun toLegacyText(): String

    /** 转换为纯文本 */
    fun toPlainText(): String

    /** 广播给所有玩家 */
    fun broadcast()

    /** 发送给玩家 */
    fun sendTo(sender: CommandSender)
}