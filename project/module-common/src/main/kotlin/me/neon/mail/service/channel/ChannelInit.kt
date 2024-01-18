package me.neon.mail.service.channel


import me.neon.mail.NeonMailLoader
import me.neon.mail.service.ServiceManager
import me.neon.mail.service.packet.AbstractPacket
import me.neon.mail.service.packet.IPacket
import taboolib.common.platform.function.pluginId
import java.util.logging.Level

import java.util.logging.Logger


abstract class ChannelInit {

    val serviceChannel = pluginId

    val bungeeCord = "BungeeCord"

    val logger: Logger = Logger.getLogger(pluginId)

    abstract fun sendPacket(packet: IPacket)

    abstract fun onStart()

    abstract fun onClose()

    protected fun readMessage(message: String) {
        try {
            // 找 & 符号
            val index = message.indexOf(AbstractPacket.headerSplit)
            // 判断是否存在并且符号后是否还有内容
            if (index == -1 || index + 1 > message.length) return
            val splitIndex = message.indexOf(AbstractPacket.packetSplit)
            if (message.substring(splitIndex+1, index) == NeonMailLoader.clusterId) {
                logger.log(Level.INFO, " 已暂停解析，这个包是本服发送...")
                return
            }
            val key = message.substring(0, message.indexOf(AbstractPacket.packetSplit)).toInt()
            // list = data~data
            ServiceManager.packetRegister[key]?.invoke(
                message.substring(index + 1).split(AbstractPacket.packetSplit)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}