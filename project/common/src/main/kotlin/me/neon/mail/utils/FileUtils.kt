package me.neon.mail.utils

import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest


fun File.forFile(end: String): List<File> {
    return mutableListOf<File>().run {
        if (isDirectory) {
            listFiles()?.forEach {
                addAll(it.forFile(end))
            }
        } else if (exists() && absolutePath.endsWith(end)) {
            add(this@forFile)
        }
        this
    }
}


/**
 * 声明一个文件
 *
 * @param file 文件
 * @param create 若文件不存在是否新建（默认为是）
 * @param folder 该文件是否为文件夹（默认为否）
 * @return 该文件自身
 */
fun newFile(file: File, create: Boolean = true, folder: Boolean = false): File {
    if (!file.parentFile.exists()) {
        file.parentFile.mkdirs()
    }
    if (!file.exists() && create) {
        if (folder) {
            file.mkdirs()
        } else {
            file.createNewFile()
        }
    }
    return file
}



/**
 * 取文件的数字签名
 *
 * @param algorithm 算法类型（可使用：md5, sha-1, sha-256 等）
 * @return 数字签名
 */
fun File.digest(algorithm: String): String {
    return FileInputStream(this).use {
        val digest = MessageDigest.getInstance(algorithm)
        val buffer = ByteArray(1024)
        var length: Int
        while (it.read(buffer, 0, 1024).also { i -> length = i } != -1) {
            digest.update(buffer, 0, length)
        }
        BigInteger(1, digest.digest()).toString(16)
    }
}