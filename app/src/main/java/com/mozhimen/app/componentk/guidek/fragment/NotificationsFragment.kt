package com.mozhimen.app.componentk.guidek.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mozhimen.app.R
import com.mozhimen.app.databinding.FragmentGuidekNotificationsBinding
import com.mozhimen.basicsk.basek.BaseKFragment
import com.mozhimen.basicsk.basek.BaseKViewModel
import com.mozhimen.guidek.annor.GuideKDestination

/**
 * @ClassName HomeFragment
 * @Description TODO
 * @Author Kolin Zhao
 * @Date 2021/9/3 13:15
 * @Version 1.0
 */
@GuideKDestination(pageUrl = "main/guidek/notifications", pageIndex = 1)
class NotificationsFragment :
    BaseKFragment<FragmentGuidekNotificationsBinding, BaseKViewModel>(R.layout.fragment_guidek_notifications) {

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: ")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause: ")
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: ")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")
    }
}