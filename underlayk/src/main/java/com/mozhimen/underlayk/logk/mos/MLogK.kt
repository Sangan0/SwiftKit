package com.mozhimen.underlayk.logk.mos

import com.mozhimen.basick.utilk.UtilKDate

/**
 * @ClassName LogKMo
 * @Description TODO
 * @Author Kolin Zhao / Mozhimen
 * @Date 2021/12/20 16:46
 * @Version 1.0
 */
class MLogK(private var timeMillis: Long, var level: Int, var tag: String, var log: String) {

    fun flattenedLog(): String {
        return getFlattened() + "\n" + log
    }

    fun getFlattened(): String {
        return "${UtilKDate.long2String(timeMillis, UtilKDate.FORMAT_yyyyMMddHHmmss)} | Level: ${CLogKType.getTypeName(level)} | Tag: $tag :"
    }
}