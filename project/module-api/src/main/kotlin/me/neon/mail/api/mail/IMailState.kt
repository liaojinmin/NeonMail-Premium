package me.neon.mail.api.mail

/**
 * NeonMail-Premium
 * me.neon.mail.api.mail
 *
 * @author 老廖
 * @since 2024/1/17 6:33
 */
enum class IMailState(val state: String) {
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
    Text("text")
}