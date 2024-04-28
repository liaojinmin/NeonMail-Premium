package me.neon.mail

import me.neon.mail.data.IPlayerData
import me.neon.mail.mail.IDraftBuilder
import taboolib.common.platform.PlatformFactory

/**
 * NeonMail
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/3/5 7:22
 */
object NeonMailAPI {

    val dataImpl: IPlayerData by lazy {
        PlatformFactory.getAPI()
    }

    val draftImpl: IDraftBuilder by lazy {
        PlatformFactory.getAPI()
    }

}