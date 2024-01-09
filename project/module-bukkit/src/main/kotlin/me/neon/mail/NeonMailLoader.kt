package me.neon.mail

import me.neon.mail.common.IMailDefaultImpl
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.function.console
import taboolib.common.platform.function.pluginVersion
import taboolib.module.metrics.Metrics
import java.util.*

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 21:43
 */

object NeonMailLoader {



    @Awake(LifeCycle.LOAD)
    private fun loader() {
        Metrics(16437, pluginVersion, Platform.BUKKIT)
        console().sendMessage("")
        console().sendMessage("正在加载 §3§lNeonMail§8-§9Premium  §f...  §8" + Bukkit.getVersion())
        console().sendMessage("")
    }

    @Awake(LifeCycle.ENABLE)
    private fun enabler() {
        register()
    }



    private fun register() {
        IMailDefaultImpl(IMailRegister.console, IMailRegister.console, IMailRegister.console).register()
    }

}