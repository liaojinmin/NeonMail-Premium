package me.neon.mail.data

import me.neon.mail.mail.IDraftBuilder
import me.neon.mail.mail.IMail
import me.neon.mail.mail.MailRegister
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
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
): IPlayerData {

    override var isLoader: Boolean = false

    override var draftIsLoad: Boolean = false

    override val senderBox: CopyOnWriteArrayList<IMail<*>> = CopyOnWriteArrayList()
    override val receiveBox: CopyOnWriteArrayList<IMail<*>> = CopyOnWriteArrayList()

    /**
     * 草稿箱
     * 限制大小 20
     */
    private val draftBox: MutableList<IDraftBuilder> = ArrayList(20)


    override fun createNewInstance(uuid: UUID, user: String): IPlayerData {
        return PlayerDataImpl(uuid, user)
    }

    override fun getAllDraft(): MutableList<IDraftBuilder> {
        return draftBox
    }
    override fun delDraft(uuid: UUID) {
        draftBox.removeIf { it.unique == uuid }
    }
    override fun addDraft(draftBuilder: IDraftBuilder) {
        draftBox.add(draftBuilder)
    }
    override fun applyDraft(data: MutableList<IDraftBuilder>) {
        draftBox.clear()
        draftBox.addAll(data)
    }

    override fun setExtendData(data: String) {

    }

    override fun getExtendData(): String {
        return ""
    }

    companion object {
        @Awake(LifeCycle.INIT)
        fun init() {
            PlatformFactory.registerAPI<IPlayerData>(PlayerDataImpl(MailRegister.console, ""))
        }
    }



}