package com.mozhimen.basick.utilk.bitmap

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.mozhimen.basick.utilk.UtilKDate
import com.mozhimen.basick.utilk.file.UtilKFile
import com.mozhimen.basick.utilk.context.UtilKApplication
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * @ClassName UtilKBitmapIO
 * @Description Bitmap IO 类
 * @Author Mozhimen & Kolin Zhao
 * @Date 2022/10/20 16:44
 * @Version 1.0
 */
object UtilKBitmapIO {
    private val TAG = "UtilKBitmapIO>>>>>"
    private val _context = UtilKApplication.instance.get()

    /**
     * 位图转文件
     * @param sourceBitmap Bitmap
     * @param filePathWithName String
     * @return File
     */
    @JvmStatic
    fun bitmap2Album(sourceBitmap: Bitmap, filePathWithName: String, quality: Int = 100, compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            bitmap2AlnumAfter29(sourceBitmap, filePathWithName, quality, compressFormat)
        } else {
            bitmap2AlbumBefore29(sourceBitmap, filePathWithName, quality, compressFormat)
        }
    }

    /**
     * 存相册 after 29
     * @param sourceBitmap Bitmap
     * @param filePathWithName String
     * @param quality Int
     * @param compressFormat CompressFormat
     * @return String
     */
    fun bitmap2AlnumAfter29(sourceBitmap: Bitmap, filePathWithName: String, quality: Int = 100, compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): String {
        var outputStream: OutputStream? = null
        val destFile = UtilKFile.createFile(filePathWithName)
        val pathArray: Array<String> = arrayOf(destFile.absolutePath)
        val typeArray: Array<String> = arrayOf("image/jpeg")
        try {
            val contentValues = ContentValues()
            val contentResolver: ContentResolver = _context.contentResolver
            contentValues.put(MediaStore.Images.ImageColumns.DATA, destFile.absolutePath)
            contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, filePathWithName.split("/").lastOrNull() ?: UtilKDate.getNowString())
            contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg")
            contentValues.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis().toString())
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) // 插入相册
            uri?.let {
                outputStream = contentResolver.openOutputStream(uri)
                sourceBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }
            return filePathWithName
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.flush()
            outputStream?.close()
            try {
                MediaScannerConnection.scanFile(_context, pathArray, typeArray) { path, uri -> Log.d(TAG, "bitmap2Album: path $path, uri $uri") }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return UtilKFile.MSG_WRONG
    }

    /**
     * 保存图片 before 29
     * @param sourceBitmap Bitmap
     * @param filePathWithName String
     * @param quality Int
     * @param compressFormat CompressFormat
     * @return String
     */
    @JvmStatic
    fun bitmap2AlbumBefore29(sourceBitmap: Bitmap, filePathWithName: String, quality: Int = 100, compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): String =
        bitmap2AlbumBefore29(sourceBitmap, File(filePathWithName), quality, compressFormat)

    /**
     * 保存图片 before 29
     * @param destFile String
     * @param sourceBitmap Bitmap?
     */
    @JvmStatic
    fun bitmap2AlbumBefore29(sourceBitmap: Bitmap, destFile: File, quality: Int = 80, compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): String {
        UtilKFile.createFile(destFile)
        var bufferedOutputStream: BufferedOutputStream? = null
        try {
            bufferedOutputStream = BufferedOutputStream(FileOutputStream(destFile))
            sourceBitmap.compress(compressFormat, quality, bufferedOutputStream)
            return destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bufferedOutputStream?.flush()
            bufferedOutputStream?.close()
        }
        return UtilKFile.MSG_WRONG
    }

    /**
     * 从相册获得图片
     * @param uri Uri
     * @return Bitmap?
     */
    @JvmStatic
    fun album2Bitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(_context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(_context.contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 删除图片
     * @param deleteFilePath String
     */
    @JvmStatic
    fun deleteBitmapFromAlbum(deleteFilePath: String) {
        _context.contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "${MediaStore.Images.Media.DATA}='${deleteFilePath}'", null)
    }
}