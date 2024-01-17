package me.neon.mail.service

import me.neon.mail.NeonMailLoader
import me.neon.mail.api.mail.IMail
import me.neon.mail.api.mail.IMailRegister
import me.neon.mail.api.mail.IMailState
import me.neon.mail.common.MailDraftBuilder
import me.neon.mail.common.PlayerData
import me.neon.mail.service.enums.TABMysql
import me.neon.mail.service.enums.TABSqlite
import me.neon.mail.service.enums.TABStatement
import me.neon.mail.service.sql.*
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.warning
import java.sql.Connection
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * NeonMail-Premium
 * me.neon.mail
 *
 * @author 老廖
 * @since 2024/1/2 16:10
 */
class SQLImpl {

    private val dataSub by lazy {
        if (NeonMailLoader.sqlConfig.use_type.equals("mysql", ignoreCase = true)) {
            Mysql(NeonMailLoader.sqlConfig)
        } else Sqlite(NeonMailLoader.sqlConfig)
    }

    fun close() {
        dataSub.onClose()
    }

    fun start() {
        if (dataSub.isActive) return
        dataSub.onStart()
        if (dataSub.isActive) {
            dataSub.createTab {
                getConnection().use {
                    createStatement().action { statement ->
                        if (dataSub is Mysql) {
                            statement.addBatch(TABMysql.MAIL_TAB.tab)
                            statement.addBatch(TABMysql.USER_TAB.tab)
                            statement.addBatch(TABMysql.DRAFT_TAB.tab)
                        } else {
                            statement.addBatch(TABSqlite.MAIL_TAB.tab)
                            statement.addBatch(TABSqlite.USER_TAB.tab)
                            statement.addBatch(TABSqlite.DRAFT_TAB.tab)
                        }
                        statement.executeBatch()
                    }
                    prepareStatement(
                        "show index from $mailTAB"
                    ).action {
                        it.executeQuery().get { res ->
                            var create = true
                            while (res.next()) {
                                if (res.getString("Key_name") == "idx_sender_target") {
                                    NeonMailLoader.debug("索引 idx_sender_target 已存在，不再创建...")
                                    create = false
                                    break
                                }
                            }
                            if (create) {
                                NeonMailLoader.debug("索引 idx_sender_target 不已存在，正在创建...")
                                createStatement().execute(TABMysql.MAIL_TAB_INDEX_1.tab)
                            }
                        }
                    }
                    prepareStatement(
                        "show index from $draftTAB"
                    ).action {
                        it.executeQuery().get { res ->
                            var create = true
                            while (res.next()) {
                                if (res.getString("Key_name") == "idx_sender") {
                                    NeonMailLoader.debug("索引 idx_sender 已存在，不再创建...")
                                    create = false
                                    break
                                }
                            }
                            if (create) {
                                NeonMailLoader.debug("索引 idx_sender 不已存在，正在创建...")
                                createStatement().execute(TABMysql.DRAFT_TAB_INDEX_1.tab)
                            }
                        }
                    }
                }
            }
        }
    }


    fun deleteMails(mails: List<IMail<*>>, isSenderDel: Boolean) {
        if (mails.isEmpty()) {
            error("mails is empty")
        }
        getConnection {
            try {
                autoCommit = false
                this.prepareStatement(
                    if (isSenderDel) TABStatement.MAIL_SD_DELETE.statement else TABStatement.MAIL_TD_DELETE.statement
                ).action {
                    for (mail in mails) {
                        it.setInt(1, 1)
                        it.setUUID(2,  mail.uuid)
                        it.addBatch()
                    }
                    it.executeBatch()
                }
                this.prepareStatement(TABStatement.MAIL_DELETE_MARKED.statement).executeUpdate()
                commit()
            } catch (e: Exception) {
                rollback()
                NeonMailLoader.debug("SQL事务已回滚，操作失败...")
                e.printStackTrace()
            } finally {
                autoCommit = true
            }
        }
    }

    /**
     * **单个邮件的删除操作**
     *
     * @param player 玩家UUID
     * @param mail 邮件实体
     */
    @Deprecated("不确定是否使用")
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
                NeonMailLoader.debug("sql 事务已回滚，操作失败...")
                e.printStackTrace()
            } finally {
                autoCommit = true
            }
        }
    }

    fun insertMail(mail: IMail<*>) {
        getConnection {
            this.prepareStatement(
                TABStatement.MAIL_INSERT.statement
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

    fun updateMailsState(mails: List<IMail<*>>): Boolean {
        if (mails.isEmpty()) return false
        val back = AtomicBoolean(false)
        getConnection {
            autoCommit = false
            try {
                this.prepareStatement(
                    TABStatement.MAIL_UPDATE_STATE.statement
                ).action {
                    mails.forEach { mail ->
                        it.setString(1, mail.state.name)
                        it.setLong(2, mail.collectTimer)
                        it.setString(3, mail.uuid.toString())
                        it.addBatch()
                    }
                    it.executeBatch()
                    back.set(true)
                    commit()
                }
            } catch (e: Exception) {
                rollback()
                e.printStackTrace()
                back.set(false)
            } finally {
                autoCommit = true
            }
        }
        return back.get()
    }

    @Deprecated("不确定是否使用")
    fun updateMailsDel(mails: List<IMail<*>>, isSenderDel: Boolean) {
        if (mails.isEmpty()) return
        getConnection {
            if (isSenderDel) {
                this.prepareStatement(
                    "UPDATE $mailTAB SET `sd`=? WHERE `uuid`=? LIMIT 1"
                ).action {
                    mails.forEach { mail ->
                        it.setInt(1, 1)
                        it.setString(2, mail.uuid.toString())
                        it.addBatch()
                    }
                    it.executeBatch()
                }
            } else {
                this.prepareStatement(
                    "UPDATE $mailTAB SET `td`=? WHERE `uuid`=? LIMIT 1"
                ).action {
                    mails.forEach { mail ->
                        it.setInt(1, 1)
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
    fun selectMails(player: UUID, limit: Int = 100): Pair<MutableList<IMail<*>>, MutableList<IMail<*>>> {
        val s = mutableListOf<IMail<*>>()
        val t = mutableListOf<IMail<*>>()
        getConnection {
            this.prepareStatement(
                "SELECT `uuid`,`sender`,`target`,`title`,`context`,`state`,`senderTimer`,`collectTimer`,`type`,`td`,`sd`,`data` " +
                        "FROM $mailTAB WHERE (sender=? AND sd=0) OR (target=? AND td=0) LIMIT ?"
            ).action {
                val k = player.toString()
                it.setString(1, k)
                it.setString(2, k)
                it.setInt(3, limit)
                fun add(sender: UUID, target: UUID, sd: Int, td: Int, mailObj: IMail<*>) {
                    if (sender == target) {
                        if (sd == 0) s.add(mailObj)
                        if (td == 0) t.add(mailObj)
                    } else if (player == sender) {
                        s.add(mailObj)
                    } else if (target == player) {
                        t.add(mailObj)
                    } else error("所查询的数据无法匹配发送者和接收者，请检查错误...")
                }
                it.executeQuery().get { res ->
                    while (res.next()) {
                        IMailRegister.getRegisterMail(res.getString("type"))?.let { mail ->
                            val uuid = UUID.fromString(res.getString("uuid"))
                            val sender = UUID.fromString(res.getString("sender"))
                            val target = UUID.fromString(res.getString("target"))
                            val data = IMailRegister.deserializeMailData(res.getBytes("data"), mail.getDataType())
                            val mailObj = mail.cloneMail(uuid, sender, target, data)
                            mailObj.title = res.getString("title")
                            mailObj.context = res.getString("context")
                            mailObj.state = IMailState.valueOf(res.getString("state"))
                            mailObj.senderTimer = res.getLong("senderTimer")
                            mailObj.collectTimer = res.getLong("collectTimer")
                            // 发送者和接受者都是一个人
                            // 特殊处理
                            add(sender, target, res.getInt("sd"), res.getInt("td"), mailObj)
                        } ?: error("未知邮件种类 -> ${res.getString("type")}")
                    }
                }
            }
        }
        return s to t
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
                    // 提交后再发送，
                    // 因为如果发送邮件中途出现异常，可能导致回滚，而没有移除对应的编辑箱
                    // 导致可能有部分邮件已送达，但是编辑箱依旧存在，从而刷取附件
                    // 虽然先提交后发送，如果出现异常会吞掉发送者附件
                    // 但好过于被玩家利用可能的漏洞刷去附件
                    // 这个问题被标记 TODO 未来寻找方法解决
                    callBack.invoke(1)
                }
            } catch (e: Exception) {
                rollback()
                warning("deleteDrafts() sql 事务已回滚，操作失败...")
                e.printStackTrace()
                // 返回删除失败
                callBack.invoke(0)
            } finally {
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