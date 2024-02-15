package me.neon.mail.api.mail

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 12:49
 */
interface IMail<T: IMailDataType>: JsonSerializer<T>, JsonDeserializer<T> {

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
    val mailType: String
    val plugin: String


    fun sendMail()

    /**
     * 像全部服务器玩家发送邮件，
     */
    fun sendGlobalMail()

    fun checkClaimCondition(player: Player): Boolean

    fun giveAppendix(player: Player): Boolean

    fun getMailClassType(): Class<out IMail<T>>

    fun getDataClassType(): Class<out T>

    /**
     * 创建附件实体
     */
    fun createData(): T
    /**
     * 克隆方法
     */
    fun cloneMail(uuid: UUID, sender: UUID, target: UUID, data: IMailDataType): IMail<T>

    /**
     * 注册邮件
     */
    fun register() {
        IMailRegister.register(this)
    }

    /**
     * 取消注册
     */
    fun unregister() {
        IMailRegister.unregister(this)
    }

}