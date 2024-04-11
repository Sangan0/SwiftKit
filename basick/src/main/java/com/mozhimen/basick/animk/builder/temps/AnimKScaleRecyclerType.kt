package com.mozhimen.basick.animk.builder.temps

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import com.mozhimen.basick.animk.builder.mos.MAnimKConfig

/**
 * @ClassName ScaleRecyclerType
 * @Description 开始扩大然后缩小.像波浪一样
 * @Author mozhimen / Kolin Zhao
 * @Version 1.0
 */
class AnimKScaleRecyclerType : AnimKScaleType() {
    init {
        setInterpolator(LinearInterpolator())
        scale(1f, 1f)
        hide()
    }

    override fun formatAnimation(animKConfig: MAnimKConfig, animation: Animation) {
        super.formatAnimation(animKConfig, animation)
        animation.apply {
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }
    }

    override fun formatAnimator(animKConfig: MAnimKConfig, animator: Animator) {
        super.formatAnimator(animKConfig, animator)
        (animator as AnimatorSet).childAnimations.forEach {
            (it as ObjectAnimator).apply {
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }
        }
    }
}