package com.mozhimen.abilityktest.scank

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.mozhimen.abilityk.scank.ScanKQR
import com.mozhimen.abilityktest.databinding.ActivityScankBinding
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVB
import com.mozhimen.basick.utilk.android.content.startContext

class ScanKActivity : BaseActivityVB<ActivityScankBinding>() {

    @SuppressLint("SetTextI18n")
    override fun initView(savedInstanceState: Bundle?) {
        vb.scankDemoBtnCreate.setOnClickListener {
            if (vb.scankDemoEdit.text.isEmpty()) {
                Toast.makeText(this, "请输入code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            vb.scankDemoImg.setImageBitmap(
                ScanKQR.createQRCodeBitmap(
                    vb.scankDemoEdit.text.toString(),
                    vb.scankDemoImg.width
                )
            )
        }
    }

    fun goScanKHSV(view: View) {
        startContext<ScanKHSVActivity>()
    }

    fun goScanKFace(view: View) {
        startContext<ScanKFaceActivity>()
    }
}
