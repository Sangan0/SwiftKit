package com.mozhimen.underlayk.logk.temps.printer

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.mozhimen.basick.lintk.optin.OptInApiInit_ByLazy
import com.mozhimen.basick.elemk.androidx.lifecycle.bases.BaseWakeBefPauseLifecycleObserver
import com.mozhimen.basick.lintk.optin.OptInApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optin.OptInApiInit_InApplication
import com.mozhimen.basick.utilk.android.app.getContentView
import com.mozhimen.underlayk.logk.LogKMgr
import com.mozhimen.underlayk.logk.commons.ILogKPrinter
import com.mozhimen.underlayk.logk.bases.BaseLogKConfig

/**
 * @ClassName ViewPrinter
 * @Description TODO
 * @Author Kolin Zhao / Mozhimen
 * @Date 2021/12/20 17:20
 * @Version 1.0
 */
@OptIn(OptInApiInit_ByLazy::class, OptInApiCall_BindLifecycle::class, OptInApiInit_InApplication::class)
class LogKPrinterView<A>(owner: A) : ILogKPrinter, BaseWakeBefPauseLifecycleObserver() where A : Activity, A : LifecycleOwner {
    private val _viewProvider: LogKPrinterViewProvider by lazy { LogKPrinterViewProvider(owner, owner.getContentView()) }
    private var _isFold = false
    private var _isShow: Boolean = false
        set(value) {
            if (value == field) return
            if (value) {
                _viewProvider.showLogView(_isFold)

            } else {
                _viewProvider.closeLogView()
            }
            field = value
        }

    init {
        bindLifecycle(owner)
    }

    fun toggleView(isFold: Boolean = true) {
        _isFold = isFold
        _isShow = !_isShow
    }

    fun getViewProvider(): LogKPrinterViewProvider =
        _viewProvider

    override fun print(config: BaseLogKConfig, priority: Int, tag: String, msg: String) {
        _viewProvider.print(config, priority, tag, msg)
    }

    override fun onPause(owner: LifecycleOwner) {
        _isShow = false
        LogKMgr.instance.removePrinter(this)
        super.onPause(owner)
    }
}