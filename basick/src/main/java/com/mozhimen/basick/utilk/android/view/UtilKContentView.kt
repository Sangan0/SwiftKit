package com.mozhimen.basick.utilk.android.view

import android.app.Activity
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.mozhimen.basick.elemk.android.view.cons.CView
import com.mozhimen.basick.utilk.android.util.et
import com.mozhimen.basick.utilk.bases.BaseUtilK
import kotlin.math.abs

/**
 * @ClassName UtilKContentView
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/6/20 15:47
 * @Version 1.0
 */
object UtilKContentView : BaseUtilK() {

    @JvmStatic
    fun <V : View> get(activity: Activity): V =
        get(activity.window)

    @JvmStatic
    fun <V : View> get(window: Window): V =
        UtilKWindow.getContentView(window)

    @JvmStatic
    fun getAsViewGroup(activity: Activity): ViewGroup =
        get(activity.window)

    @JvmStatic
    fun getAsViewGroup(window: Window): ViewGroup =
        get(window)

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @JvmStatic
    fun getTag(window: Window, tag: Int): Any? =
        get<View>(window).getTag(tag)

    @JvmStatic
    fun getChild0(activity: Activity): View? =
        getChild0(activity.window)

    @JvmStatic
    fun getChild0(window: Window): View? =
        getAsViewGroup(window).getChildAt(0)

    @JvmStatic
    fun getInvisibleHeight(activity: Activity): Int =
        getInvisibleHeight(UtilKWindow.get(activity))

    @JvmStatic
    fun getInvisibleHeight(window: Window): Int {
        val contentView = get<View>(window)
        val outRect = Rect()
        UtilKView.getWindowVisibleDisplayFrame(contentView, outRect)
        Log.d(TAG, "getInvisibleHeight: " + (contentView.bottom - outRect.bottom))
        val delta = abs(contentView.bottom - outRect.bottom)
        return if (delta <= UtilKStatusBar.getHeight() + UtilKNavigationBar.getHeight()) 0 else delta
    }
}