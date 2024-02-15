package me.neon.mail.api.mail

import org.bukkit.entity.Player

/**
 * NeonMail-Premium
 * me.neon.mail.api
 *
 * @author 老廖
 * @since 2024/1/6 12:27
 */
interface IMailDataType {




    fun getAppendixInfo(player: Player?, pad: String = "", refresh: Boolean = false): String

    fun getAppendixInfo(pad: String = ""): String

    fun hasAppendix(): Boolean


}