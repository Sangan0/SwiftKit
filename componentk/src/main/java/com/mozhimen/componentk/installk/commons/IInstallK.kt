package com.mozhimen.componentk.installk.commons

import com.mozhimen.componentk.installk.InstallK
import com.mozhimen.componentk.installk.cons.EInstallKMode

/**
 * @ClassName IInstallK
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/5/17 18:21
 * @Version 1.0
 */
interface IInstallK {
    fun setInstallMode(mode: EInstallKMode): InstallK
    fun setInstallSilenceReceiver(receiverClazz: Class<*>): InstallK
    fun setInstallSmartService(serviceClazz: Class<*>): InstallK
    fun setInstallStateChangeListener(listener: IInstallKStateListener): InstallK
}