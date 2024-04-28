package me.neon.mail.hook

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.function.warning


/**
 * NeonMail-Premium
 * me.neon.mail.hook
 *
 * @author 老廖
 * @since 2024/1/17 20:01
 */
object ProviderRegister {

    val points: HookPoints? by lazy {
        PlatformFactory.getAPIOrNull()
    }

    val money: HookMoney? by lazy {
        PlatformFactory.getAPIOrNull()
    }

    @Awake(LifeCycle.ACTIVE)
    fun init() {
        runCatching {
            PlatformFactory.registerAPI(HookPoints().getImpl()!!)
        }.onFailure {
            warning("无法获取点券实现插件...")
        }
        runCatching {
            PlatformFactory.registerAPI(HookMoney().getImpl()!!)
        }.onFailure {
            warning("无法获取经济实现插件...")
        }
    }
}


