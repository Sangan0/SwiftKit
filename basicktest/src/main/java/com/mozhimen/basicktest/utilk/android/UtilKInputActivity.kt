package com.mozhimen.basicktest.utilk.android

import android.view.MotionEvent
import android.view.View
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVB
import com.mozhimen.basick.utilk.android.view.UtilKInputManager
import com.mozhimen.basicktest.databinding.ActivityUtilkKeyboardBinding


/**
 * @ClassName UtilKKeyBoardActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/3/2 17:49
 * @Version 1.0
 */
class UtilKInputActivity : BaseActivityVB<ActivityUtilkKeyboardBinding>() {
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val focusView: View? = currentFocus
            if (focusView != null && UtilKInputManager.isShouldHide(focusView, event)) {
                UtilKInputManager.hide(this)
            }
        }
        return super.dispatchTouchEvent(event)
    }
}