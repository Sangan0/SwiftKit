package com.mozhimen.basick.taskk.temps

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.annotation.FloatRange
import androidx.core.view.ViewCompat
import com.mozhimen.basick.animk.builder.AnimKBuilder
import com.mozhimen.basick.animk.builder.commons.IAnimatorUpdateListener
import com.mozhimen.basick.animk.builder.temps.AnimatorAlphaType
import com.mozhimen.basick.animk.builder.temps.AnimatorColorRecyclerType
import com.mozhimen.basick.lintk.optins.OApiInit_ByLazy
import com.mozhimen.basick.lintk.optins.OApiCall_BindLifecycle
import com.mozhimen.basick.taskk.bases.BaseWakeBefDestroyTaskK
import com.mozhimen.basick.utilk.android.animation.UtilKAnimator
import com.mozhimen.basick.utilk.android.view.UtilKAnim
import java.util.concurrent.ConcurrentHashMap

/**
 * @ClassName TaskKAnimKAnimator
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/20 22:34
 * @Version 1.0
 */
@OApiCall_BindLifecycle
@OApiInit_ByLazy
class TaskKAnimator : BaseWakeBefDestroyTaskK() {
    private val _viewAnimMap: ConcurrentHashMap<View, Animator> = ConcurrentHashMap()

    /**
     * 背景变换
     * @param view View
     * @param colorStart Int
     * @param colorEnd Int
     */
    fun betweenColors(view: View, colorStart: Int, colorEnd: Int) {
        val colorDrawable = ColorDrawable(colorStart)
        val colorAnimator = AnimKBuilder.asAnimator().add(AnimatorColorRecyclerType().setColorRange(colorStart, colorEnd).addAnimatorUpdateListener(object : IAnimatorUpdateListener<Int> {
            override fun onChange(value: Int) {
                colorDrawable.color = value
                ViewCompat.setBackground(view, colorDrawable)
            }
        })).build()
        _viewAnimMap[view] = colorAnimator
    }

    /**
     * 背景透明度变换
     * @param view View
     * @param alphaStart Float
     * @param alphaEnd Float
     */
    fun alphaFlash(view: View, @FloatRange(from = 0.0, to = 1.0) alphaEnd: Float, @FloatRange(from = 0.0, to = 1.0) alphaStart: Float = 1f) {
        val alphaDrawable = view.background
        val alphaAnimator = AnimKBuilder.asAnimator().add(AnimatorAlphaType().addAnimatorUpdateListener(object : IAnimatorUpdateListener<Int> {
            override fun onChange(value: Int) {
                alphaDrawable.alpha = value
                ViewCompat.setBackground(view, alphaDrawable)
            }
        }).setAlpha(alphaStart, alphaEnd)).build()
        _viewAnimMap[view] = alphaAnimator
    }

    override fun isActive(): Boolean {
        return _viewAnimMap.isNotEmpty()
    }

    /**
     * 取消
     */
    override fun cancel() {
        _viewAnimMap.forEach {
            (it.value as ValueAnimator).removeAllUpdateListeners()
            UtilKAnimator.releaseAnimator(it.value)
            UtilKAnim.stopAnim(it.key)
        }
        _viewAnimMap.clear()
    }
}