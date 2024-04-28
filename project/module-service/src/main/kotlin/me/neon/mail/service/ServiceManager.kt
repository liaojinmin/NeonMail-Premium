package me.neon.mail.service

import me.neon.mail.Settings
import me.neon.mail.data.IPlayerData
import me.neon.mail.utils.asyncRunner
import me.neon.mail.utils.asyncRunnerResult
import me.neon.mail.utils.asyncRunnerWithResult
import me.neon.mail.mail.IDraftBuilder
import me.neon.mail.mail.IMail
import me.neon.mail.service.channel.RedisChannel
import me.neon.mail.service.channel.ChannelInit
import me.neon.mail.service.channel.PluginChannel
import me.neon.mail.service.channel.RedisConfig
import me.neon.mail.service.packet.PlayOutMailReceivePacket
import me.neon.mail.service.sql.ConfigSql
import me.neon.mail.smtp.SmtpService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.platform.util.bukkitPlugin
import me.neon.mail.Settings.sendLang
import me.neon.mail.mail.MailUtils
import me.neon.mail.mail.parseDataName
import me.neon.mail.utils.JsonParser
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap


@RuntimeDependencies(
    RuntimeDependency(
        value = "!redis.clients:jedis:4.2.2",
        test = "redis.clients.jedis.BuilderFactory",
        relocate = ["!redis.clients", "redis.clients"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!org.slf4j:slf4j-api:2.0.8",
        test = "org.slf4j.LoggerFactory",
        relocate = ["!org.slf4j", "org.slf4j"],
        transitive = false
    ),
    RuntimeDependency(
        value = "!com.zaxxer:HikariCP:4.0.3",
        test = "com.zaxxer.hikari.HikariDataSource",
        relocate = ["!com.zaxxer.hikari", "com.zaxxer.hikari", "!org.slf4j", "org.slf4j"],
        transitive = false
    )
)
@PlatformSide(Platform.BUKKIT)
object ServiceManager {

    internal val packetRegister: ConcurrentHashMap<Int, (message: List<String>) -> Unit> = ConcurrentHashMap()

    private val sqlDataCache: ConcurrentHashMap<UUID, IPlayerData> = ConcurrentHashMap()

    val mainRegex = Regex("""[a-zA-Z0-9]+([-_.][A-Za-zd]+)*@([a-zA-Z0-9]+[-.])+[A-Za-zd]{2,5}""")

    val bindCode = mutableMapOf<UUID, String>()

    private lateinit var redisConfig: RedisConfig

    lateinit var sqlConfig: ConfigSql
        private set

    /**
     * 用于跨服操作，不储存玩家数据
     */
    val channel: ChannelInit by lazy {
        if (redisConfig.use) {
            RedisChannel(redisConfig)
        } else PluginChannel()
    }

    private val sqlImpl: SQLImpl by lazy { SQLImpl() }

    private val smtp: SmtpService by lazy {
        SmtpService(bukkitPlugin, Settings.smtpTranslate)
    }

    fun getSmtpImpl(): SmtpService? {
        if (Settings.useSmtp) {
            return smtp
        }
        return null
    }


    @Awake(LifeCycle.ENABLE)
    internal fun initService() {
        redisConfig = RedisConfig.loader(Settings.setting.getConfigurationSection("redis") ?: throw RuntimeException())
        sqlConfig = ConfigSql.loader(Settings.setting.getConfigurationSection("data_storage") ?: throw RuntimeException())
        sqlImpl.start()
        channel.onStart()
        PlayOutMailReceivePacket.registerPacket()
       // 弱验证
        var obj = JsonParser.parseString(
            URL("http://api.neonstudio.cn/api/verify/existToken?token=${Settings.setting.getString("verifyId") ?: ""}&plugin=NeonMail").readText()).asJsonObject
        if (obj.get("code").asInt == 300 || obj.get("code").asInt == 201) {
            // 未验证
            Bukkit.getConsoleSender().sendMessage("""§8[§bNeon§9Mail§8-§ePremium§8][§6验证§8]
            |
            |       §c弱验证失败，你将收到大量消息，但不影响功能使用...
            |       
        """.trimMargin())
        } else {
            // 确认是否允许使用
            if (!Settings.activate()) {
                return
            }
            // 取信息
            obj = JsonParser.parseString(
                URL("http://api.neonstudio.cn/api/verify/query?token=${Settings.setting.getString("verifyId") ?: ""}").readText()).asJsonObject.getAsJsonObject("data")
            Bukkit.getConsoleSender().sendMessage("""§8[§bNeon§9Mail§8-§ePremium§8][§6验证§8]
            |
            |        §b弱验证成功，插件现在将安静的运行...
            |        §8| §7用户名: §a${obj.get("user").asString}
            |        §8| §7归属插件: §a${obj.get("plugin").asString}
            |        §8| §7Token: §a${obj.get("token").asString}
            |        §8| §7剩余时间: §a${MailUtils.format.format(obj.get("millis").asLong)}
            |        
            |
        """.trimMargin())
        }
    }

    @Awake(LifeCycle.DISABLE)
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
                Settings.debug("updateState() -> 更新邮件状态时发生异常，附件领取将失效...")
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
                Settings.debug("""selectAllDraft() ->
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

    fun IPlayerData.selectAllDraft(callBack: (MutableList<IDraftBuilder>) -> Unit) {
        asyncRunner {
            onJob {
                draftIsLoad = true
                sqlImpl.selectDrafts(uuid, callBack)
            }
            onError {
                Settings.debug("selectAllDraft() -> 查询 $user 草稿箱配置时发生异常...")
                it.printStackTrace()
            }
            onComplete { _, timer ->
                Settings.debug("""selectAllDraft() ->
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
                Settings.debug("waitDTO() -> 查询 $name 数据时发生异常...")
                it.printStackTrace()
            }
            onComplete { _, timer ->
                Settings.debug("""
                    waitDTO() -> 查询 $name 数据耗时 ($timer)ms...
                """.trimIndent())
            }
        }
    }


    fun Player.getPlayerData(): IPlayerData? {
        return getPlayerData(this.uniqueId)
    }

    fun getOffPlayerData(uuid: UUID): CompletableFuture<IPlayerData> {
        val data = sqlDataCache[uuid]
        return if (data != null) {
            CompletableFuture.completedFuture(data)
        } else {
            asyncRunnerResult {
                onJob {
                    sqlImpl.selectPlayerData(uuid)
                }
            }
        }
    }

    fun getPlayerData(uuid: UUID): IPlayerData? {
        val data = sqlDataCache[uuid]
        if (data != null) {
            var amount = 0
            val timer = System.currentTimeMillis()
            val list = data.receiveBox.filter {
                if (timer >= Settings.getExpiryTimer(it.senderTimer)) {
                    amount++
                    true
                } else {
                    false
                }
            }
            if (amount != 0) {
                data.receiveBox.removeIf { timer >= Settings.getExpiryTimer(it.senderTimer) }
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