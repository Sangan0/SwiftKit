package com.mozhimen.basicktest.manifestk

import android.view.View
import com.mozhimen.mvvmk.bases.activity.databinding.BaseActivityVDB
import com.mozhimen.kotlin.utilk.android.content.startContext
import com.mozhimen.basicktest.databinding.ActivityManifestkBinding


/**
 * @ClassName ManifestKActivity
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/1/12 14:17
 * @Version 1.0
 */
class ManifestKActivity : BaseActivityVDB<ActivityManifestkBinding>() {
    fun goManifestKPermission(view: View) {
        startContext<ManifestKPermissionActivity>()
    }
}