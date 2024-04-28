package me.neon.mail.service.packet

import me.neon.mail.service.ServiceManager
import org.bukkit.Bukkit
import me.neon.mail.Settings.sendLang
import java.util.UUID


/**
 * NeonMail-Premium
 * me.neon.mail.service.packet
 *
 * 通知全服某个玩家需要收取邮件
 * 玩家所在服接受到后，主动请问数据库获取更新数据。
 *
 * @author 老廖
 * @since 2024/1/18 2:37
 */
class PlayOutMailReceivePacket(
    val player: UUID,
    val mail: UUID
): AbstractPacket() {

    override fun getPacketIndexId(): Int {
        return packetIndexID
    }

    override fun senderPacket() {
        writeUUID(player)
        writeUUID(mail)
        super.senderPacket()
    }

    companion object {

        private const val packetIndexID: Int = 10

        fun registerPacket() {
            ServiceManager.packetRegister[packetIndexID] = PlayOutMailReceivePacket::receivePacket
        }

        private fun receivePacket(message: List<String>) {
            if (message.size < 2) error("数据包消息错误 -> message.size() < 2")
            val player = Bukkit.getPlayer(UUID.fromString(message[0])) ?: return
            ServiceManager.selectMail(UUID.fromString(message[1])) {
                if (it == null) {
                    throw RuntimeException("在查询玩家跨服邮件数据时发生异常 null")
                }
                val data = ServiceManager.getPlayerData(player.uniqueId) ?: return@selectMail
                val info = if (it.context.length >= 11) it.context.substring(0, 10) + "§8..." else it.context
                player.sendLang("玩家-接收邮件-送达", it.title, info.replace(";", ""))
                data.receiveBox.add(it)
            }
        }
    }
}