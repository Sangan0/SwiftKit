package com.swiftmk.app

import android.content.Context
import android.os.Bundle
import com.swiftmk.app.databinding.ActivitySecondBinding
import com.swiftmk.library.base.BaseActivity
import com.swiftmk.library.helper.ActivityBridger
import com.swiftmk.library.helper.ActivityBridgerImpl

@Suppress("CAST_NEVER_SUCCEEDS")
class SecondActivity : BaseActivity() {
    private lateinit var secondBinding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secondBinding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(secondBinding.root)

        secondBinding.secondReturn.setOnClickListener {

        }
    }

    companion object : ActivityBridgerImpl {
        override fun actionStart(context: Context, data: HashMap<String, String>) {
            ActivityBridger.actionStart(context, SecondActivity::class.java, data)
        }
    }
}