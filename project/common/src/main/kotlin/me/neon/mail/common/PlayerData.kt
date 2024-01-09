package me.neon.mail.common

import me.neon.mail.SetTings
import me.neon.mail.api.IMail
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.getProxyPlayer
import taboolib.module.lang.sendLang
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
    private var draftIsLoad: Boolean = false


    fun getAllDraft(): MutableList<MailDraftBuilder>{
        return draftBox
    }
    fun delDraft(uuid: UUID) {
        draftBox.removeIf { it.uuid == uuid }
    }
    fun addDraft(draftBuilder: MailDraftBuilder) {
        draftBox.add(draftBuilder)
    }
    fun checkDraft(): Boolean {
        return draftIsLoad
    }
    fun loadDraft(callBack: (MutableList<MailDraftBuilder>) -> Unit) {

    }

    fun getPlayer(): ProxyPlayer? {
        return getProxyPlayer(uuid)
    }

    fun removeTimerOutMail() {
        var amount = 0
        receiveBox.removeIf {
            if (it.senderTimer <= SetTings.getExpiryTimer(System.currentTimeMillis())) {
                amount++
                true
            } else false
        }
        if (amount != 0) {
            getPlayer()?.sendLang("玩家-邮件到期-删除", amount)
        }
    }

    fun setExtendData(data: String) {

    }

    fun getExtendData(): String {
        return ""
    }



}