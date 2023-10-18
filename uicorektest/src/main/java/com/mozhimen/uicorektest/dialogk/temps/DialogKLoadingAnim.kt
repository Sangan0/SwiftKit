package com.mozhimen.uicorektest.dialogk.temps

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mozhimen.basick.animk.builder.AnimKBuilder
import com.mozhimen.basick.animk.builder.temps.AnimKRotationRecyclerType
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.manifestk.annors.AManifestKRequire
import com.mozhimen.basick.utilk.android.view.stopAnim
import com.mozhimen.basick.utilk.android.widget.applyValueIfNotEmpty
import com.mozhimen.uicorek.dialogk.bases.BaseDialogK
import com.mozhimen.uicorek.dialogk.bases.commons.IDialogKClickListener
import com.mozhimen.uicorektest.R

/**
 * @ClassName LoadingDialog
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2022/12/19 15:37
 * @Version 1.0
 */
@AManifestKRequire(CPermission.SYSTEM_ALERT_WINDOW)
class DialogKLoadingAnim @JvmOverloads internal constructor(context: Context, private var _desc: String? = null) : BaseDialogK<IDialogKClickListener>(context) {
    private var _imgProgress: ImageView? = null
    private var _txtDesc: TextView? = null
    private val _rotateAnimation by lazy { AnimKBuilder.asAnimation().add(AnimKRotationRecyclerType()).setDuration(1000).build() }

    init {
        setDialogCancelable(true)
        setOnDismissListener {
            _imgProgress?.stopAnim()
        }
        setOnShowListener {
            _imgProgress?.startAnimation(_rotateAnimation)
        }
    }

    companion object {
        @JvmOverloads
        fun create(context: Context, desc: String? = null): DialogKLoadingAnim {
            return DialogKLoadingAnim(context, desc)
        }
    }

    override fun onCreateView(inflater: LayoutInflater): View {
        return inflater.inflate(R.layout.dialogk_loading_anim, null)
    }

    override fun onViewCreated(view: View) {
        _imgProgress = view.findViewById(R.id.dialogk_loading_img_progress)
        _txtDesc = view.findViewById(R.id.dialogk_loading_txt_desc)
        setDesc(_desc)
    }

    fun setDesc(desc: String?) {
        _txtDesc?.applyValueIfNotEmpty(desc)
    }
}