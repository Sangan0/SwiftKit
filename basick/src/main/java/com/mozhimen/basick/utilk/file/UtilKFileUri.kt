package com.mozhimen.basick.utilk.file

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.mozhimen.basick.elemk.cons.CVersionCode
import com.mozhimen.basick.manifestk.annors.AManifestKRequire
import com.mozhimen.basick.manifestk.cons.CManifest
import com.mozhimen.basick.utilk.context.UtilKApplication
import java.io.File


/**
 * @ClassName UtilKFileUri
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/1/12 18:58
 * @Version 1.0
 */
@AManifestKRequire(CManifest.PROVIDER)
object UtilKFileUri {
    private val TAG = "UtilKFileUri>>>>>"

    private val _context = UtilKApplication.instance.get()

    /**
     * 文件转Uri
     * if build sdk > N you also add provider and @xml/file_paths
     * @param filePathWithName String
     * @return Uri
     */
    @JvmStatic
    fun file2Uri(filePathWithName: String): Uri? {
        if (filePathWithName.isEmpty()) {
            Log.e(TAG, "file2Uri: isEmpty true")
            return null
        }
        return file2Uri(File(filePathWithName))
    }

    /**
     * if build sdk > N you also add provider and @xml/file_paths
     * @param file File
     * @return Uri?
     */
    @JvmStatic
    fun file2Uri(file: File): Uri? {
        if (!UtilKFile.isFileExist(file)) {
            Log.e(TAG, "file2Uri: file isFileExist false")
            return null
        }
        return if (Build.VERSION.SDK_INT >= CVersionCode.V_24_7_N)
            FileProvider.getUriForFile(_context, "${_context.packageName}.fileProvider", file).also {
                _context.grantUriPermission(_context.packageName, it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        else Uri.fromFile(file)
    }
}