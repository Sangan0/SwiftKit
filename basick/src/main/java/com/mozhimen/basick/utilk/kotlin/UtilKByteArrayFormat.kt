package com.mozhimen.basick.utilk.kotlin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mozhimen.basick.utilk.android.util.et
import com.mozhimen.basick.utilk.bases.IUtilK
import com.mozhimen.basick.utilk.java.io.UtilKFile
import com.mozhimen.basick.utilk.java.io.file2fileOutputStream
import com.mozhimen.basick.utilk.java.io.writeBytes2fileOutputStream
import com.mozhimen.basick.utilk.java.security.UtilKMd5
import java.io.ByteArrayInputStream
import java.io.File
import java.io.ObjectInputStream
import java.nio.charset.Charset
import java.security.NoSuchAlgorithmException

/**
 * @ClassName UtilKByteArrayFormat
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/8/1 16:13
 * @Version 1.0
 */

fun ByteArray.bytes2obj(): Any? =
    UtilKByteArrayFormat.bytes2obj(this)

fun ByteArray.bytes2byteArrayInputStream(): ByteArrayInputStream =
    UtilKByteArrayFormat.bytes2byteArrayInputStream(this)

fun ByteArray.bytes2bitmapAny(): Bitmap =
    UtilKByteArrayFormat.bytes2bitmapAny(this)

fun ByteArray.bytes2file(destFilePathWithName: String, isAppend: Boolean = false): File =
    UtilKByteArrayFormat.bytes2file(this, destFilePathWithName, isAppend)

fun ByteArray.bytes2file(destFile: File, isAppend: Boolean = false): File =
    UtilKByteArrayFormat.bytes2file(this, destFile, isAppend)

//////////////////////////////////////////////////////////////////////////////////

fun ByteArray.bytes2strMd5Hex(): String =
    UtilKByteArrayFormat.bytes2strMd5Hex(this)

fun ByteArray.bytes2strHex(size: Int): String =
    UtilKByteArrayFormat.bytes2strHex(this, size)

fun ByteArray.bytes2strHex(): String =
    UtilKByteArrayFormat.bytes2strHex(this)

fun ByteArray.bytes2str(charset: Charset = Charsets.UTF_8): String =
    UtilKByteArrayFormat.bytes2str(this, charset)

fun ByteArray.bytes2str(offset: Int, length: Int): String =
    UtilKByteArrayFormat.bytes2str(this, offset, length)

object UtilKByteArrayFormat : IUtilK {

    @JvmStatic
    fun bytes2obj(bytes: ByteArray): Any? {
        var byteArrayInputStream: ByteArrayInputStream? = null
        var objectInputStream: ObjectInputStream? = null
        try {
            byteArrayInputStream = ByteArrayInputStream(bytes)
            objectInputStream = ObjectInputStream(byteArrayInputStream)
            return objectInputStream.readObject()
        } catch (e: Exception) {
            e.printStackTrace()
            e.message?.et(TAG)
        } finally {
            byteArrayInputStream?.close()
            objectInputStream?.close()
        }
        return null
    }

    @JvmStatic
    fun bytes2byteArrayInputStream(bytes: ByteArray): ByteArrayInputStream =
        ByteArrayInputStream(bytes)

    @JvmStatic
    fun bytes2bitmapAny(bytes: ByteArray): Bitmap =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    @JvmStatic
    fun bytes2file(bytes: ByteArray, destFilePathWithName: String, isAppend: Boolean = false): File =
        bytes2file(bytes, UtilKFile.createFile(destFilePathWithName), isAppend)

    @JvmStatic
    fun bytes2file(bytes: ByteArray, destFile: File, isAppend: Boolean = false): File {
        UtilKFile.createFile(destFile)
        destFile.file2fileOutputStream(isAppend).writeBytes2fileOutputStream(bytes)
        return destFile
    }

    //////////////////////////////////////////////////////////////////////////////////

    @JvmStatic
    @Throws(NoSuchAlgorithmException::class)
    fun bytes2strMd5Hex(bytes: ByteArray): String =
        UtilKMd5.digest(bytes).bytes2strHex()

    /**
     * byte[]数组转换为16进制的字符串
     *
     * @param bytes 要转换的字节数组
     * @return 转换后的结果
     */
    @JvmStatic
    fun bytes2strHex(bytes: ByteArray, size: Int): String {
        val stringBuilder = StringBuilder()
        for (i in 0 until size) {
            val hex = Integer.toHexString(0xFF and bytes[i].toInt())
            if (hex.length == 1)
                stringBuilder.append('0')
            stringBuilder.append(hex)
        }
        return stringBuilder.toString()
    }

    /**
     * 转换字节数组为16进制字串
     * @param bytes 字节数组
     * @return 16进制字串
     */
    @JvmStatic
    fun bytes2strHex(bytes: ByteArray): String {
        val stringBuilder = StringBuilder()
        for (byte in bytes) {
            stringBuilder.append(byte.byte2strHex())
            // 也可以使用下面的方式。 X 表示大小字母，x 表示小写字母，对应的是 HEX_DIGITS 中字母
            // buf.append(String.format("%02X", value));
        }
        return stringBuilder.toString()
    }

    @JvmStatic
    fun bytes2str(bytes: ByteArray, charset: Charset = Charsets.UTF_8): String =
        String(bytes, charset)

    @JvmStatic
    fun bytes2str(bytes: ByteArray, offset: Int, length: Int): String =
        String(bytes, offset, length)
}