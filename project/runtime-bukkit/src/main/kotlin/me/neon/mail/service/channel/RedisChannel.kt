package me.neon.mail.service.channel

import me.neon.mail.NeonMailLoader
import me.neon.mail.service.packet.IPacket
import me.neon.mail.service.packet.AbstractPacket
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPubSub
import taboolib.common.platform.function.info
import taboolib.platform.util.bukkitPlugin
import java.util.logging.Level

/**
 * 作者: 老廖
 * 时间: 2022/10/15
 **/
class RedisChannel(
    private val config: RedisConfig
): ChannelInit() {

    private var subscribeTask: BukkitTask? = null

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
            it.publish(serviceChannel,  (packet as AbstractPacket).getMessageData())
        }
    }

    private fun getRedisConnection() : Jedis {
        return this.jedisPool.resource
    }


    override fun onStart() {
        subscribeTask?.cancel()
        subscribeTask = Bukkit.getScheduler().runTaskAsynchronously(bukkitPlugin, Runnable {
            getRedisConnection().use {
                if (it.isConnected) {
                    info("serviceMessage 消息通道注册成功")
                } else {
                    info("无法与 Redis 服务器建立连接")
                    return@Runnable
                }
                it.subscribe(object : JedisPubSub() {
                    // message = 1~server&data~data
                    override fun onMessage(channel: String, message: String) {
                        readMessage(message)
                    }
                }, serviceChannel)
            }
        })
    }

    override fun onClose() {
        subscribeTask?.cancel()
        jedisPool.close()
    }





}

