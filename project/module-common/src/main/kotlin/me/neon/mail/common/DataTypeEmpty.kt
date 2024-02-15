package me.neon.mail.common

import me.neon.mail.api.mail.IMailDataType
import me.neon.mail.api.mail.IMailState
import org.bukkit.entity.Player

/**
 * NeonMail-Premium
 * me.neon.mail.provider
 *
 * @author 老廖
 * @since 2024/1/6 16:27
 */
class DataTypeEmpty: IMailDataType {

    override fun getAppendixInfo(player: Player?, pad: String, refresh: Boolean): String {
        return pad + IMailState.Text.state
    }

    override fun getAppendixInfo(pad: String): String {
        return pad + IMailState.Text.state
    }

    override fun hasAppendix(): Boolean {
        return false
    }

}