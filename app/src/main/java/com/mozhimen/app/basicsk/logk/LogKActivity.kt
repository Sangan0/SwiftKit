package com.mozhimen.app.basicsk.logk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mozhimen.app.databinding.ActivityLogkBinding
import com.mozhimen.basicsk.logk.LogK
import com.mozhimen.basicsk.logk.LogKMgr
import com.mozhimen.basicsk.logk.printers.PrinterView
import com.mozhimen.basicsk.logk.mos.LogKConfig
import com.mozhimen.basicsk.logk.mos.LogKType

class LogKActivity : AppCompatActivity() {
    private val vb: ActivityLogkBinding by lazy { ActivityLogkBinding.inflate(layoutInflater) }
    private val TAG = "LogKActivity>>>>>"
    private var _printerView: PrinterView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)

        initView()
    }

    private fun initView() {
        _printerView = PrinterView(this)
        _printerView!!.getViewProvider().showFloatingView()
        vb.logkBtnPrint.setOnClickListener {
            printLog()
        }
        vb.logkBtnPrinterList.setOnClickListener {
            printLog1()
        }
    }

    private fun printLog() {
        //初级用法
        LogK.i("just a test1!")

        //中级用法
        LogK.log(LogKType.W, TAG, "just a test2!")

        //高级用法
        LogK.log(object : LogKConfig() {
            override fun includeThread(): Boolean {
                return true
            }

            override fun stackTraceDepth(): Int {
                return 5
            }
        }, LogKType.E, TAG, "just a test3!")
    }

    private fun printLog1() {
        val stringBuilder = StringBuilder()
        LogKMgr.getInstance().getPrinters().forEach { printer ->
            stringBuilder.append(printer.getName() + ", ")
        }
        LogK.dt(TAG, stringBuilder)
    }

    override fun onResume() {
        super.onResume()
        LogKMgr.getInstance().addPrinter(_printerView!!)
    }

    override fun onPause() {
        super.onPause()
        LogKMgr.getInstance().removePrinter(_printerView!!)
    }
}