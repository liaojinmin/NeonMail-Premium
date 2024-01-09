package me.neon.mail.api

import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import me.neon.mail.IMailRegister
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.getProxyPlayer
import taboolib.library.xseries.XMaterial
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

    var senderDel: Int
    var targetDel: Int

    val permission: String
    val mainIcon: XMaterial
    val mailType: String
    val plugin: String

    fun sendMail()

    fun sendCondition(player: ProxyPlayer, data: T): Boolean

    fun giveAppendix(player: ProxyPlayer): Boolean

    fun getAppendixInfo(player: ProxyPlayer = getProxyPlayer(target) ?: getProxyPlayer(sender) ?: error("找不到用于解析的玩家")): String

    fun getMailClassType(): Class<out IMail<T>>

    fun getDataType(): Class<out T>


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

}