package com.mozhimen.basick.manifestk.permission

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.mozhimen.basick.elemk.commons.IA_Listener
import com.mozhimen.basick.elemk.commons.I_Listener
import com.mozhimen.basick.manifestk.permission.annors.APermissionCheck
import com.mozhimen.basick.manifestk.permission.commons.IManifestKPermissionListener
import com.mozhimen.basick.manifestk.permission.helpers.InvisibleProxyFragment
import com.mozhimen.basick.utilk.bases.BaseUtilK
import com.mozhimen.basick.utilk.android.app.UtilKLaunchActivity
import com.mozhimen.basick.utilk.android.app.UtilKPermission
import com.mozhimen.basick.utilk.android.app.getAnnotation
import com.mozhimen.basick.utilk.android.util.wt
import com.mozhimen.basick.utilk.android.widget.showToastOnMain

/**
 * @ClassName ManifestKPermission
 * @Description TO
 * @Author mozhimen
 * @Date 2021/4/14 17:08
 * @Version 1.0
 */
object ManifestKPermission : BaseUtilK() {

    @JvmStatic
    fun requestPermissions(
        activity: AppCompatActivity,
        onSuccess: I_Listener,
        onFail: I_Listener? = { UtilKLaunchActivity.startSettingAppDetails(activity) }
    ) {
        requestPermissions(
            activity,
            onResult = { if (it) onSuccess.invoke() else onFail?.invoke() })
    }

    @JvmStatic
    @Throws(Exception::class)
    fun requestPermissions(
        activity: AppCompatActivity,
        onResult: IA_Listener<Boolean>/*(isGranted: Boolean) -> Unit*/? = null,
    ) {
        val permissionAnnor = activity.getAnnotation(APermissionCheck::class.java)
        require(permissionAnnor != null) { "$TAG you may be forget add annor" }
        val permissions = mutableSetOf<String>()
        permissions.addAll(permissionAnnor.permissions)
        requestPermissions(activity, permissions.toTypedArray(), onResult)
    }

    @JvmStatic
    fun requestPermissions(
        activity: AppCompatActivity,
        permissions: Array<String>,
        onResult: IA_Listener<Boolean>/*(isGranted: Boolean) -> Unit*/? = null
    ) {
        if (permissions.isNotEmpty()) {
            if (!UtilKPermission.hasPermissions(permissions)) {
                requestPermissionsByFragment(activity, permissions) { isAllGranted, deniedList ->
                    printDeniedPermissions(deniedList)
                    onResult?.invoke(isAllGranted)
                }
            } else onResult?.invoke(true)
        } else onResult?.invoke(true)
    }

    @JvmStatic
    fun requestPermissionsByFragment(
        activity: FragmentActivity,
        permissions: Array<out String>,
        onResult: IManifestKPermissionListener
    ) {
        val noPermissions = mutableListOf<String>()
        for (permission in permissions) {
            if (!UtilKPermission.hasPermission(permission))
                noPermissions.add(permission)
        }
        if (noPermissions.isNotEmpty()) {
            val fragmentManager = activity.supportFragmentManager
            val existedFragment = fragmentManager.findFragmentByTag(TAG)
            val fragment = if (existedFragment != null) {
                existedFragment as InvisibleProxyFragment
            } else {
                val invisibleProxyFragment = InvisibleProxyFragment()
                fragmentManager.beginTransaction().add(invisibleProxyFragment, TAG).commitNow()
                invisibleProxyFragment
            }
            fragment.request(noPermissions.toTypedArray(), onResult)
        } else {
            onResult.invoke(true, emptyList())
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    private fun printDeniedPermissions(deniedList: List<String>) {
        "printDeniedPermissions $deniedList".wt(TAG)
        if (deniedList.isNotEmpty())
            "请在设置中打开${deniedList.joinToString()}权限".showToastOnMain()
    }
}