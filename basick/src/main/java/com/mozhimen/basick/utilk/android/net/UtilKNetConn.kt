package com.mozhimen.basick.utilk.android.net

import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.mozhimen.basick.elemk.android.net.cons.ENetType
import com.mozhimen.basick.manifestk.annors.AManifestKRequire
import com.mozhimen.basick.manifestk.cons.CPermission
import com.mozhimen.basick.utilk.bases.BaseUtilK
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import kotlin.math.abs

/**
 * @ClassName UtilKNet
 * @Description TODO
 * @Author Kolin Zhao / Mozhimen
 * @Date 2022/2/16 19:57
 * @Version 1.0
 */
@AManifestKRequire(
    CPermission.ACCESS_NETWORK_STATE,
    CPermission.ACCESS_WIFI_STATE,
    CPermission.INTERNET
)
object UtilKNetConn : BaseUtilK() {

    @JvmStatic
    fun getConnectivityManager(): ConnectivityManager =
        UtilKConnectivityManager.get(_context)

    @JvmStatic
    fun getWifiManager(): WifiManager =
        UtilKWifiManager.get(_context)

    @JvmStatic
    @RequiresPermission(value = CPermission.ACCESS_NETWORK_STATE)
    fun registerNetworkCallback(request: NetworkRequest, networkCallback: ConnectivityManager.NetworkCallback) {
        UtilKConnectivityManager.get(_context).registerNetworkCallback(request, networkCallback)
    }

    @JvmStatic
    fun unregisterNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        UtilKConnectivityManager.get(_context).unregisterNetworkCallback(networkCallback)
    }


    /**
     * 获取Wifi强度
     * @return Int
     */
    @JvmStatic
    @RequiresPermission(allOf = [CPermission.ACCESS_WIFI_STATE, CPermission.ACCESS_FINE_LOCATION])
    fun getWifiStrength(): Int {
        return abs(UtilKWifiManager.getRssi(_context))
    }

    /**
     * 获取NetworkInfo
     * @return NetworkInfo?
     */
    @JvmStatic
    @RequiresPermission(CPermission.ACCESS_NETWORK_STATE)
    fun getActiveNetworkInfo(): NetworkInfo? {
        return UtilKConnectivityManager.getActiveNetworkInfo(_context)
    }

    /**
     * 获取网路IP
     * @return String
     */
    @JvmStatic
    fun getIP(): String? {
        try {
            val networkInterfaces: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            var inetAddress: InetAddress
            while (networkInterfaces.hasMoreElements()) {
                val inetAddresses: Enumeration<InetAddress> = (networkInterfaces.nextElement() as NetworkInterface).inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement() as InetAddress
                    if (inetAddress !is Inet6Address) {
                        if (inetAddress.hostAddress != "127.0.0.1") {
                            return inetAddress.hostAddress ?: continue
                        }
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
            Log.e(TAG, "getIP SocketException ${e.message}")
        }
        return null
    }

    @JvmStatic
    fun getNetType(): ENetType {
        var netKType = ENetType.NONE
        val activeNetworkInfo: NetworkInfo? = getActiveNetworkInfo()
        if (activeNetworkInfo != null && activeNetworkInfo.isAvailable) {
            netKType = when (activeNetworkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> ENetType.WIFI
                ConnectivityManager.TYPE_MOBILE -> {
                    when (activeNetworkInfo.subtype) {
                        TelephonyManager.NETWORK_TYPE_TD_SCDMA, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> ENetType.M3G
                        TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN -> ENetType.M4G
                        TelephonyManager.NETWORK_TYPE_GSM, TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> ENetType.M2G
                        else -> {
                            val subtypeName = activeNetworkInfo.subtypeName
                            if (subtypeName.equals("TD-SCDMA", ignoreCase = true) || subtypeName.equals("WCDMA", ignoreCase = true) || subtypeName.equals("CDMA2000", ignoreCase = true)) ENetType.M3G
                            else ENetType.UNKNOWN
                        }
                    }
                }

                else -> ENetType.UNKNOWN
            }
        }
        return netKType
    }

    /**
     * 网络是否连接
     * @return Boolean
     */
    @JvmStatic
    @RequiresPermission(CPermission.ACCESS_NETWORK_STATE)
    fun isNetConnected(): Boolean {
        UtilKConnectivityManager.getAllNetworkInfo(_context).forEach {
            if (it.isConnected) return true
        }
        return false
    }

    /**
     * 是否连接网络,需要权限:ACCESS_NETWORK_STATE
     * @return Boolean
     */
    @JvmStatic
    @RequiresPermission(CPermission.ACCESS_NETWORK_STATE)
    fun isNetAvailable(): Boolean =
        UtilKConnectivityManager.getActiveNetworkInfo(_context)?.isAvailable ?: false

    /**
     * 是否连接无线网
     * @return Boolean
     */
    @JvmStatic
    @RequiresPermission(CPermission.ACCESS_NETWORK_STATE)
    fun isWifiConnected(): Boolean {
        val activeNetworkInfo = UtilKConnectivityManager.getActiveNetworkInfo(_context)
        return activeNetworkInfo != null && activeNetworkInfo.state == NetworkInfo.State.CONNECTED && activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
    }

    /**
     * 是否连接移动网络
     * @return Boolean
     */
    @JvmStatic
    @RequiresPermission(CPermission.ACCESS_NETWORK_STATE)
    fun isMobileConnected(): Boolean {
        val activeNetworkInfo = UtilKConnectivityManager.getActiveNetworkInfo(_context)
        return activeNetworkInfo != null && activeNetworkInfo.state == NetworkInfo.State.CONNECTED && activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
    }

    /**
     * 获取连接类型
     * @return Int
     */
    @JvmStatic
    @RequiresPermission(CPermission.ACCESS_NETWORK_STATE)
    fun getConnectionType(): Int =
        UtilKConnectivityManager.getActiveNetworkInfo(_context)?.type ?: -1

    /**
     * 打印连接信息
     */
    @JvmStatic
    @RequiresPermission(CPermission.ACCESS_NETWORK_STATE)
    fun printActiveNetworkInfo() {
        val activeNetworkInfo = UtilKConnectivityManager.getActiveNetworkInfo(_context)
        if (activeNetworkInfo != null) {
            Log.i(TAG, "isAvailable " + activeNetworkInfo.isAvailable + " isConnected " + activeNetworkInfo.isConnected + " networkInfo " + activeNetworkInfo.toString())
            Log.i(TAG, "subtypeName " + activeNetworkInfo.subtypeName + " typeName " + activeNetworkInfo.typeName + " extraInfo " + activeNetworkInfo.extraInfo)
        }
    }
}