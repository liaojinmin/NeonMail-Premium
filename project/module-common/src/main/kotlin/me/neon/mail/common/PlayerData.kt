package me.neon.mail.common

import me.neon.mail.api.mail.IMail
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.getProxyPlayer
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 16:12
 */
data class PlayerData(
    val uuid: UUID,
    val user: String,
    var mail: String = ""
) {

    val senderBox: CopyOnWriteArrayList<IMail<*>> = CopyOnWriteArrayList()
    val receiveBox: CopyOnWriteArrayList<IMail<*>> = CopyOnWriteArrayList()

    /**
     * 草稿箱
     * 限制大小 20
     */
    private val draftBox: MutableList<MailDraftBuilder> = ArrayList(20)
    internal var draftIsLoad: Boolean = false


    fun getAllDraft(): MutableList<MailDraftBuilder>{
        return draftBox
    }
    fun delDraft(uuid: UUID) {
        draftBox.removeIf { it.uuid == uuid }
    }
    fun addDraft(draftBuilder: MailDraftBuilder) {
        draftBox.add(draftBuilder)
    }
    fun applyDraft(data: MutableList<MailDraftBuilder>) {
        draftBox.clear()
        draftBox.addAll(data)
    }
    fun checkDraft(): Boolean {
        return draftIsLoad
    }

    fun getPlayer(): ProxyPlayer? {
        return getProxyPlayer(uuid)
    }


    fun setExtendData(data: String) {

    }

    fun getExtendData(): String {
        return ""
    }



}