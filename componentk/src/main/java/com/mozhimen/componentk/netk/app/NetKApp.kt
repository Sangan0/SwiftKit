package com.mozhimen.componentk.netk.app

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.ProcessLifecycleOwner
import com.liulishuo.okdownload.core.exception.ServerCanceledException
import com.mozhimen.basick.elemk.commons.IAB_Listener
import com.mozhimen.basick.elemk.commons.I_Listener
import com.mozhimen.basick.lintk.optin.OptInApiCall_BindLifecycle
import com.mozhimen.basick.lintk.optin.OptInApiInit_ByLazy
import com.mozhimen.basick.lintk.optin.OptInApiInit_InApplication
import com.mozhimen.basick.taskk.executor.TaskKExecutor
import com.mozhimen.basick.taskk.handler.TaskKHandler
import com.mozhimen.basick.utilk.bases.BaseUtilK
import com.mozhimen.basick.utilk.java.io.UtilKFileDir
import com.mozhimen.basick.utilk.java.io.deleteFile
import com.mozhimen.basick.utilk.java.io.deleteFolder
import com.mozhimen.basick.utilk.java.io.file2strMd5
import com.mozhimen.basick.utilk.kotlin.strFilePath2file
import com.mozhimen.componentk.installk.manager.InstallKManager
import com.mozhimen.componentk.netk.app.commons.IAppStateListener
import com.mozhimen.componentk.netk.app.download.commons.IAppDownloadListener
import com.mozhimen.componentk.netk.app.cons.CNetKAppErrorCode
import com.mozhimen.componentk.netk.app.cons.CNetKAppState
import com.mozhimen.componentk.netk.app.cons.ENetKAppFinishType
import com.mozhimen.componentk.netk.app.task.db.AppTaskDaoManager
import com.mozhimen.componentk.netk.app.unzip.AppUnzipManager
import com.mozhimen.componentk.netk.app.task.db.AppTaskDbManager
import com.mozhimen.componentk.netk.app.download.NetKAppDownloadManager
import com.mozhimen.componentk.netk.app.install.NetKAppInstallManager
import com.mozhimen.componentk.netk.app.download.mos.AppDownloadException
import com.mozhimen.componentk.netk.app.install.NetKAppInstallProxy
import com.mozhimen.componentk.netk.app.task.db.AppTask
import com.mozhimen.componentk.netk.app.verify.AppVerifyManager
import java.io.File

/**
 * @ClassName NetKAppDownload
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/10/12 9:38
 * @Version 1.0
 */
@OptInApiInit_InApplication
object NetKApp : IAppStateListener, BaseUtilK() {
    private val _appDownloadStateListeners = mutableListOf<IAppStateListener>()

    @OptIn(OptInApiCall_BindLifecycle::class, OptInApiInit_ByLazy::class)
    private val _netKAppInstallProxy by lazy { NetKAppInstallProxy(_context, ProcessLifecycleOwner.get()) }

    /////////////////////////////////////////////////////////////////
    // init
    /////////////////////////////////////////////////////////////////
    //region # init
    @OptIn(OptInApiCall_BindLifecycle::class, OptInApiInit_ByLazy::class)
    fun init(context: Context) {
        _netKAppInstallProxy.bindLifecycle(ProcessLifecycleOwner.get())// 注册应用安装的监听 InstalledApkReceiver.registerReceiver(this)
        AppTaskDbManager.init(context)
        InstallKManager.init(context)
        NetKAppDownloadManager.init(context)
    }

    fun registerDownloadStateListener(listener: IAppStateListener) {
        if (!_appDownloadStateListeners.contains(listener)) {
            _appDownloadStateListeners.add(listener)
        }
    }

    fun unregisterDownloadListener(listener: IAppStateListener) {
        val indexOf = _appDownloadStateListeners.indexOf(listener)
        if (indexOf >= 0)
            _appDownloadStateListeners.removeAt(indexOf)
    }
    //endregion

    /////////////////////////////////////////////////////////////////
    // control
    /////////////////////////////////////////////////////////////////
    //region
    fun taskStart(appTask: AppTask, listener: IAppDownloadListener? = null) {
        try {
            if (appTask.apkFileSize != 0L) {
                //当前剩余的空间
                val availMemory = UtilKFileDir.External.getFilesRootFreeSpace()
                //需要的最小空间
                val needMinMemory: Long = (appTask.apkFileSize * 1.2).toLong()
                //如果当前需要的空间大于剩余空间，提醒清理空间
                if (availMemory < needMinMemory) {
                    throw AppDownloadException(CNetKAppErrorCode.CODE_TASK_NEED_MEMORY_APK)
                }

                //判断是否为npk,如果是npk,判断空间是否小于需要的2.2倍，如果小于2.2，提示是否继续
                if (appTask.apkName.endsWith(".npk")) {
                    //警告空间
                    val warningsMemory: Long = (appTask.apkFileSize * 2.2).toLong()
                    //如果当前空间小于警告空间，
                    if (availMemory < warningsMemory) {
                        /*                    NiuAlertDialog.Builder(currentActivity)
                                                .setTitle("提示")
                                                .setMessage("存储空间不足，可能会导致安装失败,是否继续下载？")
                                                .setLeftButton("是") { dialog, witch ->
                                                    DownloadManager.download(appTask)
                                                    downloadCallback?.invoke(true)
                                                    dialog.dismiss()
                                                }
                                                .setRightButton("否") { dialog, witch ->
                                                    dialog.dismiss()
                                                }
                                                .show()*/
                        throw AppDownloadException(CNetKAppErrorCode.CODE_TASK_NEED_MEMORY_NPK)
                    }
                }
            }
            createTask2Db(appTask)
            /**
             * [CNetKAppState.STATE_TASK_CREATE]
             */
            onTaskCreate(appTask)
            /**
             * [CNetKAppState.STATE_DOWNLOAD_CREATE]
             */
            onDownloadCreate(appTask)

            NetKAppDownloadManager.download(appTask)
            listener?.onSuccess()
        } catch (e: AppDownloadException) {
            onTaskFail(appTask)
            listener?.onFail(e.code)
        }
    }

    fun taskResume(appTask: AppTask) {
        if (appTask.taskState != CNetKAppState.STATE_TASK_PAUSE) {
            Log.d(TAG, "taskResume: already tasking")
            return
        }
        NetKAppDownloadManager.downloadResume(appTask)
    }

    fun taskCancel(appTask: AppTask, onCancelBlock: IAB_Listener<Boolean, Int>? = null) {
        if (appTask.taskState == CNetKAppState.STATE_TASK_WAIT) {
            NetKAppDownloadManager.downloadWaitCancel(appTask)
        } else {
            if (AppUnzipManager.isUnziping(appTask)) {
                onCancelBlock?.invoke(false, CNetKAppErrorCode.CODE_TASK_CANCEL_FAIL_ON_UNZIPING)
                return
            }
            TaskKExecutor.execute(TAG + "onTaskCancel") {
                NetKAppDownloadManager.downloadCancelOnBack(appTask, onCancelBlock)//从数据库中移除掉
            }
        }
    }

    fun downloadPause(appTask: AppTask) {
        /**
         * [CNetKAppState.STATE_DOWNLOAD_PAUSE]
         */
        onDownloadPause(appTask)
        NetKAppDownloadManager.downloadPause(appTask)
    }

    fun downloadResume(appTask: AppTask) {
        NetKAppDownloadManager.downloadResume(appTask)
    }

    /////////////////////////////////////////////////////////////////
    // state
    /////////////////////////////////////////////////////////////////
    //region # state
    /**
     *  根据游戏id查询下载信息
     */
    fun getAppTaskByDownloadId(downloadId: String): AppTask? {
        return AppTaskDaoManager.getByTaskId(downloadId)
    }

    /**
     * 通过保存名称获取下载信息
     */
    fun getAppTaskByApkSaveName(apkSaveName: String): AppTask? {
        return AppTaskDaoManager.getByApkName(apkSaveName)
    }

    /**
     * 通过包名获取下载信息
     */
    fun getAppTaskByApkPackageName(apkPackageName: String): AppTask? {
        return AppTaskDaoManager.getByApkPackageName(apkPackageName)
    }

    /**
     * 是否有正在下载的任务
     */
    fun hasDownloading(): Boolean {
        return AppTaskDaoManager.hasDownloading()
    }

    /**
     * 是否有正在校验的任务
     */
    fun hasVerifying(): Boolean {
        return AppTaskDaoManager.hasVerifying()
    }

    /**
     * 是否有正在解压的任务
     */
    fun hasUnziping(): Boolean {
        return AppTaskDaoManager.hasUnziping()
    }

    /**
     * 判断是否正在下载中
     * @return true 正在下载中  false 当前不是正在下载中
     */
    fun isDownloading(appTask: AppTask): Boolean {
        return NetKAppDownloadManager.getAppDownloadProgress(appTask).isDownloading()//查询下载状态
    }
    //endregion

    /////////////////////////////////////////////////////////////////

    override fun onTaskCreate(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_TASK_CREATE)
    }

    override fun onTaskWait(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_TASK_WAIT)
    }

    override fun onTasking(appTask: AppTask, state: Int) {
        TODO("Not yet implemented")
    }

    override fun onTaskPause(appTask: AppTask) {
        TODO("Not yet implemented")
    }

    override fun onTaskFinish(appTask: AppTask, finishType: ENetKAppFinishType) {
        TODO("Not yet implemented")
    }

    override fun onTaskWaitCancel(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_TASK_WAIT_CANCEL)
    }

    override fun onTaskCancel(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_TASK_CANCEL, nextMethod = {
            onTaskCreate(appTask)
        })
    }

    override fun onTaskSuccess(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_TASK_SUCCESS)
    }

    override fun onTaskFail(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_TASK_FAIL)
    }

    /////////////////////////////////////////////////////////////////

    override fun onDownloadCreate(appTask: AppTask) {
        /*        //将结果传递给服务端
                GlobalScope.launch(Dispatchers.IO) {
                    if (appTask.appId == "2") {
                        ApplicationService.updateAppDownload("1")
                    } else {
                        ApplicationService.updateAppDownload(appTask.appId)
                    }
                }*/
        applyAppTaskState(appTask, CNetKAppState.STATE_DOWNLOAD_CREATE)
    }

    override fun onDownloadWait(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_DOWNLOAD_WAIT, 0, nextMethod = {
            /**
             * [CNetKAppState.STATE_TASK_WAIT]
             */
            onTaskWait(appTask)
        })
    }

    override fun onDownloading(appTask: AppTask, progress: Int) {
        applyAppTaskState(appTask, CNetKAppState.STATE_DOWNLOADING, progress)
    }

    override fun onDownloadPause(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_DOWNLOAD_PAUSE)
    }

    override fun onDownloadCancel(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_DOWNLOAD_CANCEL, nextMethod = {
            onTaskCancel(appTask)
        })
    }

    override fun onDownloadSuccess(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_DOWNLOAD_SUCCESS, nextMethod = {
            verifyCreate(appTask)//下载完成，去安装
        })
    }

    fun onDownloadFail(appTask: AppTask, exception: Exception?) {
        if (exception is ServerCanceledException) {
            if (exception.responseCode == 404 && appTask.downloadUrlCurrent != appTask.downloadUrl && appTask.downloadUrl.isNotEmpty()) {
                appTask.downloadUrlCurrent = appTask.downloadUrl
                taskStart(appTask)
            } else {
                /**
                 * [CNetKAppState.STATE_DOWNLOAD_FAIL]
                 */
                onDownloadFail(appTask)
            }
        } else {
            /**
             * [CNetKAppState.STATE_DOWNLOAD_FAIL]
             */
            onDownloadFail(appTask)
        }
    }

    override fun onDownloadFail(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_DOWNLOAD_FAIL, nextMethod = {
            /**
             * [CNetKAppState.STATE_TASK_FAIL]
             */
            onTaskFail(appTask)
        })
    }

    /////////////////////////////////////////////////////////////////

    override fun onVerifyCreate(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_VERIFY_CREATE)
    }

    override fun onVerifying(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_VERIFYING)
    }

    override fun onVerifySuccess(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_VERIFY_SUCCESS)
    }

    override fun onVerifyFail(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_VERIFY_FAIL, nextMethod = {
            /**
             * [CNetKAppState.STATE_TASK_FAIL]
             */
            onTaskFail(appTask)
        })
    }

    /////////////////////////////////////////////////////////////////

    override fun onUnzipCreate(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_UNZIP_CREATE, nextMethod = {
            /**
             * [CNetKAppState.STATE_UNZIPING]
             */
            onUnziping(appTask)
        })
    }

    override fun onUnziping(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_UNZIPING, nextMethod = {
            TaskKExecutor.execute(TAG + "onUnziping") {
                val strPathNameUnzip = AppUnzipManager.unzip(appTask)
                if (strPathNameUnzip.isEmpty()) return@execute//正在解压
                /**
                 * [CNetKAppState.STATE_UNZIP_SUCCESS]
                 */
                onUnzipSuccess(appTask)
            }
        })
    }

    override fun onUnzipSuccess(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_UNZIP_SUCCESS, nextMethod = {
            /**
             * [CNetKAppState.STATE_INSTALL_CREATE]
             */
            onInstallCreate(appTask)//调用安装的回调
        })
    }

    override fun onUnzipFail(appTask: AppTask) {
        //            AlertTools.showToast("解压失败，请检测存储空间是否足够！")
        applyAppTaskState(appTask, CNetKAppState.STATE_UNZIP_FAIL, nextMethod = {
            /**
             * [CNetKAppState.STATE_TASK_FAIL]
             */
            onTaskFail(appTask)
        })
    }

    /////////////////////////////////////////////////////////////////

    override fun onInstallCreate(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_INSTALL_CREATE, nextMethod = {
            /**
             * [CNetKAppState.STATE_INSTALLING]
             */
            onInstalling(appTask)
        })
    }

    override fun onInstalling(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_INSTALLING, nextMethod = {
            installApkOnMain(appTask, appTask.apkPathName.strFilePath2file())
        })
    }

    override fun onInstallSuccess(appTask: AppTask) {
        if (appTask.apkPackageName.isEmpty()) return
        TaskKExecutor.execute(TAG + "onInstallSuccess") {
            val appTask1 = AppTaskDaoManager.getByApkPackageName(appTask.apkPackageName) ?: return@execute//从本地数据库中查询出下载信息//如果查询不到，就不处理
            if (appTask1.apkIsInstalled)//删除数据库中的其他已安装的数据，相同包名的只保留一条已安装的数据
                AppTaskDaoManager.deleteOnBack(appTask1)
            //将安装状态发给后端
            /*            GlobalScope.launch(Dispatchers.IO) {
                            ApplicationService.install(appDownloadParam0.appId)
                        }*/
            //将安装状态更新到数据库中
            applyAppTaskStateOnBack(appTask1.apply { apkIsInstalled = true }, CNetKAppState.STATE_INSTALL_SUCCESS, 0, nextMethod = {
                onTaskSuccess(appTask1)
            })//更新安装的状态为1 q })
            /*            //TODO 如果设置自动删除安装包，安装成功后删除安装包
                        if (AutoDeleteApkSettingHelper.isAutoDelete()) {
                            if (deleteApkFile(appDownloadParam0)) {
                                HandlerHelper.post {
                                    AlertTools.showToast("文件已经删除！")
                                }
                            }
                        }*/
        }
    }

    override fun onInstallFail(appTask: AppTask) {
        applyAppTaskState(appTask, CNetKAppState.STATE_INSTALL_FAIL, nextMethod = {
            /**
             * [CNetKAppState.STATE_TASK_FAIL]
             */
            onTaskFail(appTask)
        })
    }

    /////////////////////////////////////////////////////////////////

    override fun onUninstallSuccess(appTask: AppTask) {
        if (appTask.apkPackageName.isEmpty()) return
        val appTask = AppTaskDaoManager.getByApkPackageName(appTask.apkPackageName) ?: return
        applyAppTaskState(appTask.apply { apkIsInstalled = false }, CNetKAppState.STATE_UNINSTALL_CREATE, 0)//设置为未安装
    }

    /////////////////////////////////////////////////////////////////

    private fun applyAppTaskState(appTask: AppTask, state: Int, progress: Int = -1, nextMethod: I_Listener? = null) {
        AppTaskDaoManager.update(appTask.apply {
            taskState = state
            if (progress > -1) downloadProgress = progress
        })
        postAppTaskState(appTask, state, progress, nextMethod)
    }

    @WorkerThread
    private fun applyAppTaskStateOnBack(appTask: AppTask, state: Int, progress: Int = -1, nextMethod: I_Listener? = null) {
        AppTaskDaoManager.updateOnBack(appTask.apply {
            taskState = state
            if (progress > -1) downloadProgress = progress
        })
        postAppTaskState(appTask, state, progress, nextMethod)
    }

    private fun postAppTaskState(appTask: AppTask, state: Int, progress: Int = -1, nextMethod: I_Listener? = null) {
        TaskKHandler.post {
            for (listener in _appDownloadStateListeners) {
                when (state) {
                    CNetKAppState.STATE_TASK_CREATE -> listener.onTaskCreate(appTask)
                    CNetKAppState.STATE_TASK_WAIT -> listener.onTaskWait(appTask)
                    CNetKAppState.STATE_TASK_WAIT_CANCEL -> listener.onTaskWaitCancel(appTask)
                    CNetKAppState.STATE_TASK_CANCEL -> listener.onTaskCancel(appTask)
                    CNetKAppState.STATE_TASK_SUCCESS -> listener.onTaskSuccess(appTask)
                    CNetKAppState.STATE_TASK_FAIL -> listener.onTaskFail(appTask)
                    ///////////////////////////////////////////////////////////////////////////////
                    CNetKAppState.STATE_DOWNLOAD_CREATE -> listener.onDownloadCreate(appTask)
                    CNetKAppState.STATE_DOWNLOADING -> listener.onDownloading(appTask, progress)
                    CNetKAppState.STATE_DOWNLOAD_PAUSE -> listener.onDownloadPause(appTask)
                    CNetKAppState.STATE_DOWNLOAD_CANCEL -> listener.onDownloadCancel(appTask)
                    CNetKAppState.STATE_DOWNLOAD_SUCCESS -> listener.onDownloadSuccess(appTask)
                    CNetKAppState.STATE_DOWNLOAD_FAIL -> listener.onDownloadFail(appTask)
                    ///////////////////////////////////////////////////////////////////////////////
                    CNetKAppState.STATE_VERIFY_CREATE -> listener.onVerifyCreate(appTask)
                    CNetKAppState.STATE_VERIFYING -> listener.onVerifying(appTask)
                    CNetKAppState.STATE_VERIFY_SUCCESS -> listener.onVerifySuccess(appTask)
                    CNetKAppState.STATE_VERIFY_FAIL -> listener.onVerifyFail(appTask)
                    ///////////////////////////////////////////////////////////////////////////////
                    CNetKAppState.STATE_UNZIP_CREATE -> listener.onUnzipCreate(appTask)
                    CNetKAppState.STATE_UNZIPING -> listener.onUnziping(appTask)
                    CNetKAppState.STATE_UNZIP_SUCCESS -> listener.onUnzipSuccess(appTask)
                    CNetKAppState.STATE_UNZIP_FAIL -> listener.onUnzipFail(appTask)
                    ///////////////////////////////////////////////////////////////////////////////
                    CNetKAppState.STATE_INSTALL_CREATE -> listener.onInstallCreate(appTask)
                    CNetKAppState.STATE_INSTALLING -> listener.onInstalling(appTask)
                    CNetKAppState.STATE_INSTALL_SUCCESS -> listener.onInstallSuccess(appTask)
                    CNetKAppState.STATE_INSTALL_FAIL -> listener.onInstallFail(appTask)
                    ///////////////////////////////////////////////////////////////////////////////
                    CNetKAppState.STATE_UNINSTALL_SUCCESS -> listener.onUninstallSuccess(appTask)
                }
            }
            nextMethod?.invoke()
        }
    }

    private fun createTask2Db(appTask: AppTask) {
        val downloadId = AppTaskDaoManager.getByTaskId(appTask.taskId)//更新本地数据库中的数据
        if (downloadId == null) {
            AppTaskDaoManager.addAll(appTask)
        }
    }

    private fun verifyCreate(appTask: AppTask) {
        if (appTask.apkName.endsWith(".npk"))//如果文件以.npk结尾则先解压
            verifyAndUnzipNpk(appTask)
        else
            verifyApk(appTask)
    }

    /**
     * 安装.npk文件
     */
    private fun verifyAndUnzipNpk(appTask: AppTask) {
        if (AppUnzipManager.isUnziping(appTask)) return//正在解压中，不进行操作

        /**
         * [CNetKAppState.STATE_VERIFYING]
         */
        onVerifying(appTask)
        val externalFilesDir = UtilKFileDir.External.getFilesDownloadsDir()
        if (externalFilesDir == null) {
            /**
             * [CNetKAppState.STATE_VERIFY_FAIL]
             */
            onVerifyFail(appTask)
            return
        }
        val fileApk = File(externalFilesDir, appTask.apkName)
        if (!fileApk.exists()) {
            /**
             * [CNetKAppState.STATE_VERIFY_FAIL]
             */
            onVerifyFail(appTask)
            return
        }

        if (AppVerifyManager.isNeedVerify(appTask)) {
            val apkFileMd5Remote = appTask.apkFileMd5
            if (apkFileMd5Remote.isNotEmpty() && "NONE" != apkFileMd5Remote) {
                val apkFileMd5Locale = fileApk.file2strMd5()//取文件的MD5值
                if (!TextUtils.equals(apkFileMd5Remote, apkFileMd5Locale)) {
                    /**
                     * [CNetKAppState.STATE_VERIFY_FAIL]
                     */
                    onVerifyFail(appTask)
                    return
                }
            }
        }
        /**
         * [CNetKAppState.STATE_VERIFY_SUCCESS]
         */
        onVerifySuccess(appTask)
        /**
         * [CNetKAppState.STATE_UNZIP_CREATE]
         */
        onUnzipCreate(appTask.apply {
            apkPathName = fileApk.absolutePath
        })
    }

    /**
     * 安装apk文件
     */
    private fun verifyApk(appTask: AppTask) {
        if (appTask.apkFileMd5.isEmpty() || "NONE" == appTask.apkFileMd5) {//如果文件没有MD5值或者为空，则不校验 直接去安装
            /**
             * [CNetKAppState.STATE_INSTALL_CREATE]
             */
            onInstallCreate(appTask.apply {
                apkPathName = File(UtilKFileDir.External.getFilesDownloadsDir() ?: return, appTask.apkName).absolutePath
            })
            return
        }
        /**
         * [CNetKAppState.STATE_VERIFYING]
         */
        onVerifying(appTask)
        val externalFilesDir = UtilKFileDir.External.getFilesDownloadsDir() ?: kotlin.run {
            /**
             * [CNetKAppState.STATE_VERIFY_FAIL]
             */
            onVerifyFail(appTask)
            return
        }
        val fileApk = File(externalFilesDir, appTask.apkName)
        if (!fileApk.exists()) {
            /**
             * [CNetKAppState.STATE_VERIFY_FAIL]
             */
            onVerifyFail(appTask)
            return
        }

        if (AppVerifyManager.isNeedVerify(appTask)) {//判断是否需要校验MD5值
            val apkFileMd5Remote = appTask.apkFileMd5//如果本地文件存在，且MD5值相等
            if (apkFileMd5Remote.isNotEmpty()) {
                val apkFileMd5Locale = (fileApk.file2strMd5() ?: "") /*+ "1"*///取文件的MD5值
                if (!TextUtils.equals(apkFileMd5Remote, apkFileMd5Locale)) {
                    /**
                     * [CNetKAppState.STATE_VERIFY_FAIL]
                     */
                    onVerifyFail(appTask)

                    fileApk.deleteFile()//删除本地文件
                    if (appTask.downloadUrlCurrent != appTask.downloadUrl) {//重新使用内部地址下载
                        if (appTask.downloadUrl.isNotEmpty()) {
                            appTask.downloadUrlCurrent = appTask.downloadUrl
                            taskStart(appTask)
                        } else {
                            appTask.apkVerifyNeed = false//重新下载，下次不校验MD5值
                            taskStart(appTask)
                        }
                    }
                    return
                }
            }
        }
        /**
         * [CNetKAppState.STATE_VERIFY_SUCCESS]
         */
        onVerifySuccess(appTask)//检测通过，去安装
        /**
         * [CNetKAppState.STATE_INSTALL_CREATE]
         */
        onInstallCreate(appTask.apply {
            apkPathName = fileApk.absolutePath
        })//调用安装的回调
    }

    @OptIn(OptInApiCall_BindLifecycle::class, OptInApiInit_ByLazy::class)
    private fun installApkOnMain(appTask: AppTask, fileApk: File) {
        TaskKHandler.post {
            _netKAppInstallProxy.setAppTask(appTask)
            NetKAppInstallManager.installApk(fileApk)
        }
    }

//    /**
//     * 获取本地保存的文件
//     */
//    private fun getApkSavePathName(appTask: AppTask): File? {
//        val externalFilesDir = UtilKFileDir.External.getFilesDownloadsDir() ?: return null
//        return File(externalFilesDir, appTask.apkName)
//    }

    /**
     * 删除Apk文件
     */
    private fun deleteFileApk(appTask: AppTask): Boolean {
        val externalFilesDir = UtilKFileDir.External.getFilesDownloadsDir() ?: return true
        File(externalFilesDir, appTask.apkName).deleteFile()
        if (appTask.apkName.endsWith(".npk")) {//如果是npk,删除解压的文件夹
            File(externalFilesDir, appTask.apkName.split(".npk")[0]).deleteFolder()
        }
        return true
    }
}