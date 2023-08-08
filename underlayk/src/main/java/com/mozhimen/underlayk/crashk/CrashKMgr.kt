package com.mozhimen.underlayk.crashk

import com.mozhimen.basick.lintk.optin.OptInApiInit_InApplication
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.manifestk.annors.AManifestKRequire
import com.mozhimen.underlayk.crashk.commons.ICrashKListener
import java.io.File

/**
 * @ClassName CrashKMgr
 * @Description TODO
 * @Author Kolin Zhao / Mozhimen
 * @Date 2022/3/28 14:32
 * @Version 1.0
 */
@OptInApiInit_InApplication
@AManifestKRequire(CPermission.READ_PHONE_STATE, CPermission.READ_PRIVILEGED_PHONE_STATE)
class CrashKMgr {

    companion object {
        @JvmStatic
        val instance = INSTANCE.holder
    }

    //////////////////////////////////////////////////////////////

    private val _crashKJava by lazy { CrashKJava() }
    private val _crashKNative by lazy { CrashKNative() }

    fun init(
        crashKJavaListener: ICrashKListener? = null,
        crashKNativeListener: ICrashKListener? = null
    ) {
        _crashKJava.init(crashKJavaListener)
        _crashKNative.init(crashKNativeListener)
    }

    fun getJavaCrashFiles(): Array<File> {
        return _crashKJava.getJavaCrashFiles()
    }

    fun getNativeCrashFiles(): Array<File> {
        return _crashKNative.getNativeCrashFiles()
    }

    fun getCrashFiles(): Array<File> {
        return _crashKJava.getJavaCrashFiles() + _crashKNative.getNativeCrashFiles()
    }

    //////////////////////////////////////////////////////////////

    private object INSTANCE {
        val holder = CrashKMgr()
    }
}