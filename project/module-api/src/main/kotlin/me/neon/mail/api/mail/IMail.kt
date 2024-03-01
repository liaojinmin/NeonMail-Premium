package me.neon.mail.api.mail

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import org.bukkit.Material
import java.util.*

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 12:49
 */
interface IMail<T: IMailData>: JsonSerializer<T>, JsonDeserializer<T> {

    val uuid: UUID
    val sender: UUID
    val target: UUID

    var title: String
    var context: String
    var state: IMailState
    var senderTimer: Long
    var collectTimer: Long
    var data: T


    val permission: String
    val mainIcon: Material
    val translateType: String
    val plugin: String


    fun sendMail()

    /**
     * 发送到所有离线玩家
     */
    fun sendToOfflinePlayers()

    /**
     * 发送到所有在线玩家
     */
    fun sendToOnlinePlayers()


    fun getDataClassType(): Class<out T>

    /**
     * 克隆方法
     */
    fun cloneMail(uuid: UUID, sender: UUID, target: UUID, data: IMailData): IMail<T>



}