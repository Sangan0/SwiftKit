package com.mozhimen.basick.animk

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.annotation.FloatRange
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.animk.builder.AnimKBuilder
import com.mozhimen.basick.animk.builder.commons.IAnimatorUpdateListener
import com.mozhimen.basick.animk.builder.temps.DrawableAlphaRecyclerType
import com.mozhimen.basick.animk.builder.temps.DrawableColorRecyclerType
import com.mozhimen.basick.taskk.commons.ITaskK
import com.mozhimen.basick.utilk.UtilKAnim
import com.mozhimen.basick.utilk.UtilKAnimator
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName LifecycleAnimatorHelper
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/20 22:34
 * @Version 1.0
 */
class AnimKLifecycleAnimatorHelper(owner: LifecycleOwner) : ITaskK(owner) {
    private val _viewAndListeners: ConcurrentHashMap<View, Animator> = ConcurrentHashMap()

    /**
     * 背景变换
     * @param view View
     * @param colorStart Int
     * @param colorEnd Int
     */
    fun betweenColors(view: View, colorStart: Int, colorEnd: Int) {
        val colorDrawable = ColorDrawable(colorStart)
        val colorAnimator = AnimKBuilder.asAnimator().asAlpha(DrawableColorRecyclerType().setColorRange(colorStart, colorEnd).addAnimatorUpdateListener(object : IAnimatorUpdateListener {
            override fun onChange(value: Int) {
                colorDrawable.color = value
                ViewCompat.setBackground(view, colorDrawable)
            }
        })).build()
        _viewAndListeners[view] = colorAnimator
    }

    /**
     * 背景透明度变换
     * @param view View
     * @param alphaStart Float
     * @param alphaEnd Float
     */
    fun alphaFlash(view: View, @FloatRange(from = 0.0, to = 1.0) alphaEnd: Float, @FloatRange(from = 0.0, to = 1.0) alphaStart: Float = 1f) {
        val alphaDrawable = view.background
        val alphaAnimator = AnimKBuilder.asAnimator().asAlpha(DrawableAlphaRecyclerType().addAnimatorUpdateListener(object : IAnimatorUpdateListener {
            override fun onChange(value: Int) {
                alphaDrawable.alpha = value
                ViewCompat.setBackground(view, alphaDrawable)
            }
        }).setAlpha(alphaStart, alphaEnd)).build()
        _viewAndListeners[view] = alphaAnimator
    }

    override fun cancel() {
        _viewAndListeners.forEach {
            (it.value as ValueAnimator).removeAllUpdateListeners()
            UtilKAnimator.releaseAnimator(it.value)
            UtilKAnim.stopAnim(it.key)
        }
        _viewAndListeners.clear()
    }
}