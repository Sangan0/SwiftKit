package com.mozhimen.basick.utilk.kotlin

/**
 * @ClassName UtilKLongTime
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2024/4/1
 * @Version 1.0
 */
object UtilKLongTime {
    //是否是整小时
    @JvmStatic
    fun isLongTime_ofHH(longTime: Long): Boolean =
        (longTime / 1000) % 3600 == 0L
}