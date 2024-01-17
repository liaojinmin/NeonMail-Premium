package me.neon.mail.service

import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IMail
import me.neon.mail.common.MailDraftBuilder
import me.neon.mail.common.PlayerData
import me.neon.mail.service.channel.RedisChannel
import me.neon.mail.service.channel.ChannelInit
import me.neon.mail.utils.asyncRunner
import me.neon.mail.utils.asyncRunnerWithResult
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyPlayer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ServiceManager {


    val packetRegister: ConcurrentHashMap<Int, (message: List<String>) -> Unit> = ConcurrentHashMap()

    val sqlDataCache: ConcurrentHashMap<UUID, PlayerData> = ConcurrentHashMap()

    /**
     * 用于跨服操作，不储存玩家数据
     */
    val channel: ChannelInit by lazy {
        RedisChannel(NeonMailLoader.redisConfig)
    }

    private val sqlImpl: SQLImpl by lazy {
        SQLImpl()
    }


    @Awake(LifeCycle.ACTIVE)
    fun startInit() {
        sqlImpl.start()
       // channel.onStart()
    }

    @Awake(LifeCycle.DISABLE)
    fun close() {
        sqlImpl.close()
      //  channel.onClose()
    }


    fun List<IMail<*>>.updateState(callBack: () -> Unit) {
        asyncRunnerWithResult<Boolean> {
            onJob {
                sqlImpl.updateMailsState(this@updateState)
            }
            onError {
                NeonMailLoader.debug("""
                    updateState() ->
                       更新邮件状态时发生异常，附件领取将失效...
                """.trimIndent())
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

    fun MailDraftBuilder.deleteToSql(callBack: (Int) -> Unit) {
        asyncRunner {
            onJob {
                sqlImpl.deleteDrafts(listOf(this@deleteToSql), callBack)
            }
        }
    }

    fun MailDraftBuilder.updateToSql() {
        asyncRunner {
            onJob {
                sqlImpl.updateDrafts(listOf(this@updateToSql))
            }
        }
    }

    fun PlayerData.selectAllDraft(callBack: (MutableList<MailDraftBuilder>) -> Unit) {
        asyncRunner {
            onJob {
                draftIsLoad = true
                sqlImpl.selectDrafts(uuid, callBack)
            }
            onError {
                NeonMailLoader.debug("""
                    selectAllDraft() ->
                       查询 $user 草稿箱配置时发生异常...
                """.trimIndent())
                it.printStackTrace()
            }
            onComplete { _, timer ->
                NeonMailLoader.debug("""selectAllDraft() ->
                    |
                    |    查询 $user 草稿数据耗时 $timer ms...
                    |
                """.trimMargin())
            }
        }
    }


    fun ProxyPlayer.waitDTO() {
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
                NeonMailLoader.debug("""
                    waitDTO() -> 查询 $name 数据时发生异常...
                """.trimIndent())
                it.printStackTrace()
            }
            onComplete { _, timer ->
                NeonMailLoader.debug("""
                    waitDTO() -> 查询 $name 数据耗时 $timer ms...
                """.trimIndent())
            }
        }

    }

    fun ProxyPlayer.getPlayerData(): PlayerData? {
        return getPlayerData(this.uniqueId)
    }

    fun getPlayerData(uuid: UUID): PlayerData? {
        val data = sqlDataCache[uuid]
        if (data != null) {
            data.removeTimerOutMail()
        }
        return data
    }

    fun PlayerData.savePlayerData() {
        asyncRunner {
            onJob {
                sqlImpl.updatePlayerData(this@savePlayerData)
            }
        }
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