package com.mozhimen.componentk.netk.app.download

import android.content.Context
import android.util.Log
import android.util.SparseArray
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection
import com.liulishuo.okdownload.core.listener.DownloadListener1
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import com.mozhimen.basick.elemk.javax.net.bases.BaseX509TrustManager
import com.mozhimen.basick.lintk.optin.OptInApiInit_InApplication
import com.mozhimen.basick.utilk.bases.IUtilK
import com.mozhimen.basick.utilk.java.io.UtilKFileDir
import com.mozhimen.basick.utilk.javax.net.UtilKSSLSocketFactory
import com.mozhimen.componentk.netk.app.NetKApp
import com.mozhimen.componentk.netk.app.cons.CNetKAppErrorCode
import com.mozhimen.componentk.netk.app.cons.CNetKAppState
import com.mozhimen.componentk.netk.app.download.helpers.AppDownloadSerialQueue
import com.mozhimen.componentk.netk.app.download.mos.AppDownloadException
import com.mozhimen.componentk.netk.app.download.mos.MAppDownloadProgress
import com.mozhimen.componentk.netk.app.task.cons.CNetKAppTaskState
import com.mozhimen.componentk.netk.app.task.db.AppTask
import com.mozhimen.componentk.netk.app.task.db.AppTaskDaoManager
import com.mozhimen.componentk.netk.app.verify.NetKAppVerifyManager
import okhttp3.OkHttpClient
import java.lang.Exception

/**
 * @ClassName AppDownloadManager
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 14:19
 * @Version 1.0
 */
@OptInApiInit_InApplication
object NetKAppDownloadManager : DownloadListener1(), IUtilK {
    private val _downloadingTasks = SparseArray<AppTask>()
    private val _appDownloadSerialQueue: AppDownloadSerialQueue by lazy { AppDownloadSerialQueue(this) }

    ///////////////////////////////////////////////////////////////////////////////////////

    @JvmStatic
    fun init(context: Context) {
        try {
            val builder = OkDownload.Builder(context)
                .connectionFactory(
                    DownloadOkHttp3Connection.Factory().setBuilder(
                        OkHttpClient.Builder()
                            .sslSocketFactory(UtilKSSLSocketFactory.getTLS(), BaseX509TrustManager())
                            .hostnameVerifier { _, _ -> true }
                    )
                )
            OkDownload.setSingletonInstance(builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    @JvmStatic
    fun download(appTask: AppTask) {
        val externalFilesDir = UtilKFileDir.External.getFilesDownloadsDir() ?: throw AppDownloadException(CNetKAppErrorCode.CODE_DOWNLOAD_PATH_NOT_EXIST)
        val downloadTask = DownloadTask.Builder(appTask.downloadUrlCurrent, externalFilesDir)//先构建一个Task 框架可以保证Id唯一
            .setFilename(appTask.apkName)
            .setMinIntervalMillisCallbackProcess(1000)// 下载进度回调的间隔时间（毫秒）
            .setPassIfAlreadyCompleted(!appTask.apkIsInstalled)// 任务过去已完成是否要重新下载
            .build()
        //先根据Id去查找当前队列中有没有相同的任务，
        //如果有相同的任务，则不进行提交
        val appTask1 = _downloadingTasks[downloadTask.id]
        if (appTask1 != null) {
            Log.d(TAG, "download: the task is downloading")
            return
        }
        when (StatusUtil.getStatus(downloadTask)) {
            StatusUtil.Status.PENDING -> {
                //等待中 不做处理
            }

            StatusUtil.Status.RUNNING -> {
                //下载中，不做处理
            }

            StatusUtil.Status.COMPLETED -> {
                onDownloadSuccess(appTask)
                return
            }

            StatusUtil.Status.IDLE -> {

            }
            //StatusUtil.Status.UNKNOWN
            else -> {

            }
        }

        _downloadingTasks.put(downloadTask.id, appTask)
        _appDownloadSerialQueue.enqueue(downloadTask)

        /**
         * [CNetKAppState.STATE_DOWNLOAD_WAIT]
         */
        NetKApp.onDownloadWait(appTask)
//        listener?.onSuccess()
    }

    fun downloadPause(appTask: AppTask) {
        val task = getDownloadTask(appTask) ?: run {
            Log.d(TAG, "downloadPause: get download task fail")
            return
        }
        task.cancel()//取消任务

        /**
         * [CNetKAppState.STATE_DOWNLOAD_PAUSE]
         */
        NetKApp.onDownloadPause(appTask)
    }

    /**
     * 恢复任务
     */
    fun downloadResume(appTask: AppTask) {
        val task = getDownloadTask(appTask) ?: run {
            Log.d(TAG, "downloadPause: get download task fail")
            return
        }
        if (StatusUtil.getStatus(task) != StatusUtil.Status.RUNNING) {
            _appDownloadSerialQueue.enqueue(task)
        }
        _downloadingTasks.put(task.id, appTask)

        /**
         * [CNetKAppState.STATE_DOWNLOADING]
         */
        NetKApp.onDownloading(appTask, appTask.downloadProgress)
    }

    /**
     * 任务取消等待
     */
    fun downloadWaitCancel(appTask: AppTask/*, onDeleteBlock: IAB_Listener<Boolean, Int>?*/) {
        val task = getDownloadTask(appTask) ?: run {
//            TaskKHandler.post {
//                onDeleteBlock?.invoke(false, CNetKAppErrorCode.CODE_DOWNLOAD_CANT_FIND_TASK)
//            }
            Log.d(TAG, "downloadPause: get download task fail")
            return
        }
        _appDownloadSerialQueue.remove(task)//先从队列中移除
        task.cancel()//然后取消任务

//        /**
//         * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
//         */
//        NetKApp.onDownloadCancel(appTask)
    }

    /**
     * 删除任务
     */
    fun downloadCancel(appTask: AppTask/*, onDeleteBlock: IAB_Listener<Boolean, Int>?*/) {
        val task = getDownloadTask(appTask) ?: run {
//            TaskKHandler.post {
//                onDeleteBlock?.invoke(false, CNetKAppErrorCode.CODE_DOWNLOAD_CANT_FIND_TASK)
//            }
            Log.d(TAG, "downloadPause: get download task fail")
            return
        }
        task.cancel()
        _downloadingTasks.delete(task.id)//先从队列中移除
        _appDownloadSerialQueue.remove(task)
        OkDownload.with().breakpointStore().remove(task.id)
        task.file?.delete()
        AppTaskDaoManager.delete(appTask)
//            onDeleteBlock?.invoke(true, -1)
//        /**
//         * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
//         */
//        NetKApp.onDownloadCancel(appTask)
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * 查询下载状态
     */
    fun getAppDownloadProgress(appTask: AppTask): MAppDownloadProgress {
        val task = getDownloadTask(appTask) ?: run {
            Log.d(TAG, "downloadPause: get download task fail")
            return MAppDownloadProgress()
        }
        return when (StatusUtil.getStatus(task)) {
            StatusUtil.Status.PENDING -> {//等待中 不做处理
                MAppDownloadProgress().apply {
                    progressState = CNetKAppState.STATE_DOWNLOAD_WAIT
                }
            }

            StatusUtil.Status.RUNNING -> {//下载中，不做处理
                MAppDownloadProgress().apply {
                    progressState = CNetKAppState.STATE_DOWNLOADING
                    progress = StatusUtil.getCurrentInfo(task)?.let {
                        (it.totalOffset.toFloat() / it.totalLength * 100).toInt()
                    } ?: 0
                }
            }

            StatusUtil.Status.COMPLETED -> {//下载完成，去安装
                MAppDownloadProgress().apply {
                    progressState = CNetKAppState.STATE_DOWNLOAD_SUCCESS
                    progress = 100
                }
            }

            StatusUtil.Status.IDLE -> {
                return MAppDownloadProgress().apply {
                    progressState = CNetKAppState.STATE_DOWNLOAD_PAUSE
                    progress = StatusUtil.getCurrentInfo(task)?.let {
                        (it.totalOffset.toFloat() / it.totalLength * 100).toInt()
                    } ?: 0
                }
            }
            //StatusUtil.Status.UNKNOWN
            else -> {
                return MAppDownloadProgress().apply {
                    progressState = CNetKAppTaskState.STATE_TASKING
                    progress = StatusUtil.getCurrentInfo(task)?.let {
                        (it.totalOffset.toFloat() / it.totalLength * 100).toInt()
                    } ?: 0
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    override fun taskStart(task: DownloadTask, model: Listener1Assist.Listener1Model) {
        Log.d(TAG, "taskStart: task $task")
        _downloadingTasks[task.id]?.let { appTask ->
            /**
             * [CNetKAppState.STATE_DOWNLOADING]
             */
            NetKApp.onDownloading(appTask, 0)
        }
    }

    override fun retry(task: DownloadTask, cause: ResumeFailedCause) {
        Log.d(TAG, "retry: task $task")
    }

    override fun connected(task: DownloadTask, blockCount: Int, currentOffset: Long, totalLength: Long) {
        Log.d(TAG, "connected: task $task")
    }

    override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
        Log.d(TAG, "progress: task $task currentOffset $currentOffset  totalLength $totalLength")
        _downloadingTasks[task.id]?.let { appTask ->
            val progress = (currentOffset.toFloat() / totalLength * 100).toInt()
            /**
             * [CNetKAppState.STATE_DOWNLOADING]
             */
            NetKApp.onDownloading(appTask, progress)
        }
    }

    override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?, model: Listener1Assist.Listener1Model) {
        Log.d(TAG, "taskEnd: $task cause ${cause.name} realCause ${realCause.toString()}")
        _downloadingTasks[task.id]?.let { appTask ->
            when (cause) {
                EndCause.COMPLETED -> {
                    onDownloadSuccess(appTask)
                }

                EndCause.CANCELED -> {
                    /**
                     * [CNetKAppState.STATE_DOWNLOAD_CANCEL]
                     */
                    NetKApp.onDownloadCancel(appTask)//下载取消
                }

                else -> {
                    /**
                     * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                     */
                    NetKApp.onDownloadFail(appTask, realCause)
                }
            }
        }
        _downloadingTasks.delete(task.id)//从队列里移除掉
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private fun onDownloadSuccess(appTask: AppTask) {
        /**
         * [CNetKAppState.STATE_DOWNLOAD_SUCCESS]
         */
        NetKApp.onDownloadSuccess(appTask)//下载完成，去安装

        NetKAppVerifyManager.verify(appTask)//下载完成，去安装
    }

    private fun getDownloadTask(appTask: AppTask): DownloadTask? {
        val externalFilesDir = UtilKFileDir.External.getFilesDownloadsDir() ?: run {
            Log.d(TAG, "getDownloadTask: get download dir fail")
            return null
        }
        return DownloadTask.Builder(appTask.downloadUrlCurrent, externalFilesDir.absolutePath, appTask.apkName).build()
    }
}