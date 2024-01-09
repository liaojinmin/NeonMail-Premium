package me.neon.mail.api

import taboolib.common.platform.ProxyPlayer

/**
 * NeonMail-Premium
 * me.neon.mail.provider
 *
 * @author 老廖
 * @since 2024/1/6 16:27
 */
class DataTypeEmpty: IMailDataType {
    override fun getAppendixInfo(player: ProxyPlayer, pad: String): String {
        return IMail.IMailState.Text.state
    }

    override fun hasAppendix(): Boolean {
        return false
    }

}