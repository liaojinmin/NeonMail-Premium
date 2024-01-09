package me.neon.mail.service

import me.neon.mail.api.IMail
import me.neon.mail.IMailRegister
import me.neon.mail.SetTings
import me.neon.mail.common.MailDraftBuilder
import me.neon.mail.common.PlayerData
import me.neon.mail.service.sql.*
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import java.sql.Connection
import java.util.UUID

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 16:10
 */
class SQLImpl {

    private val dataSub by lazy {
        if (SetTings.sqlConfig.use_type.equals("mysql", ignoreCase = true)) {
            Mysql(SetTings.sqlConfig)
        } else Sqlite(SetTings.sqlConfig)
    }

    fun start() {
        if (dataSub.isActive) return
        dataSub.onStart()
        if (dataSub.isActive) {
            dataSub.createTab {
                getConnection().use {
                    createStatement().action { statement ->
                        if (dataSub is Mysql) {
                            statement.addBatch(MysqlTab.MAIL_TAB.tab)
                            statement.addBatch(MysqlTab.MAIL_TAB_INDEX_1.tab)
                            statement.addBatch(MysqlTab.USER_TAB.tab)
                            statement.addBatch(MysqlTab.DRAFT_TAB.tab)
                            statement.addBatch(MysqlTab.DRAFT_TAB_INDEX_1.tab)
                        } else {
                           // statement.addBatch("PRAGMA foreign_keys = ON;")
                          //  statement.addBatch("PRAGMA encoding = 'UTF-8';")
                            statement.addBatch(SqliteTab.MAIL_TAB.tab)
                            statement.addBatch(SqliteTab.MAIL_TAB_INDEX_1.tab)
                            statement.addBatch(SqliteTab.MAIL_TAB_INDEX_2.tab)
                            statement.addBatch(SqliteTab.USER_TAB.tab)
                            statement.addBatch(SqliteTab.DRAFT_TAB.tab)
                            statement.addBatch(SqliteTab.DRAFT_TAB_INDEX_1.tab)
                        }
                        statement.executeBatch()
                    }
                }
            }
        }
    }

    fun close() {
        dataSub.onClose()
    }

    fun deleteMail(player: UUID, mail: IMail<*>) {
        getConnection {
            try {
                autoCommit = false
                this.prepareStatement(
                    "SELECT `sd`,`td`,`sender`,`target` FROM $mailTAB WHERE uuid=? LIMIT 1"
                ).action {
                    val mailID = mail.uuid.toString()
                    it.setString(1, mailID)
                    it.executeQuery().get { res ->
                        if (res.next()) {
                            val sd = res.getInt("sd")
                            val td = res.getInt("td")
                            if (sd == 1 && td == 1) {
                                this.prepareStatement(
                                    "DELETE FROM $mailTAB WHERE `uuid`=? LIMIT 1"
                                ).action { a2 ->
                                    a2.setString(1, mailID)
                                    a2.executeUpdate()
                                }
                            } else {
                                if (player.toString() == res.getString("sender")) {
                                    if (sd == 1) throw RuntimeException("重复的删除请求... ${mail.uuid}")
                                    this.prepareStatement(
                                        "UPDATE $mailTAB SET `sd`=? WHERE `uuid`=? LIMIT 1"
                                    ).action { a2 ->
                                        a2.setInt(1, 1)
                                        a2.setString(2, mailID)
                                        a2.executeUpdate()
                                    }
                                } else {
                                    if (td == 1) throw RuntimeException("重复的删除请求... ${mail.uuid}")
                                    this.prepareStatement(
                                        "UPDATE $mailTAB SET `td`=? WHERE `uuid`=? LIMIT 1"
                                    ).action { a2 ->
                                        a2.setInt(1, 1)
                                        a2.setString(2, mailID)
                                        a2.executeUpdate()
                                    }
                                }
                            }
                            commit()
                        }
                    }
                }
            } catch (e: Exception) {
                rollback()
                info("sql 事务已回滚，操作失败...")
                e.printStackTrace()
            } finally {
                autoCommit = true
            }
        }
    }

    fun insertMail(mail: IMail<*>) {
        getConnection {
            this.prepareStatement(
                "INSERT INTO $mailTAB(`uuid`,`sender`,`target`,`title`,`context`,`state`,`senderTimer`,`collectTimer`,`type`,`data`,`sd`,`td`)" +
                        " VALUES(?,?,?,?,?,?,?,?,?,?,?,?)"
            ).action {
                it.setString(1, mail.uuid.toString())
                it.setString(2, mail.sender.toString())
                it.setString(3, mail.target.toString())
                it.setString(4, mail.title)
                it.setString(5, mail.context)
                it.setString(6, mail.state.name)
                it.setLong(7, mail.senderTimer)
                it.setLong(8, mail.collectTimer)
                it.setString(9, mail.mailType)
                it.setBytes(10, IMailRegister.serializeMailData(mail))
                it.setInt(11, mail.senderDel)
                it.setInt(12, mail.targetDel)
                it.execute()
            }
        }
    }

    fun updateMailsState(mails: List<IMail<*>>) {
        if (mails.isEmpty()) return
        getConnection {
            this.prepareStatement(
                "UPDATE $mailTAB SET `state`=?, `collectTimer`=? WHERE `uuid`=? LIMIT 1"
            ).action {
                mails.forEach { mail ->
                    it.setString(1, mail.state.name)
                        it.setLong(2, mail.collectTimer)
                        it.setString(3, mail.uuid.toString())
                        it.addBatch()
                    }
                    it.executeBatch()
                }
        }
    }

    fun updateMailsDel(mails: List<IMail<*>>, isSenderDel: Boolean) {
        if (mails.isEmpty()) return
        getConnection {
            if (isSenderDel) {
                this.prepareStatement(
                    "UPDATE $mailTAB SET `sd`=? WHERE `uuid`=? LIMIT 1"
                ).action {
                    mails.forEach { mail ->
                        it.setInt(1, mail.senderDel)
                        it.setString(5, mail.uuid.toString())
                        it.addBatch()
                    }
                    it.executeBatch()
                }
            } else {
                this.prepareStatement(
                    "UPDATE $mailTAB SET `td`=? WHERE `uuid`=? LIMIT 1"
                ).action {
                    mails.forEach { mail ->
                        it.setInt(1, mail.targetDel)
                        it.setString(2, mail.uuid.toString())
                        it.addBatch()
                    }
                    it.executeBatch()
                }
            }
        }
    }


    /**
     * 此查询会返回收件箱、发件箱，两种状态邮件
     */
    fun selectMail(key: UUID, limit: Int = 100): Pair<MutableList<IMail<*>>, MutableList<IMail<*>>> {
        val s = mutableListOf<IMail<*>>()
        val r = mutableListOf<IMail<*>>()
        getConnection {
            this.prepareStatement(
                "SELECT `uuid`,`sender`,`target`,`title`,`context`,`state`,`senderTimer`,`collectTimer`,`type`,`data` " +
                        "FROM $mailTAB WHERE (sender = ? OR target = ?)" +
                        "    AND (" +
                        "        (sender = ? AND sd = 0)" +
                        "        OR" +
                        "        (target = ? AND td = 0)" +
                        "    ) LIMIT ?"
            ).action {
                val k = key.toString()
                it.setString(1, k)
                it.setString(2, k)
                it.setString(3, k)
                it.setString(4, k)
                it.setInt(5, limit)
                it.executeQuery().get { res ->
                    while (res.next()) {
                        IMailRegister.getRegisterMail(res.getString("type"))?.let { mail ->
                            val uuid = UUID.fromString(res.getString("uuid"))
                            val send = UUID.fromString(res.getString("sender"))
                            val target = UUID.fromString(res.getString("target"))
                            val data = IMailRegister.deserializeMailData(res.getBytes("data"), mail.getDataType())
                            val mailObj = mail.cloneMail(uuid, send, target, data)
                            mailObj.title = res.getString("title")
                            mailObj.context = res.getString("context")
                            mailObj.state = IMail.IMailState.valueOf(res.getString("state"))
                            mailObj.senderTimer = res.getLong("senderTimer")
                            mailObj.collectTimer = res.getLong("collectTimer")
                            if (key == send) {
                                s.add(mailObj)
                            } else if (target == key) {
                                r.add(mailObj)
                            } else error("所查询的数据无法匹配发送者和接收者，请检查错误...")
                        } ?: error("未知邮件种类 -> ${res.getString("type")}")
                    }
                }
            }
        }
        return s to r
    }

    fun insertDraft(edite: MailDraftBuilder) {
        getConnection {
            prepareStatement("INSERT INTO $draftTAB(`uuid`,`type`,`sender`,`title`,`context`,`data`) " +
                    "VALUES(?, ?, ?, ?, ?, ?)"
            ).action {
                it.setString(1, edite.uuid.toString())
                it.setString(2, edite.type)
                it.setString(3, edite.sender.toString())
                it.setString(4, edite.title)
                it.setString(5, edite.context.joinToString(";"))
                it.setBytes(6, MailDraftBuilder.serialize(edite.targets))
                it.execute()
            }
        }
    }


    fun deleteDrafts(edite: List<MailDraftBuilder>, callBack: (Int) -> Unit) {
        if (edite.isEmpty()) return
        getConnection {
            try {
                autoCommit = false
                this.prepareStatement("DELETE FROM $draftTAB WHERE `uuid`=? LIMIT 1"
                ).action {
                    edite.forEach { mail ->
                        it.setUUID(1, mail.uuid)
                        it.addBatch()
                    }
                    it.executeBatch()
                    commit()
                    callBack.invoke(1)
                }
            } catch (e: Exception) {
                rollback()
                warning("deleteDrafts() sql 事务已回滚，操作失败...")
                e.printStackTrace()
            } finally {
                // 返回删除失败
                callBack.invoke(0)
                autoCommit = true
            }
        }
    }

    fun updateDrafts(edite: List<MailDraftBuilder>) {
        if (edite.isEmpty()) return
        getConnection {
            try {
                autoCommit = false
                this.prepareStatement(
                    """
                    INSERT INTO $draftTAB (`uuid`, `sender`, `type`, `title`, `context`, `data`)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE 
                    `sender` = VALUES(sender), 
                    `type` = VALUES(type), 
                    `title` = VALUES(title), 
                    `context` = VALUES(context), 
                    `data` = VALUES(data);
            """).action {
                    edite.forEach { draft ->
                        it.setUUID(1, draft.uuid)
                        it.setUUID(2, draft.sender)
                        it.setString(3, draft.type)
                        it.setString(4, draft.title)
                        it.setStringList(5, draft.context)
                        it.setBytes(6, MailDraftBuilder.serialize(draft.targets))
                        it.addBatch()
                    }
                    it.executeBatch()
                    commit()
                }
            } catch (e: Exception) {
                rollback()
                warning("updateDrafts() sql 事务已回滚，操作失败...")
                e.printStackTrace()
            } finally {
              autoCommit = true
            }
        }
    }

    fun selectDrafts(player: UUID, callBack: (MutableList<MailDraftBuilder>) -> Unit) {
        getConnection {
            this.prepareStatement(
                "SELECT `uuid`,`type`,`title`,`context`,`data` FROM $draftTAB WHERE sender=? LIMIT 20"
            ).action {
                it.setUUID(1, player)
                it.executeQuery().get { res ->
                    val list = mutableListOf<MailDraftBuilder>()
                    while (res.next()) {
                        val type = res.getString("type")
                        IMailRegister.getRegisterMail(type)?.let { im ->
                            val uuid = res.getUUID("uuid")
                            val title = res.getString("title")
                            val context = res.getStringList("context").toMutableList()
                            val data = MailDraftBuilder.deserialize(res.getBytes("data"), im.getDataType())
                            list.add(MailDraftBuilder(player, type, uuid, title, context, data))
                        } ?: warning("在获取草稿邮件时发生以外，邮件种类缺少 -> $type")
                    }
                    callBack.invoke(list)
                }
            }
        }
    }

    private fun insertPlayerData(data: PlayerData) {
        if (dataSub.isActive) {
            getConnection {
                prepareStatement(
                    "INSERT INTO $userTAB(`uuid`,`user`,`mail`,`data`) VALUES(?,?,?,?)"
                ).action {
                    it.setUUID(1, data.uuid)
                    it.setString(2, data.user)
                    it.setString(3, data.mail)
                    it.setString(4, data.getExtendData())
                    it.executeUpdate()
                }
            }
        } else {
            error("数据库连接异常")
        }
    }

    fun updatePlayerData(data: PlayerData) {
        if (dataSub.isActive) {
            getConnection {
                prepareStatement(
                    "UPDATE $userTAB SET `user`=?, `mail`=?, `data`=? WHERE `uuid`=? LIMIT 1"
                ).action {
                    it.setString(1, data.user)
                    it.setString(2, data.mail)
                    it.setString(3, data.getExtendData())
                    it.setUUID(4, data.uuid)
                    it.executeUpdate()
                }
            }
        } else {
            error("数据库连接异常")
        }
    }

    fun selectPlayerData(player: ProxyPlayer, callBack: (PlayerData) -> Unit) {
        if (dataSub.isActive) {
            getConnection {
                this.prepareStatement(
                    "SELECT `mail`,`data` FROM $userTAB WHERE uuid=? LIMIT 1"
                ).action {
                    it.setString(1, player.uniqueId.toString())
                    val res = it.executeQuery()
                    val playerData = PlayerData(player.uniqueId, player.displayName!!)
                    if (res.next()) {
                        playerData.mail = res.getString("mail")
                        playerData.setExtendData(res.getString("data"))
                    } else {
                        insertPlayerData(playerData)
                    }
                    callBack.invoke(playerData)
                    res.close()
                }
            }
        } else {
            error("数据库连接异常")
        }
    }


    private fun getConnection(func: Connection.() -> Unit) {
        if (dataSub.isActive) {
            dataSub.getConnection().use(func)
        } else {
            error("数据库连接异常")
        }
    }

    companion object {
        const val userTAB: String = "neon_user_tab"
        const val mailTAB: String = "neon_mail_tab"
        const val draftTAB: String = "neon_draft_tab"

    }
}