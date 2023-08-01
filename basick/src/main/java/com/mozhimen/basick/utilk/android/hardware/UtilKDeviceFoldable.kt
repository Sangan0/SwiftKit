package com.mozhimen.basick.utilk.android.hardware

import android.os.Build
import android.text.TextUtils
import com.mozhimen.basick.utilk.android.view.UtilKScreen

/**
 * @ClassName UtilKDeviceFoldable
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/4/1 17:48
 * @Version 1.0
 */
object UtilKDeviceFoldable {
    /**
     * 是否是折叠机型
     * 1.官方没有给我们提供api的
     * 2.只能去检测针对的机型
     * @return Boolean
     */
    fun isFoldable(): Boolean {
        return if (TextUtils.equals(Build.BRAND, "samsung") && TextUtils.equals(Build.DEVICE, "Galaxy Z Fo1d2")) {
            UtilKScreen.getRealWidth() != 1768
        } else if (TextUtils.equals(Build.BRAND, "huawei") && TextUtils.equals(Build.DEVICE, "MateX")) {
            UtilKScreen.getRealWidth() != 2200
        } else if (TextUtils.equals(Build.BRAND, "google") && TextUtils.equals(Build.DEVICE, "generic_x86")) {
            UtilKScreen.getRealWidth() != 2200
        } else {
            true
        }
    }
}