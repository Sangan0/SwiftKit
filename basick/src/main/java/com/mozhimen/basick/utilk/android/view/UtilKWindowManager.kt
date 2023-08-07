package com.mozhimen.basick.utilk.android.view

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.view.Display
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.annotation.RequiresApi
import com.mozhimen.basick.elemk.android.os.cons.CVersCode
import com.mozhimen.basick.utilk.android.content.UtilKContext


/**
 * @ClassName UtilKWindowManager
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/3/20 10:33
 * @Version 1.0
 */
object UtilKWindowManager {

    @JvmStatic
    fun get(context: Context): WindowManager =
        UtilKContext.getWindowManager(context)

    @RequiresApi(CVersCode.V_30_11_R)
    @JvmStatic
    fun getCurrentWindowMetrics(context: Context): WindowMetrics =
        get(context).currentWindowMetrics

    @JvmStatic
    fun getDefaultDisplay(context: Context): Display =
        get(context).defaultDisplay

    /**
     * 获取旋转
     * @return Int
     */
    @JvmStatic
    fun getRotation(context: Context): Int =
        getDefaultDisplay(context).rotation

    @JvmStatic
    fun getSize(context: Context, size: Point) {
        getDefaultDisplay(context).getSize(size)
    }

    @JvmStatic
    fun getRealSize(context: Context, size: Point) {
        getDefaultDisplay(context).getRealSize(size)
    }

    @RequiresApi(CVersCode.V_30_11_R)
    @JvmStatic
    fun getBounds(context: Context): Rect =
        getCurrentWindowMetrics(context).bounds

    @RequiresApi(CVersCode.V_30_11_R)
    @JvmStatic
    fun getBoundsWidth(context: Context): Int =
        getBounds(context).width()

    @RequiresApi(CVersCode.V_30_11_R)
    @JvmStatic
    fun getBoundsHeight(context: Context): Int =
        getBounds(context).height()

    @JvmStatic
    fun getDefaultDisplayWidth(context: Context): Int =
        getDefaultDisplay(context).width

    @JvmStatic
    fun getDefaultDisplayHeight(context: Context): Int =
        getDefaultDisplay(context).height

    ///////////////////////////////////////////////////////////////////

    @JvmStatic
    fun getSize(context: Context): Point {
        val size = Point()
        getSize(context, size)
        return size
    }

    @JvmStatic
    fun getSizeY(context: Context): Int =
        getSize(context).y

    @JvmStatic
    fun getSizeX(context: Context): Int =
        getSize(context).x
}