package me.neon.mail.service.channel


import me.neon.mail.service.packet.IPacket
import taboolib.common.platform.function.pluginId

import java.util.logging.Logger


abstract class ChannelInit {

    val serviceChannel = pluginId

    val logger: Logger = Logger.getLogger(pluginId)

    abstract fun sendPacket(packet: IPacket)

    abstract fun getPacketData(key: ByteArray): ByteArray?

    abstract fun onStart()

    abstract fun onClose()




}