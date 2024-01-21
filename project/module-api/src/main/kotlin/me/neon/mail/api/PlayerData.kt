package me.neon.mail.api

import me.neon.mail.api.mail.IDraftBuilder
import me.neon.mail.api.mail.IMail
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * NeonMail-Premium
 * me.neon.mail.api
 *
 * @author 老廖
 * @since 2024/1/22 5:10
 */
interface PlayerData {

    val uuid: UUID

    val user: String

    var mail: String

    val senderBox: CopyOnWriteArrayList<IMail<*>>

    val receiveBox: CopyOnWriteArrayList<IMail<*>>

    fun getAllDraft(): MutableList<IDraftBuilder>
    fun applyDraft(data: MutableList<IDraftBuilder>)
    fun addDraft(draftBuilder: IDraftBuilder)
    fun delDraft(uuid: UUID)
    fun checkDraftIsLoad(): Boolean

    fun setExtendData(data: String)

    fun getExtendData(): String


}