package me.neon.mail.mail

/**
 * NeonMail-Premium
 * me.neon.mail.api.mail
 *
 * @author 老廖
 * @since 2024/1/17 6:33
 */
enum class MailState(val type: String) {

    /**
     * 已领取
     */
    Acquired("已提取"),

    /**
     * 未领取
     */
    NotObtained("未提取"),

    /**
     * 纯文本邮件
     */
    Text("纯文本"),

    /**
     * 纯文本邮件 已读
     */
    TextAcquired("纯文本已读")
}