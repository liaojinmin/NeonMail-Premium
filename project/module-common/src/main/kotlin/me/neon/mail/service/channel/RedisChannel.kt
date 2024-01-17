package me.neon.mail.service.channel

import me.neon.mail.NeonMailLoader
import me.neon.mail.service.ServiceManager
import me.neon.mail.service.packet.DataPacket
import me.neon.mail.service.packet.IPacket
import me.neon.mail.service.packet.PacketSub
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPubSub
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.service.PlatformExecutor
import java.util.logging.Level

/**
 * 作者: 老廖
 * 时间: 2022/10/15
 **/
class RedisChannel(
    private val config: RedisConfig
): ChannelInit() {

    private var subscribeTask: PlatformExecutor.PlatformTask? = null

    private val jedisPool by lazy {
        if (config.password.isEmpty()) {
            val poolConfig = JedisPoolConfig()
            poolConfig.maxTotal = 10
            JedisPool(poolConfig, config.host, config.port, 5000, config.ssl)
        } else {
            JedisPool(JedisPoolConfig(), config.host, config.port, 5000, config.password, config.ssl)
        }
    }

    override fun sendPacket(packet: IPacket) {
        getRedisConnection().use {
            val data = packet.data
            if (data != null) {
                val pig = it.pipelined()
                pig.setex(data.key, data.timer, data.data)
                pig.publish(packet.getTargetChannel(), (packet as PacketSub).getMessageData())
            } else {
                it.publish(packet.getTargetChannel(),  (packet as PacketSub).getMessageData())
            }
        }
    }

    override fun getPacketData(key: ByteArray): ByteArray? {
        getRedisConnection().use {
            val data = it.get(key)
            if (data != null && data.isNotEmpty()) {
                return data
            }
            return null
        }
    }

    private fun getRedisConnection() : Jedis {
        return this.jedisPool.resource
    }


    override fun onStart() {
        subscribeTask?.cancel()
        subscribeTask = submitAsync {
            getRedisConnection().use {
                if (it.isConnected) {
                    logger.log(Level.INFO, "serviceMessage 消息通道注册成功")
                } else {
                    logger.log(Level.WARNING, "无法与 Redis 服务器建立连接")
                    return@submitAsync
                }
                it.subscribe(object : JedisPubSub() {
                    // message = 1~server&data~data
                    override fun onMessage(channel: String, message: String) {
                        try {
                            // 找 & 符号
                            val index = message.indexOf(DataPacket.headerSplit)
                            // 判断是否存在并且符号后是否还有内容
                            if (index == -1 || index + 1 > message.length) return
                            val splitIndex = message.indexOf(DataPacket.packetSplit)
                            if (message.substring(splitIndex+1, index) == NeonMailLoader.clusterId) {
                                logger.log(Level.INFO, " 已暂停解析，这个包是本服发送...")
                                return
                            }
                            val key = message.substring(0, message.indexOf(DataPacket.packetSplit)).toInt()
                            val callBack = ServiceManager.packetRegister[key]
                            if (callBack != null) {
                                // list = data~data
                                val list = message.substring(index + 1).split(DataPacket.packetSplit)
                                // 另起线程，避免消息阻塞
                                submitAsync {
                                    callBack.invoke(list)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }, pluginId)
            }
        }
    }

    override fun onClose() {
        jedisPool.close()
        subscribeTask?.cancel()
    }





}

