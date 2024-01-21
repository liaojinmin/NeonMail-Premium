package me.neon.mail.api.mail

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * NeonMail-Premium
 * me.neon.mail.api.mail
 *
 * @author 老廖
 * @since 2024/1/22 5:38
 */
interface IDraftBuilder {

    val sender: UUID

    val type: String

    val uuid: UUID

    var title: String

    val context: MutableList<String>
    fun checkGlobalModel(): Boolean
    fun senderMail()
    fun addTarget(uuid: UUID, dataType: IMailDataType)
    fun getTargets(): ConcurrentHashMap<UUID, IMailDataType>
    fun changeGlobalModel(): IMailDataType
    fun isAllowSender(): Boolean
    fun isAllowDeletion(): Boolean
}