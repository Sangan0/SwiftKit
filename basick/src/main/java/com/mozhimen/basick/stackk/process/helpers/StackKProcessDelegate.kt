package com.mozhimen.basick.stackk.process.helpers

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.mozhimen.basick.lintk.optin.OptInApiInit_ByLazy
import com.mozhimen.basick.elemk.androidx.lifecycle.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.basick.lintk.optin.OptInApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optin.OptInApiInit_InApplication
import com.mozhimen.basick.stackk.cb.StackKCb
import com.mozhimen.basick.stackk.commons.IStackK
import com.mozhimen.basick.stackk.commons.IStackKListener
import java.lang.ref.WeakReference

/**
 * @ClassName StackKProcessProxy
 * @Description TODO
 * @Author Mozhimen / Kolin Zhao
 * @Date 2023/6/11 14:34
 * @Version 1.0
 */
@OptInApiInit_InApplication
internal class StackKProcessDelegate : IStackK {
    @OptIn(OptInApiInit_ByLazy::class, OptInApiCall_BindLifecycle::class)
    private val _applicationLifecycleProxy by lazy { ApplicationLifecycleProxy() }
    private val _frontBackListeners = ArrayList<IStackKListener>()
    private var _isFront = true

    /////////////////////////////////////////////////////////////////////////

    @OptIn(OptInApiInit_ByLazy::class, OptInApiCall_BindLifecycle::class)
    override fun init() {
        _applicationLifecycleProxy.bindLifecycle(ProcessLifecycleOwner.get())
    }


    override fun addFrontBackListener(listener: IStackKListener) {
        if (!_frontBackListeners.contains(listener)) {
            _frontBackListeners.add(listener)
        }
    }

    override fun removeFrontBackListener(listener: IStackKListener) {
        if (_frontBackListeners.contains(listener)) {
            _frontBackListeners.remove(listener)
        }
    }

    override fun getFrontBackListeners(): ArrayList<IStackKListener> =
        _frontBackListeners

    override fun getStackTopActivity(): Activity? =
        StackKCb.instance.getStackTopActivity()

    override fun getStackTopActivity(onlyAlive: Boolean): Activity? =
        StackKCb.instance.getStackTopActivity(onlyAlive)

    override fun getStackTopActivityRef(): WeakReference<Activity>? =
        StackKCb.instance.getStackTopActivityRef()

    override fun getStackTopActivityRef(onlyAlive: Boolean): WeakReference<Activity>? =
        StackKCb.instance.getStackTopActivityRef(onlyAlive)

    override fun getActivityRefs(): ArrayList<WeakReference<Activity>> =
        StackKCb.instance.getActivityRefs()

    override fun getStackCount(): Int =
        StackKCb.instance.getStackCount()

    override fun getLaunchCount(): Int =
        StackKCb.instance.getLaunchCount()

    override fun finishAllActivity() {
        StackKCb.instance.finishAllActivity()
    }

    override fun finishAllInvisibleActivity() {
        StackKCb.instance.finishAllInvisibleActivity()
    }

    override fun isFront(): Boolean =
        _isFront

    /////////////////////////////////////////////////////////////////////////

    @OptInApiCall_BindLifecycle
    @OptInApiInit_ByLazy
    private inner class ApplicationLifecycleProxy : BaseWakeBefDestroyLifecycleObserver() {

        /**
         * 在程序的整个生命周期中只会调用一次
         * @param owner LifecycleOwner
         */
        override fun onCreate(owner: LifecycleOwner) {
            Log.d(TAG, "onCreate")
        }

        /**
         * 当程序在前台时调用
         * @param owner LifecycleOwner
         */
        override fun onStart(owner: LifecycleOwner) {
            Log.d(TAG, "onStart")
            if (getLaunchCount() > 0 && !_isFront && owner is Activity) {
                onFrontBackChanged(true.also { _isFront = true }, owner)
            }
        }

        /**
         * 当程序在前台时调用
         * @param owner LifecycleOwner
         */
        override fun onResume(owner: LifecycleOwner) {
            Log.d(TAG, "onResume")
        }

        /**
         * 在程序在后台时调用
         * @param owner LifecycleOwner
         */
        override fun onPause(owner: LifecycleOwner) {
            Log.d(TAG, "onPause")
        }

        /**
         * 在程序在后台时调用
         * @param owner LifecycleOwner
         */
        override fun onStop(owner: LifecycleOwner) {
            Log.d(TAG, "onStop")
            if (getLaunchCount() <= 0 && _isFront && owner is Activity) {
                onFrontBackChanged(false.also { _isFront = false }, owner)
            }
        }

        /**
         * 永远不会调用
         * @param owner LifecycleOwner
         */
        override fun onDestroy(owner: LifecycleOwner) {
            Log.d(TAG, "onDestroy")
            super.onDestroy(owner)
        }

        //////////////////////////////////////////////////////////////////////////

        private fun onFrontBackChanged(isFront: Boolean, activity: Activity) {
            for (listener in _frontBackListeners) {
                listener.onChanged(isFront, WeakReference(activity))
            }
        }
    }
}