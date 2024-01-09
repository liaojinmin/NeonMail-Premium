package me.neon.mail


import me.neon.mail.api.IMail
import me.neon.mail.common.MailDraftBuilder
import me.neon.mail.service.channel.RedisChannel
import me.neon.mail.common.PlayerData
import me.neon.mail.service.SQLImpl
import me.neon.mail.service.channel.ChannelInit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyPlayer
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

object ServiceManager {


    val packetRegister: ConcurrentHashMap<Int, (message: List<String>) -> Unit> = ConcurrentHashMap()

    val sqlDataCache: ConcurrentHashMap<UUID, PlayerData> = ConcurrentHashMap()

    /**
     * 用于跨服操作，不储存玩家数据
     */
    val channel: ChannelInit by lazy {
        RedisChannel(SetTings.redisConfig)
    }

    private val sqlImpl: SQLImpl by lazy {
        SQLImpl()
    }


    @Awake(LifeCycle.ACTIVE)
    fun startInit() {
        sqlImpl.start()
        channel.onStart()
    }

    @Awake(LifeCycle.DISABLE)
    fun close() {
      //  PlayOutServerOfflinePacket().sender()
        sqlImpl.close()
        channel.onClose()
    }


    fun List<IMail<*>>.updateState() {
        CompletableFuture.runAsync {
            sqlImpl.updateMailsState(this)
        }
    }

    fun List<IMail<*>>.updateDel(isSenderDel: Boolean) {
        CompletableFuture.runAsync {
            sqlImpl.updateMailsDel(this, isSenderDel)
        }
    }

    fun IMail<*>.insert() {
        CompletableFuture.runAsync {
            sqlImpl.insertMail(this)
        }
    }

    fun MailDraftBuilder.deleteToSql(callBack: (Int) -> Unit) {
        CompletableFuture.runAsync {
            sqlImpl.deleteDrafts(listOf(this), callBack)
        }
    }

    fun MailDraftBuilder.updateToSql() {
        CompletableFuture.runAsync {
            sqlImpl.updateDrafts(listOf(this))
        }
    }

    fun PlayerData.selectAllDraft(callBack: (MutableList<MailDraftBuilder>) -> Unit) {
        CompletableFuture.runAsync {
            sqlImpl.selectDrafts(this.uuid, callBack)
        }
    }


    fun ProxyPlayer.waitDTO() {
        CompletableFuture.runAsync {
            sqlImpl.selectPlayerData(this) {
                sqlDataCache[this.uniqueId] = it
                val data = sqlImpl.selectMail(this.uniqueId)
                it.senderBox.addAll(data.first)
                it.receiveBox.addAll(data.second)
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
        CompletableFuture.runAsync {
            sqlImpl.updatePlayerData(this)
        }
    }
    fun savePlayerData(uuid: UUID, delCache: Boolean = false) {
        if (delCache) {
            sqlDataCache.remove(uuid)?.let {
                CompletableFuture.runAsync {
                    sqlImpl.updatePlayerData(it)
                }
            }
        } else {
            sqlDataCache[uuid]?.let {
                CompletableFuture.runAsync {
                    sqlImpl.updatePlayerData(it)
                }
            }
        }
    }





}