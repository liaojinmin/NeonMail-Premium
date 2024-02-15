package me.neon.mail.smtp

import me.neon.mail.api.mail.IMail
import me.neon.mail.libs.taboolib.chat.HexColor.uncolored
import me.neon.mail.libs.utils.io.asyncRunner
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class SmtpService(
    private val plugin: Plugin,
    private val param: MutableMap<String, String>
) {
    private val account = param["account"] ?: error("找不到发件账号")
    private val password = param["password"] ?: error("找不到smt授权码") // "JRKHOKSYAPSOUHOS"
    private val personal = param["personal"] ?: "NeonMail-Premium"
    private val subjects = param["subjects"] ?: "NeonMail-收件提醒"
    private val html by lazy {
        File(plugin.dataFolder, "smtp/web.html").also {
            if (!it.exists()) plugin.saveResource("smtp/web.html", true)
        }.toHtmlString()
    }

    private val bind by lazy {
        File(plugin.dataFolder, "smtp/bind.html").also {
            if (!it.exists()) plugin.saveResource("smtp/bind.html", true)
        }.toHtmlString()
    }

    private val properties by lazy {
        Properties().apply { putAll(param) }
    }

    private val authenticator: Authenticator by lazy {
        object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(account, password)
            }
        }
    }

    fun sendBindEmail(player: Player, code: String, toMail: String) {
        asyncRunner {
            createConnection { transport, session ->
                val out = bind.replace("{player}", player.name)
                    .replace("{code}", code)
                val message = createMimeMessage(session)
                message.setRecipient(Message.RecipientType.TO, InternetAddress(toMail))
                message.setContent(out, "text/html;charset=gb2312")
                transport.sendMessage(message, message.allRecipients)
            }
        }
    }

    fun sendEmail(target: String, mail: IMail<*>) {
        asyncRunner {
            createConnection { transport, session ->
                val message = createMimeMessage(session)
                message.setRecipient(Message.RecipientType.TO, InternetAddress(target))
                message.setContent(createMailInfo(Bukkit.getOfflinePlayer(mail.target), mail), "text/html;charset=gb2312")
                transport.sendMessage(message, message.allRecipients)
            }
        }
    }

    fun sendEmail(mails: MutableMap<String, IMail<*>>) {
        asyncRunner {
            createConnection { transport, session ->
                val message = createMimeMessage(session)
                mails.forEach { (target, mail) ->
                    message.setRecipient(Message.RecipientType.TO, InternetAddress(target))
                    message.setContent(createMailInfo(Bukkit.getOfflinePlayer(mail.target), mail), "text/html;charset=gb2312")
                    transport.sendMessage(message, message.allRecipients)
                }
            }
        }
    }

    private fun createMimeMessage(session: Session): MimeMessage {
        return MimeMessage(session).apply {
            setFrom(InternetAddress(account, personal, "UTF-8"))
            subject = subjects
        }
    }

    private fun createConnection(function: (Transport, Session) -> Unit) {
        val session = Session.getInstance(properties, authenticator)
        session.transport.use { function.invoke(this, session) }
    }

    private fun createMailInfo(player: OfflinePlayer, iMail: IMail<*>): String {
        return html.replace("{name}", player.name ?: player.uniqueId.toString())
            .replace("{title}", iMail.title.uncolored())
            .replace("{text}", iMail.context.uncolored())
            .replace("{app}", iMail.data.getAppendixInfo(Bukkit.getPlayer(player.uniqueId)).uncolored())
    }

    private fun File.toHtmlString(): String {
        // 获取HTML文件流
        val htmlSb = StringBuffer()
        BufferedReader(
            InputStreamReader(
                FileInputStream(this), "UTF-8"
            )
        ).use {
            while (it.ready()) {
                htmlSb.append(it.readLine())
            }
        }
        return htmlSb.toString()
    }

    private fun <T> Transport.use(func: Transport.() -> T): T {
        return try {
            func(this)
        } catch (ex: Exception) {
            throw ex
        } finally {
            close()
        }
    }
}


