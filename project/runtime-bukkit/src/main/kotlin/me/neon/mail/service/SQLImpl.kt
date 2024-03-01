package me.neon.mail.service

import me.neon.mail.NeonMailLoader
import me.neon.mail.api.PlayerData
import me.neon.mail.api.mail.IDraftBuilder
import me.neon.mail.api.mail.IMail
import me.neon.mail.api.mail.IMailRegister
import me.neon.mail.api.mail.IMailState
import me.neon.mail.common.DraftBuilderImpl
import me.neon.mail.common.PlayerDataImpl
import me.neon.mail.service.enums.TABMysql
import me.neon.mail.service.enums.TABSqlite
import me.neon.mail.service.enums.TABStatement
import me.neon.mail.service.sql.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.warning
import java.sql.Connection
import java.sql.ResultSet
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
                    if (dataSub is Mysql) {
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
     * 批量插入待实现
     */
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
                it.setString(9, mail.data.sourceType)
                it.setBytes(10, IMailRegister.serializeMailData(mail))
                // 如果发送者是系统，默认 1
                it.setInt(11, if (mail.sender == IMailRegister.console) 1 else 0)
                it.setInt(12, 0)
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


    fun selectMail(uuid: UUID): IMail<*>? {
        var mail: IMail<*>? = null
        getConnection {
            this.prepareStatement(
                    TABStatement.SELECT_MAIL.statement
            ).action {
                it.setUUID(1, uuid)
                it.executeQuery().get { res ->
                    if (res.next()) {
                        if (res.getInt("sd") < 1 && res.getInt("td") < 1) {
                            mail = res.getMailData()
                        } else {
                            warning("异常的邮件查询，这个邮件应当被标记删除 $uuid")
                        }
                    }
                }
            }
        }
        return mail
    }

    /**
     * 此查询会返回收件箱、发件箱，两种状态邮件
     */
    fun selectMails(player: UUID, limit: Int = 100): Pair<MutableList<IMail<*>>, MutableList<IMail<*>>> {
        val s = mutableListOf<IMail<*>>()
        val t = mutableListOf<IMail<*>>()
        getConnection {
            this.prepareStatement(
                TABStatement.SELECT_MAIL_LIST.statement
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
                        val mailObj = res.getMailData()
                        add(mailObj.sender, mailObj.target, res.getInt("sd"), res.getInt("td"), mailObj)
                    }
                }
            }
        }
        return s to t
    }


    fun deleteDrafts(edite: List<IDraftBuilder>, callBack: (Int) -> Unit) {
        if (edite.isEmpty()) return
        getConnection {
            try {
                autoCommit = false
                this.prepareStatement(
                    TABStatement.DELETE_DRAFTS.statement
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


    fun updateDrafts(edite: List<IDraftBuilder>) {
        if (edite.isEmpty()) return
        getConnection {
            try {
                autoCommit = false
                this.prepareStatement(
                    if (dataSub is Mysql)
                        TABStatement.UPDATE_DRAFTS_MYSQL.statement
                    else
                        TABStatement.UPDATE_DRAFTS_SQLITE.statement).action {
                    edite.forEach { draft ->
                        it.setUUID(1, draft.uuid)
                        it.setUUID(2, draft.sender)
                        it.setString(3, draft.type)
                        it.setString(4, draft.title)
                        it.setStringList(5, draft.context)
                        it.setBoolean(6, draft.checkGlobalModel())
                        it.setBytes(7, DraftBuilderImpl.serialize(draft.getTargets()))
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

    fun selectDrafts(player: UUID, callBack: (MutableList<IDraftBuilder>) -> Unit) {
        getConnection {
            this.prepareStatement(
                TABStatement.SELECT_DRAFTS.statement
            ).action {
                it.setUUID(1, player)
                it.executeQuery().get { res ->
                    val list = mutableListOf<IDraftBuilder>()
                    while (res.next()) {
                        val type = res.getString("type")
                        IMailRegister.getRegisterMail(type)?.let { im ->
                            val uuid = res.getUUID("uuid")
                            val title = res.getString("title")
                            val context = res.getStringList("context").toMutableList()
                            val global = res.getBoolean("global")
                            val data = DraftBuilderImpl.deserialize(res.getBytes("data"), im.getDataClassType())
                            list.add(DraftBuilderImpl(player, type, uuid, title, context, global, data))
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
                    TABStatement.INSERT_PLAYER_DATA.statement
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
                    TABStatement.UPDATE_PLAYER_DATA.statement
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

    fun selectPlayerData(player: UUID, name: String? = ""): PlayerData {
        val data = PlayerDataImpl(player, (name ?: Bukkit.getOfflinePlayer(player).name) ?: "")
        if (dataSub.isActive) {
            getConnection {
                this.prepareStatement(
                    TABStatement.SELECT_PLAYER_DATA.statement
                ).action {
                    it.setString(1, player.toString())
                    val res = it.executeQuery()
                    if (res.next()) {
                        data.mail = res.getString("mail")
                        data.setExtendData(res.getString("data"))
                    } else {
                        insertPlayerData(data)
                    }
                    data.isLoader = true
                    res.close()
                }
            }
        } else {
            error("数据库连接异常")
        }
        return data
    }

    fun selectPlayerData(player: Player, callBack: (PlayerData) -> Unit) {
        return callBack.invoke(selectPlayerData(player.uniqueId, player.name))
    }

    private fun ResultSet.getMailData(): IMail<*> {
        return IMailRegister.getRegisterMail(this.getString("type"))?.let { mail ->
            val uuid = UUID.fromString(this.getString("uuid"))
            val sender = UUID.fromString(this.getString("sender"))
            val target = UUID.fromString(this.getString("target"))
            val data = IMailRegister.deserializeMailData(this.getBytes("data"), mail.getDataClassType())
            val mailObj = mail.cloneMail(uuid, sender, target, data)
            mailObj.title = this.getString("title")
            mailObj.context = this.getString("context")
            mailObj.state = IMailState.valueOf(this.getString("state"))
            mailObj.senderTimer = this.getLong("senderTimer")
            mailObj.collectTimer = this.getLong("collectTimer")
            mailObj
        } ?: error("未知邮件种类 -> ${this.getString("type")}")
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