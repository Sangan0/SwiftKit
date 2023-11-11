package com.mozhimen.basick.elemk.android.content.bases

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.lintk.optin.OptInApiInit_ByLazy
import com.mozhimen.basick.elemk.androidx.lifecycle.bases.BaseWakeBefDestroyLifecycleObserver
import com.mozhimen.basick.elemk.commons.IConnectionListener
import com.mozhimen.basick.lintk.optin.OptInApiCall_BindLifecycle
import com.mozhimen.basick.utilk.androidx.lifecycle.runOnMainThread


/**
 * @ClassName BaseReceiverProxy
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/1/10 14:32
 * @Version 1.0
 */
@OptInApiCall_BindLifecycle
@OptInApiInit_ByLazy
open class BaseBroadcastReceiverProxy2 : BaseWakeBefDestroyLifecycleObserver {
    protected val _activity: Context
    protected val _owner: LifecycleOwner
    protected val _receiver: BroadcastReceiver
    protected val _actions: Array<out String>

    /////////////////////////////////////////////////////////////////////////////

    constructor(activity: Context, owner: LifecycleOwner, receiver: BroadcastReceiver, vararg actions: String) : this(activity, owner, receiver, actions)

    constructor(activity: Context, owner: LifecycleOwner, receiver: BroadcastReceiver, actions: Array<out String>) : super() {
        _activity = activity
        _owner = owner
        _receiver = receiver
        _actions = actions
        _owner.runOnMainThread(::registerReceiver)
    }

    /////////////////////////////////////////////////////////////////////////////

    override fun onDestroy(owner: LifecycleOwner) {
        _activity.unregisterReceiver(_receiver)
        super.onDestroy(owner)
    }

    /////////////////////////////////////////////////////////////////////////////

    protected open fun registerReceiver() {
        val intentFilter = IntentFilter()
        if (_actions.isNotEmpty()) for (action in _actions) intentFilter.addAction(action)
        _activity.registerReceiver(_receiver, intentFilter)
    }
}