package com.mozhimen.uicorektest.layoutk.refresh

import android.view.View
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVB
import com.mozhimen.basick.utilk.android.content.startContext
import com.mozhimen.uicorektest.databinding.ActivityLayoutkRefreshBinding

/**
 * @ClassName LayoutKRefreshActivity
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/7 1:36
 * @Version 1.0
 */
class LayoutKRefreshActivity : BaseActivityVB<ActivityLayoutkRefreshBinding>() {
    fun goLayoutKRefreshLottie(view: View) {
        startContext<LayoutKRefreshLottieActivity>()
    }

    fun goLayoutKRefreshText(view: View) {
        startContext<LayoutKRefreshTextActivity>()
    }
}