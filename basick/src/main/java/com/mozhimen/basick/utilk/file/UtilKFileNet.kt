package com.mozhimen.basick.utilk.file

import android.Manifest
import com.mozhimen.basick.permissionk.annors.APermissionK
import com.mozhimen.basick.utilk.net.UtilKNetDeal
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * @ClassName UtilKDownload
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2023/1/7 23:37
 * @Version 1.0
 */
object UtilKFileNet {
    private const val TAG = "UtilKDownload>>>>>"

    @APermissionK(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET)
    fun downLoadFile(httpUrl: String, saveFileNameWithName: String): File {
        require(httpUrl.isNotEmpty()) { "$TAG httpUrl must be not empty" }

        val file = File(saveFileNameWithName)
        if (UtilKFile.isFileExist(file)) UtilKFile.deleteFile(file)

        var inputStream: InputStream? = null
        var fileOutputStream: FileOutputStream? = null
        var httpURLConnection: HttpURLConnection? = null

        try {
            val url = URL(httpUrl)
            httpURLConnection = url.openConnection() as HttpURLConnection
            if (httpURLConnection is HttpsURLConnection) {
                val sslContext = UtilKNetDeal.getSLLContext()
                if (sslContext != null) {
                    val sslSocketFactory = sslContext.socketFactory
                    httpURLConnection.sslSocketFactory = sslSocketFactory
                }
            }
            httpURLConnection.connectTimeout = 60 * 1000
            httpURLConnection.readTimeout = 60 * 1000
            httpURLConnection.connect()
            inputStream = httpURLConnection.inputStream
            fileOutputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } > 0) {
                fileOutputStream.write(buffer, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            fileOutputStream?.flush()
            fileOutputStream?.close()
            httpURLConnection?.disconnect()
        }
        return file
    }
}