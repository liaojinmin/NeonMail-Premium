package me.neon.mail.data

import me.neon.mail.mail.IDraftBuilder
import me.neon.mail.mail.IMail
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * NeonMail-Premium
 * me.neon.mail.api
 *
 * @author 老廖
 * @since 2024/1/22 5:10
 */
interface IPlayerData {

    val uuid: UUID

    val user: String

    var mail: String

    val senderBox: CopyOnWriteArrayList<IMail<*>>

    val receiveBox: CopyOnWriteArrayList<IMail<*>>

    var isLoader: Boolean

    var draftIsLoad: Boolean

    fun createNewInstance(uuid: UUID, user: String): IPlayerData

    fun getAllDraft(): MutableList<IDraftBuilder>

    fun applyDraft(data: MutableList<IDraftBuilder>)

    fun addDraft(draftBuilder: IDraftBuilder)

    fun delDraft(uuid: UUID)

    fun setExtendData(data: String)

    fun getExtendData(): String


}