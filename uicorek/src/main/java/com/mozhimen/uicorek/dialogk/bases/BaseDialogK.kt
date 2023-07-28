package com.mozhimen.uicorek.dialogk.bases

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.ComponentDialog
import androidx.annotation.StyleRes
import androidx.lifecycle.lifecycleScope
import com.mozhimen.basick.elemk.android.view.cons.CWinMgr
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.manifestk.annors.AManifestKRequire
import com.mozhimen.basick.utilk.android.util.et
import com.mozhimen.basick.utilk.java.lang.UtilKThread
import com.mozhimen.uicorek.R
import com.mozhimen.uicorek.dialogk.bases.annors.ADialogMode
import com.mozhimen.uicorek.dialogk.bases.commons.IBaseDialogK
import com.mozhimen.uicorek.dialogk.bases.commons.IDialogKClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @ClassName BaseDialogK
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/11/24 22:31
 * @Version 1.0
 */
@AManifestKRequire(CPermission.SYSTEM_ALERT_WINDOW)
abstract class BaseDialogK<I : IDialogKClickListener> @JvmOverloads constructor(context: Context, @StyleRes themeResId: Int = R.style.DialogK_Theme_Blur) : ComponentDialog(context, themeResId),
    IBaseDialogK<I> {

    private var _isHasSetWindowAttr = false
    private var _dialogMode = ADialogMode.BOTH
    private var _dialogView: View? = null
    private var _dialogClickListener: I? = null

    override fun getDialogClickListener(): I? {
        return _dialogClickListener
    }

    @ADialogMode
    override fun getDialogMode(): Int {
        return _dialogMode
    }

    override fun setDialogClickListener(listener: I): BaseDialogK<*> {
        this._dialogClickListener = listener
        return this
    }

    override fun setDialogMode(@ADialogMode mode: Int): BaseDialogK<*> {
        return setDialogMode(mode, true)
    }

    override fun setDialogMode(@ADialogMode mode: Int, callModeChange: Boolean): BaseDialogK<*> {
        val hasChange = this._dialogMode != mode
        this._dialogMode = mode
        if (hasChange && callModeChange) {
            onInitMode(mode)
        }
        return this
    }

    override fun setDialogCancelable(flag: Boolean): BaseDialogK<*> {
        setCancelable(flag)
        return this
    }

    override fun show() {
        if (isShowing) return
        lifecycleScope.launch(Dispatchers.Main) {
            super.show()
        }
    }

    override fun showByDelay(delayMillis: Long) {
        if (isShowing) return
        lifecycleScope.launch(Dispatchers.Main) {
            if (delayMillis <= 0) {
                super.show()
            } else {
                delay(delayMillis)
                super.show()
            }
        }
    }

    override fun showInSystemWindow() {
        try {
            val window = window ?: return
            window.setType(CWinMgr.Lpt.SYSTEM_ALERT)
            show()
        } catch (e: Exception) {
            e.printStackTrace()
            e.message?.et(TAG)
        }
    }

    override fun dismiss() {
        if (!isShowing) return
        if (UtilKThread.isMainThread()) {
            super.dismiss()
        } else {
            lifecycleScope.launch(Dispatchers.Main) {
                super.dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (_dialogView == null) {
            _dialogView = onCreateView(LayoutInflater.from(context))
        }
        if (_dialogView == null) return
        onViewCreated(_dialogView!!)
        onInitMode(_dialogMode)
        setContentView(_dialogView!!)
        if (window != null && !_isHasSetWindowAttr) {
            val layoutParams = window!!.attributes
            layoutParams.width = onInitWindowWidth()
            layoutParams.height = onInitWindowHeight()
            layoutParams.gravity = onInitWindowGravity()
            window!!.attributes = layoutParams
            _isHasSetWindowAttr = true
        }
    }

    override fun onStop() {
        super.onStop()
        onDestroyView()
    }

    override fun onDestroyView() {}

    //////////////////////////////////////////////////////////////////////////////
    //callback
    //////////////////////////////////////////////////////////////////////////////

    override fun onInitWindowWidth(): Int {
        return ViewGroup.LayoutParams.WRAP_CONTENT
    }

    override fun onInitWindowHeight(): Int {
        return ViewGroup.LayoutParams.WRAP_CONTENT
    }

    override fun onInitWindowGravity(): Int {
        return Gravity.CENTER
    }
}