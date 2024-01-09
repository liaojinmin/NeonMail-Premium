package me.neon.mail.service

/**
 * NeonMail-Premium
 * me.neon.mail.service
 *
 * @author 老廖
 * @since 2024/1/3 10:41
 */
enum class SqliteTab(val tab: String) {
    // 邮件表
    MAIL_TAB(
        "CREATE TABLE IF NOT EXISTS ${SQLImpl.mailTAB} (" +
                " uuid CHAR(36) UNIQUE," +
                " sender TEXT NOT NULL," +
                " target TEXT NOT NULL," +
                " title VARCHAR(255) NOT NULL," +
                " context TEXT NOT NULL," +
                " state CHAR(16) NOT NULL," +
                " senderTimer INTEGER NOT NULL," +
                " collectTimer INTEGER NOT NULL," +
                " type CHAR(16) NOT NULL," +
                " `sd`            int(1)     DEFAULT `0`," +
                " `td`            int(1)     DEFAULT `0`," +
                " data TEXT NOT NULL," +
                " time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ");"
    ),
    MAIL_TAB_INDEX_1(
        "CREATE INDEX IF NOT EXISTS idx_sender ON ${SQLImpl.mailTAB}(sender);"
    ),
    MAIL_TAB_INDEX_2(
        "CREATE INDEX IF NOT EXISTS idx_target ON ${SQLImpl.mailTAB}(target);"
    ),
    // 用户
    USER_TAB(
        "CREATE TABLE IF NOT EXISTS ${SQLImpl.userTAB} (" +
                " uuid TEXT NOT NULL UNIQUE," +
                " user VARCHAR(16) NOT NULL," +
                " mail VARCHAR(36) NOT NULL," +
                " data TEXT NOT NULL," +
                " PRIMARY KEY (uuid)" +
                ");"
    ),
    DRAFT_TAB(
        "CREATE TABLE IF NOT EXISTS ${SQLImpl.draftTAB} (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " sender TEXT NOT NULL," +
                " title VARCHAR(255) NOT NULL," +
                " context TEXT NOT NULL," +
                " type CHAR(16) NOT NULL," +
                " data MEDIUMTEXT NOT NULL," +
                " time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ");"
    ),
    DRAFT_TAB_INDEX_1(
        "CREATE INDEX IF NOT EXISTS idx_sender ON ${SQLImpl.draftTAB} (sender);"
    ),
}