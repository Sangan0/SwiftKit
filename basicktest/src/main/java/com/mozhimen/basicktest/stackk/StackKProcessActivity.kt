package com.mozhimen.basicktest.stackk

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVB
import com.mozhimen.basick.lintk.optins.OApiInit_InApplication
import com.mozhimen.basick.stackk.commons.IStackKListener
import com.mozhimen.basick.stackk.process.StackKProcess
import com.mozhimen.basick.utilk.android.widget.showToast
import com.mozhimen.basicktest.databinding.ActivityStackkProcessBinding
import java.lang.ref.WeakReference

class StackKProcessActivity : BaseActivityVB<ActivityStackkProcessBinding>(), IStackKListener {

    @OptIn(OApiInit_InApplication::class)
    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        val stackTopActivity = StackKProcess.instance.getStackTopActivity()
        val stackCount = StackKProcess.instance.getStackCount()
        vb.stackkProcessTitle.text = "StackTop: ${stackTopActivity?.javaClass?.simpleName ?: "Null"}, StackCount: $stackCount"

        StackKProcess.instance.addFrontBackListener(this)
    }

    @OptIn(OApiInit_InApplication::class)
    override fun onDestroy() {
        StackKProcess.instance.removeFrontBackListener(this)
        super.onDestroy()
    }

    override fun onChanged(isFront: Boolean, activityRef: WeakReference<Activity>?) {
        "App is At Front ?: $isFront".showToast()
        Log.d(TAG, "App is At Front ?: $isFront")
    }
}