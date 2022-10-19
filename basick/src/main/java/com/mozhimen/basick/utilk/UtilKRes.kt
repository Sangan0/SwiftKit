package com.mozhimen.basick.utilk

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

/**
 * @ClassName UtilKRes
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2021/12/25 16:56
 * @Version 1.0
 */
object UtilKRes {
    private val _context = UtilKGlobal.instance.getApp()!!

    /**
     * 获取字符串
     * @param resId Int
     * @return String
     */
    @JvmStatic
    fun getString(@StringRes resId: Int): String =
        _context.getString(resId)

    /**
     * 获取字符串
     * @param resId Int
     * @param formatArgs Array<out Any?>
     * @return String
     */
    @JvmStatic
    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String =
        _context.getString(resId, *formatArgs)

    /**
     * 获取颜色
     * @param resId Int
     * @return Int
     */
    @JvmStatic
    fun getColor(@ColorRes resId: Int): Int =
        ContextCompat.getColor(_context, resId)

    /**
     * 获取颜色list
     * @param resId Int
     * @return ColorStateList?
     */
    @JvmStatic
    fun getColorStateList(@ColorRes resId: Int): ColorStateList? =
        ContextCompat.getColorStateList(_context, resId)

    /**
     * 获取Drawable
     * @param drawableId Int
     * @return Drawable?
     */
    @JvmStatic
    fun getDrawable(@DrawableRes drawableId: Int): Drawable? =
        ContextCompat.getDrawable(_context, drawableId)

    /**
     * 获取DimensionPixelSize
     * @param dimensionId Int
     * @return Int
     */
    @JvmStatic
    fun getDimensionPixelSize(dimensionId: Int): Int =
        _context.resources.getDimensionPixelSize(dimensionId)
}