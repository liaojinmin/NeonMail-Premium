package me.neon.mail

import me.neon.mail.api.PlayerData
import me.neon.mail.api.mail.IDraftBuilder
import me.neon.mail.api.mail.IMail
import me.neon.mail.common.PlayerDataImpl
import me.neon.mail.libs.taboolib.lang.sendLang
import me.neon.mail.libs.utils.io.asyncRunner
import me.neon.mail.libs.utils.io.asyncRunnerWithResult
import me.neon.mail.service.SQLImpl
import me.neon.mail.service.channel.RedisChannel
import me.neon.mail.service.channel.ChannelInit
import me.neon.mail.service.channel.PluginChannel
import me.neon.mail.service.packet.PlayOutMailReceivePacket
import me.neon.mail.smtp.SmtpService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ServiceManager {


    val packetRegister: ConcurrentHashMap<Int, (message: List<String>) -> Unit> = ConcurrentHashMap()

    private val sqlDataCache: ConcurrentHashMap<UUID, PlayerData> = ConcurrentHashMap()

    /**
     * 用于跨服操作，不储存玩家数据
     */
    internal val channel: ChannelInit by lazy {
        if (NeonMailLoader.redisConfig.use) {
            RedisChannel(NeonMailLoader.redisConfig)
        } else PluginChannel()
    }

    private val sqlImpl: SQLImpl by lazy { SQLImpl() }

    private val smtp: SmtpService by lazy {
        SmtpService(NeonMailLoader.plugin, NeonMailLoader.smtpTranslate)
    }

    fun getSmtpImpl(): SmtpService? {
        if (NeonMailLoader.useSmtp) {
            return smtp
        }
        return null
    }

    internal fun initService() {
        sqlImpl.start()
        channel.onStart()
        PlayOutMailReceivePacket.registerPacket()
    }

    internal fun closeService() {
        sqlImpl.close()
        channel.onClose()
    }


    fun List<IMail<*>>.updateState(callBack: () -> Unit) {
        asyncRunnerWithResult<Boolean> {
            onJob {
                sqlImpl.updateMailsState(this@updateState)
            }
            onError {
                NeonMailLoader.debug("updateState() -> 更新邮件状态时发生异常，附件领取将失效...")
                it.printStackTrace()
            }
            onComplete { run, _ ->
                if (run != null && run) {
                    callBack.invoke()
                }
            }
        }
    }

    fun List<IMail<*>>.deleteMails(isSenderDel: Boolean) {
        asyncRunner {
            onJob {
                sqlImpl.deleteMails(this@deleteMails, isSenderDel)
            }
        }
    }

    fun selectMail(uuid: UUID, callBack: (IMail<*>?) -> Unit) {
        asyncRunnerWithResult {
            onJob {
                sqlImpl.selectMail(uuid)
            }
            onComplete { mail, timer ->
                callBack.invoke(mail)
                NeonMailLoader.debug("""selectAllDraft() ->
                    |
                    |    查询 $uuid 邮件数据耗时 ($timer)ms...
                    |
                """.trimMargin())
            }
        }
    }
    fun IMail<*>.deleteMail(isSenderDel: Boolean) {
        asyncRunner {
            onJob {
                sqlImpl.deleteMails(listOf(this@deleteMail), isSenderDel)
            }
        }
    }
    fun IMail<*>.insertMail() {
        asyncRunner {
            onJob {
                sqlImpl.insertMail(this@insertMail)
            }
        }
    }

    fun IDraftBuilder.deleteToSql(callBack: (Int) -> Unit) {
        asyncRunner {
            onJob {
                sqlImpl.deleteDrafts(listOf(this@deleteToSql), callBack)
            }
        }
    }

    fun IDraftBuilder.updateToSql() {
        asyncRunner {
            onJob {
                sqlImpl.updateDrafts(listOf(this@updateToSql))
            }
        }
    }

    fun PlayerDataImpl.selectAllDraft(callBack: (MutableList<IDraftBuilder>) -> Unit) {
        asyncRunner {
            onJob {
                draftIsLoad = true
                sqlImpl.selectDrafts(uuid, callBack)
            }
            onError {
                NeonMailLoader.debug("selectAllDraft() -> 查询 $user 草稿箱配置时发生异常...")
                it.printStackTrace()
            }
            onComplete { _, timer ->
                NeonMailLoader.debug("""selectAllDraft() ->
                    |
                    |    查询 $user 草稿数据耗时 ($timer)ms...
                    |
                """.trimMargin())
            }
        }
    }


    fun Player.waitDTO() {
        asyncRunner {
            onJob {
                sqlImpl.selectPlayerData(this@waitDTO) {
                    sqlDataCache[uniqueId] = it
                    val data = sqlImpl.selectMails(uniqueId)
                    it.senderBox.addAll(data.first)
                    it.receiveBox.addAll(data.second)
                }
            }
            onError {
                NeonMailLoader.debug("waitDTO() -> 查询 $name 数据时发生异常...")
                it.printStackTrace()
            }
            onComplete { _, timer ->
                NeonMailLoader.debug("""
                    waitDTO() -> 查询 $name 数据耗时 ($timer)ms...
                """.trimIndent())
            }
        }

    }

    fun Player.getPlayerData(): PlayerData? {
        return getPlayerData(this.uniqueId)
    }

    fun getPlayerData(uuid: UUID): PlayerData? {
        val data = sqlDataCache[uuid]
        if (data != null) {
            var amount = 0
            val timer = System.currentTimeMillis()
            val list = data.receiveBox.filter {
                if (timer >= NeonMailLoader.getExpiryTimer(it.senderTimer)) {
                    amount++
                    true
                } else {
                    false
                }
            }
            if (amount != 0) {
                data.receiveBox.removeIf { timer >= NeonMailLoader.getExpiryTimer(it.senderTimer) }
                Bukkit.getPlayer(data.uuid)?.sendLang("玩家-邮件到期-删除", amount)
                list.deleteMails(false)
            }
        }
        return data
    }


    fun savePlayerData(uuid: UUID, delCache: Boolean = false) {
        if (delCache) {
            sqlDataCache.remove(uuid)?.let {
                asyncRunner {
                    onJob {
                        sqlImpl.updatePlayerData(it)
                    }
                }
            }
        } else {
            sqlDataCache[uuid]?.let {
                asyncRunner {
                    onJob {
                        sqlImpl.updatePlayerData(it)
                    }
                }
            }
        }
    }





}