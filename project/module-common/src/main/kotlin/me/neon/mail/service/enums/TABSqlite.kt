package me.neon.mail.service.enums

import me.neon.mail.service.SQLImpl

/**
 * NeonMail-Premium
 * me.neon.mail.service
 *
 * @author 老廖
 * @since 2024/1/3 10:41
 */
enum class TABSqlite(val tab: String) {
    // 邮件表
    MAIL_TAB(
        "CREATE TABLE IF NOT EXISTS ${SQLImpl.mailTAB} (" +
                " `uuid`            CHAR(36)        UNIQUE PRIMARY KEY," +
                " `sender`          CHAR(36)        NOT NULL," +
                " `target`          CHAR(36)        NOT NULL," +
                " `title`           VARCHAR(255)    NOT NULL," +
                " `context`         TEXT            NOT NULL," +
                " `state`           CHAR(16)        NOT NULL," +
                " `senderTimer`     INTEGER         NOT NULL," +
                " `collectTimer`    INTEGER         NOT NULL," +
                " `type`            CHAR(16)        NOT NULL," +
                " `sd`              int             DEFAULT `0`," +
                " `td`              int             DEFAULT `0`," +
                " `data`            TIMESTAMP       NOT NULL," +
                " `time` DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");"
    ),
    // 用户
    USER_TAB(
        "CREATE TABLE IF NOT EXISTS ${SQLImpl.userTAB} (" +
                " `uuid`    TEXT            NOT NULL UNIQUE PRIMARY KEY," +
                " `user`    VARCHAR(16)     NOT NULL," +
                " `mail`    VARCHAR(36)     NOT NULL," +
                " `data`    TEXT            NOT NULL," +
                " `time` DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");"
    ),
    DRAFT_TAB(
        "CREATE TABLE IF NOT EXISTS ${SQLImpl.draftTAB} (" +
                " `uuid`      TEXT             NOT NULL UNIQUE PRIMARY KEY," +
                " `type`      CHAR(16)         NOT NULL," +
                " `sender`    TEXT             NOT NULL," +
                " `title`     VARCHAR(255)     NOT NULL," +
                " `context`   TEXT             NOT NULL," +
                " `global`    Boolean          NOT NULL," +
                " `data`      MEDIUMTEXT       NOT NULL," +
                " `time` DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");"
    ),
}