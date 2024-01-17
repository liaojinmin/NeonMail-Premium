package me.neon.mail.service.enums

import me.neon.mail.service.SQLImpl
import me.neon.mail.service.SQLImpl.Companion.draftTAB

import me.neon.mail.service.SQLImpl.Companion.userTAB

/**
 * NeonMail-Premium
 * me.neon.mail.service
 *
 * @author 老廖
 * @since 2024/1/3 9:56
 */
enum class TABMysql(val tab: String) {

    // 邮件表
    MAIL_TAB(
        "CREATE TABLE IF NOT EXISTS ${SQLImpl.mailTAB} (" +
                " `uuid`          CHAR(36)       UNIQUE," +
                " `sender`        CHAR(36)       NOT NULL," +
                " `target`        CHAR(36)       NOT NULL," +
                " `title`         VARCHAR(255)   NOT NULL," +
                " `context`       TEXT           NOT NULL," +
                " `state`         CHAR(16)       NOT NULL," +
                " `senderTimer`   bigint         NOT NULL," +
                " `collectTimer`  bigint         NOT NULL," +
                " `type`          CHAR(16)       NOT NULL," +
                " `sd`            tinyint     DEFAULT 0," +
                " `td`            tinyint     DEFAULT 0," +
                " `data`          MEDIUMTEXT     NOT NULL," +
                " `time`          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`uuid`)" +
                ");"
    ),
    MAIL_TAB_INDEX_1(
        "CREATE INDEX idx_sender_target ON ${SQLImpl.mailTAB}(sender, target);"
    ),
    // 用户
    USER_TAB(
        "CREATE TABLE IF NOT EXISTS $userTAB (" +
                // 主键 UUID 唯一 邮件 ID
                " `uuid` CHAR(36)     NOT NULL   UNIQUE," +
                // 用户名称
                " `user` varchar(16)  NOT NULL," +
                // 绑定的邮箱
                " `mail` varchar(36)  NOT NULL," +
                // 其它数据
                " `data`  text         NOT NULL," +
                " `time` TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`uuid`)" +
                ");"
    ),
    DRAFT_TAB(
        "CREATE TABLE IF NOT EXISTS $draftTAB (" +
                " `uuid`          CHAR(36)       NOT NULL   UNIQUE," +
                " `type`          CHAR(16)       NOT NULL," +
                " `sender`        CHAR(36)       NOT NULL," +
                " `title`         VARCHAR(255)   NOT NULL," +
                " `context`       TEXT           NOT NULL," +
                " `data`          MEDIUMTEXT     NOT NULL," +
                " `time`          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`uuid`)" +
                ");"
    ),
    DRAFT_TAB_INDEX_1(
        "CREATE INDEX idx_sender ON $draftTAB(sender);"
    ),
}