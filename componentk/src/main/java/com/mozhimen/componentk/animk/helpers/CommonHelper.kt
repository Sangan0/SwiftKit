package com.mozhimen.componentk.animk.helpers

import android.view.View

object CommonHelper {
    @JvmStatic
    fun stopAnim(view: View) {
        view.apply {
            animation?.cancel()
            view.clearAnimation()
        }
    }
}