package com.mozhimen.basick.utilk.android.net

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.annotation.RequiresPermission
import com.mozhimen.basick.manifestk.annors.AManifestKRequire
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.utilk.android.content.UtilKContext

/**
 * @ClassName UtilKWifiManager
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/3/20 22:12
 * @Version 1.0
 */
@AManifestKRequire(CPermission.ACCESS_WIFI_STATE, CPermission.ACCESS_FINE_LOCATION)
object UtilKWifiManager {
    @JvmStatic
    fun get(context: Context): WifiManager =
        UtilKContext.getWifiManager(context)

    /////////////////////////////////////////////////////////////////////////////

    /**
     * 获取ConnectionInfo
     * @param context Context
     * @return WifiInfo
     */
    @JvmStatic
    @RequiresPermission(allOf = [CPermission.ACCESS_WIFI_STATE, CPermission.ACCESS_FINE_LOCATION])
    fun getConnectionInfo(context: Context): WifiInfo =
        get(context).connectionInfo

    /**
     * 获取Rssi
     * @param context Context
     * @return Int
     */
    @JvmStatic
    @RequiresPermission(allOf = [CPermission.ACCESS_WIFI_STATE, CPermission.ACCESS_FINE_LOCATION])
    fun getRssi(context: Context): Int =
        getConnectionInfo(context).rssi
}