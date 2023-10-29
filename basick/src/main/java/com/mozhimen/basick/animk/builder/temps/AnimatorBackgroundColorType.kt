package com.mozhimen.basick.animk.builder.temps

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import com.mozhimen.basick.animk.builder.bases.BaseAnimatorType
import com.mozhimen.basick.animk.builder.mos.MAnimKConfig


/**
 * @ClassName BackgroundColorAnimatorType
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/1/29 16:08
 * @Version 1.0
 */
class AnimatorBackgroundColorType : BaseAnimatorType<AnimatorBackgroundColorType>() {
    private var _colors: IntArray = intArrayOf(Color.WHITE, Color.BLACK)
    override lateinit var _animator: Animator
    private var _view: View? = null

    fun setColors(@ColorInt vararg intColor: Int): AnimatorBackgroundColorType {
        _colors = intColor
        return this
    }

    fun setView(view: View): AnimatorBackgroundColorType {
        _view = view
        return this
    }

    override fun buildAnimator(animKConfig: MAnimKConfig): Animator {
        requireNotNull(_view) { "$TAG you should set _view" }
        _animator = ObjectAnimator.ofInt(_view!!, "backgroundColor", *_colors)
        (_animator as ObjectAnimator).setEvaluator(ArgbEvaluator())
        formatAnimator(animKConfig, _animator)
        return _animator
    }
}