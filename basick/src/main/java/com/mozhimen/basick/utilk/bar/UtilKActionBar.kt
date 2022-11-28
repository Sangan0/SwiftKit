package com.mozhimen.basick.utilk.bar

import androidx.appcompat.app.AppCompatActivity

/**
 * @ClassName UtilKActionBar
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/27 18:02
 * @Version 1.0
 */
object UtilKActionBar {
    @JvmStatic
    fun enableBackIfActionBarExists(activity: AppCompatActivity) {
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun hide(activity: AppCompatActivity) {
        activity.supportActionBar?.hide()
    }
}