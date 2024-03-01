package me.neon.mail.service.enums

import me.neon.mail.service.SQLImpl


/**
 *
 * **已确认使用的语句会以枚举方式存留.**
 *
 * NeonMail-Premium
 * me.neon.mail.service.enums
 *
 * @author 老廖
 * @since 2024/1/17 3:50
 */
enum class TABStatement(val statement: String) {
    // 邮件相关
    /**
     * 更新邮件领取状态
     */
    MAIL_UPDATE_STATE("UPDATE ${SQLImpl.mailTAB} SET `state`=?, `collectTimer`=? WHERE `uuid`=?"),
    /**
     * 删除发送者与接收者均已标记删除的邮件
     */
    MAIL_DELETE_MARKED("DELETE FROM ${SQLImpl.mailTAB} WHERE `sd`=1 AND`td`=1"),
    /**
     * 新增邮件
     */
    MAIL_INSERT("INSERT INTO ${SQLImpl.mailTAB}(`uuid`,`sender`,`target`,`title`,`context`,`state`,`senderTimer`,`collectTimer`,`type`,`data`,`sd`,`td`) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)"),
    /**
     * 标记发送者已删除邮件
     */
    MAIL_SD_DELETE("UPDATE ${SQLImpl.mailTAB} SET `sd`=? WHERE `uuid`=?"),
    /**
     * 标记接收者已删除邮件
     */
    MAIL_TD_DELETE("UPDATE ${SQLImpl.mailTAB} SET `td`=? WHERE `uuid`=?"),
    /**
     * 查询玩家邮件列表
     */
    SELECT_MAIL_LIST("SELECT `uuid`,`sender`,`target`,`title`,`context`,`state`,`senderTimer`,`collectTimer`,`type`,`td`,`sd`,`data` " +
            "FROM ${SQLImpl.mailTAB} WHERE (sender=? AND sd=0) OR (target=? AND td=0) LIMIT ?"),
    /**
     * 查询玩家单个邮箱
     */
    SELECT_MAIL("SELECT `uuid`,`sender`,`target`,`title`,`context`,`state`,`senderTimer`,`collectTimer`,`type`,`td`,`sd`,`data` " +
            "FROM ${SQLImpl.mailTAB} WHERE `uuid`=? LIMIT 1"),

    // 草稿箱相关
    DELETE_DRAFTS("DELETE FROM ${SQLImpl.draftTAB} WHERE `uuid`=?"),
    /**
     * 查询玩家 20 个草稿箱内容
     */
    SELECT_DRAFTS("SELECT `uuid`,`type`,`title`,`context`,`global`,`data` FROM ${SQLImpl.draftTAB} WHERE sender=? LIMIT 20"),
    UPDATE_DRAFTS_MYSQL("""
                    INSERT INTO ${SQLImpl.draftTAB} (`uuid`, `sender`, `type`, `title`, `context`,`global`, `data`)
                    VALUES (?, ?, ?, ?, ?, ?,?)
                    ON DUPLICATE KEY UPDATE 
                    `sender` = VALUES(`sender`), 
                    `type` = VALUES(`type`), 
                    `title` = VALUES(`title`), 
                    `context` = VALUES(`context`),
                    `global` = VALUES(`global`),
                    `data` = VALUES(`data`);
            """),
    UPDATE_DRAFTS_SQLITE("INSERT OR REPLACE INTO ${SQLImpl.draftTAB} (uuid, sender, type, title, context, global, data) VALUES (?, ?, ?, ?, ?, ?, ?)"),


    // 玩家数据
    SELECT_PLAYER_DATA("SELECT `mail`,`data` FROM ${SQLImpl.userTAB} WHERE uuid=? LIMIT 1"),
    UPDATE_PLAYER_DATA("UPDATE ${SQLImpl.userTAB} SET `user`=?, `mail`=?, `data`=? WHERE `uuid`=?"),
    INSERT_PLAYER_DATA("INSERT INTO ${SQLImpl.userTAB}(`uuid`,`user`,`mail`,`data`) VALUES(?,?,?,?)")
}