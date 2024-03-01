package me.neon.mail.common

import me.neon.mail.api.PlayerData
import me.neon.mail.api.mail.IDraftBuilder
import me.neon.mail.api.mail.IMail
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 16:12
 */
data class PlayerDataImpl(
    override val uuid: UUID,
    override val user: String,
    override var mail: String = ""
): PlayerData {

    override var isLoader: Boolean = false

    override val senderBox: CopyOnWriteArrayList<IMail<*>> = CopyOnWriteArrayList()
    override val receiveBox: CopyOnWriteArrayList<IMail<*>> = CopyOnWriteArrayList()

    /**
     * 草稿箱
     * 限制大小 20
     */
    private val draftBox: MutableList<IDraftBuilder> = ArrayList(20)
    internal var draftIsLoad: Boolean = false


    override fun getAllDraft(): MutableList<IDraftBuilder> {
        return draftBox
    }
    override fun delDraft(uuid: UUID) {
        draftBox.removeIf { it.uuid == uuid }
    }
    override fun addDraft(draftBuilder: IDraftBuilder) {
        draftBox.add(draftBuilder)
    }
    override fun applyDraft(data: MutableList<IDraftBuilder>) {
        draftBox.clear()
        draftBox.addAll(data)
    }
    override fun checkDraftIsLoad(): Boolean {
        return draftIsLoad
    }

    override fun setExtendData(data: String) {

    }

    override fun getExtendData(): String {
        return ""
    }



}