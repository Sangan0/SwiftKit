package com.mozhimen.basick.utilk.kotlin

import com.mozhimen.basick.elemk.commons.I_Listener
import com.mozhimen.basick.elemk.cons.CMsg
import com.mozhimen.basick.utilk.android.os.UtilKBuildVersion
import com.mozhimen.basick.utilk.bases.BaseUtilK
import java.text.DecimalFormat
import java.util.*
import java.util.stream.Collectors


/**
 * @ClassName UtilKString
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/4/29 21:43
 * @Version 1.0
 */
fun String.getSplitLast(splitStr: String): String =
    UtilKString.getSplitLast(this, splitStr)

fun String.getSplitFirst(splitStr: String): String =
    UtilKString.getSplitFirst(this, splitStr)

fun String.asUnicode(): String =
    UtilKString.str2unicode(this)

fun Boolean.asStr(locale: Locale = Locale.CHINA): String =
    UtilKString.boolean2str(this, locale)

fun Double.decimal2str(pattern: String = "#.0"): String =
    UtilKString.decimal2str(this, pattern)

fun String.throwIllegalStateException() {
    UtilKString.throwIllegalStateException(this)
}

fun <T> Array<T>.joinArray2str(defaultValue: String = "", splitChar: String = ","): String =
    UtilKString.joinArray2Str(this, defaultValue, splitChar)

fun <T> List<T>.joinList2str(defaultValue: String = "", splitStr: String = ","): String =
    UtilKString.joinList2Str(this, defaultValue, splitStr)

fun String.getFilenameExtension(): String =
    UtilKString.getFilenameExtension(this)

fun CharSequence.asStringTrim(): String =
    UtilKString.asStringTrim(this)

fun Any.asStringTrim(): String =
    UtilKString.asStringTrim(this)

fun String.regexLineBreak2str(): String =
    UtilKString.regexLineBreak2str(this)

fun String.isNotEmptyOrElse(isNotEmptyBlock: I_Listener, orElseBlock: I_Listener) {
    UtilKString.isNotEmptyOrElse(this, isNotEmptyBlock, orElseBlock)
}

fun CharSequence.applyEquals(charSequence: CharSequence): Boolean =
    UtilKString.applyEquals(this, charSequence)

fun String.isHasSpace(): Boolean =
    UtilKString.isHasSpace(this)


object UtilKString : BaseUtilK() {

    @JvmStatic
    fun applyEquals(charSequence1: CharSequence, charSequence2: CharSequence): Boolean {
        if (charSequence1 === charSequence2) return true
        var length: Int
        return if (charSequence1.length.also { length = it } == charSequence2.length) {
            if (charSequence1 is String && charSequence2 is String) {
                charSequence1 == charSequence2
            } else {
                for (i in 0 until length) {
                    if (charSequence1[i] != charSequence2[i]) return false
                }
                true
            }
        } else false
    }

    @JvmStatic
    fun isNotEmptyOrElse(str: String, isNotEmptyBlock: I_Listener, orElseBlock: I_Listener) {
        if (str.isNotEmpty()) isNotEmptyBlock.invoke() else orElseBlock.invoke()
    }

    @JvmStatic
    fun asStringTrim(charSequence: CharSequence): String =
        charSequence.toString().trim()

    @JvmStatic
    fun asStringTrim(obj: Any): String =
        obj.toString().trim()

    /**
     * 包含String
     * @param content String
     * @param str String
     * @return Boolean
     */
    @JvmStatic
    fun containStr(content: String, str: String): Boolean {
        if (content.isEmpty() || str.isEmpty()) return false
        return content.contains(str)
    }

    /**
     * 判断是否不为Empty
     * @param str Array<out String>
     * @return Boolean
     */
    @JvmStatic
    fun isNotEmpty(vararg str: String): Boolean {
        for (char in str) if (char.isEmpty()) return false
        return true
    }

    /**
     * 是否含有空格
     * @param str String
     * @return Boolean
     */
    @JvmStatic
    fun isHasSpace(str: String): Boolean {
        var i = 0
        val len = str.length
        while (i < len) {
            if (!Character.isWhitespace(str[i])) return false
            ++i
        }
        return true
    }

    /**
     * 找到第一个匹配的字符的位置
     * @param content String
     * @param char Char
     * @return Int
     */
    @JvmStatic
    fun findFirst(content: String, char: Char): Int =
        content.indexOfFirst { it == char }

    /**
     * 找到第一个匹配的字符串的位置
     * @param content String
     * @param str String
     * @return Int
     */
    fun findFirst(content: String, str: String): Int =
        content.indexOf(str)

    /**
     * 切割字符串
     * @param content String
     * @param firstIndex Int
     * @param length Int
     * @return String
     */
    @JvmStatic
    fun subStr(content: String, firstIndex: Int, length: Int): String =
        content.substring(firstIndex.normalize(content.indices), if (firstIndex + length > content.length) content.length else firstIndex + length)

    /**
     * 是否为空
     */
    @JvmStatic
    fun isEmpty(str: CharSequence?): Boolean {
        return str.isNullOrEmpty()
    }

    /**
     * 获取分割后的最后一个元素
     * @param str String
     * @param splitStr String
     * @return String
     */
    @JvmStatic
    fun getSplitLast(str: String, splitStr: String): String =
        str.substring(str.lastIndexOf(splitStr) + 1, str.length)

    /**
     * 获取分割后的第一个元素
     * @param str String
     * @param splitStr String
     * @return String
     */
    @JvmStatic
    fun getSplitFirst(str: String, splitStr: String): String =
        str.substring(0, str.indexOf(splitStr))

    /**
     * icon代码转unicode
     * @param str String
     * @return String
     */
    @JvmStatic
    fun str2unicode(str: String): String {
        if (str.isEmpty()) return ""
        val stringBuffer = StringBuffer()
        if (str.startsWith("&#x")) {
            val hex = str.replace("&#x", "").replace(";", "").trim()
            val data = Integer.parseInt(hex, 16)
            stringBuffer.append(data.toChar())
        } else if (str.startsWith("&#")) {
            val hex = str.replace("&#", "").replace(";", "").trim()
            val data = Integer.parseInt(hex, 10)
            stringBuffer.append(data.toChar())
        } else if (str.startsWith("\\u")) {
            val hex = str.replace("\\u", "").trim()
            val data = Integer.parseInt(hex, 16)
            stringBuffer.append(data.toChar())
        } else {
            return str
        }

        return stringBuffer.toString()
    }

    /**
     * decimal转String
     * @param double Double
     * @param pattern String
     * @return String
     */
    @JvmStatic
    fun decimal2str(double: Double, pattern: String = "#.0"): String =
        DecimalFormat(pattern).format(double)

    /**
     * bool转String
     * @param bool Boolean
     * @param locale Locale
     * @return String
     */
    @JvmStatic
    fun boolean2str(bool: Boolean, locale: Locale = Locale.CHINA) =
        if (locale == Locale.CHINA) if (bool) "是" else "否" else (if (bool) "true" else "false")

    /**
     * 聚合array
     * @param array Array<T>
     * @param defaultValue String
     * @param splitChar String
     * @return String
     */
    @JvmStatic
    fun <T> joinArray2Str(array: Array<T>, defaultValue: String = "", splitChar: String = ","): String =
        joinList2Str(array.toList(), defaultValue, splitChar)

    /**
     * 聚合list
     * @param list List<T>
     * @param defaultValue String
     * @param splitChar String
     * @return String
     */
    @JvmStatic
    fun <T> joinList2Str(list: List<T>, defaultValue: String = "", splitChar: String = ","): String =
        if (UtilKBuildVersion.isAfterV_24_7_N()) {
            val ret = list.stream().map { elem: T? -> elem?.toString() ?: "" }
                .collect(Collectors.joining(splitChar))
            ret.ifEmpty { defaultValue }
        } else {
            val stringBuilder = StringBuilder()
            for (obj in list) stringBuilder.append(obj?.toString() ?: "").append(splitChar)
            if (stringBuilder.isNotEmpty()) stringBuilder.deleteAt(stringBuilder.length - 1).toString() else defaultValue
        }

    /**
     * 电话号码隐藏中间四位
     * @param str String
     * @return String
     */
    @JvmStatic
    fun hidePhone(str: String): String =
        if (str.length == 11) str.substring(0, 3) + "****" + str.substring(7, str.length) else str

    /**
     * 名字脱敏
     * @param str String
     * @return String
     */
    @JvmStatic
    fun hideName(str: String): String {
        if (str.isEmpty()) return ""
        if (str.length <= 2) return str
        val stringBuilder = StringBuilder()
        repeat(str.length - 2) {
            stringBuilder.append("*")
        }
        return str.first() + stringBuilder.toString() + str.last()
    }

    /**
     * 生成随机字符串
     * @return String
     */
    @JvmStatic
    fun getRandomUuid(): String =
        UUID.randomUUID().toString().replace("-", "")

    /**
     * 获取扩展名
     * @param str String
     * @return String
     */
    @JvmStatic
    fun getFilenameExtension(str: String): String = str.substring(str.lastIndexOf(".") + 1)

    @JvmStatic
    fun throwIllegalStateException(msgStr: String) {
        throw IllegalStateException(msgStr)
    }

    @JvmStatic
    fun regexLineBreak2str(str: String): String =
        str.replace("\\n".toRegex(), CMsg.LINE_BREAK)
}