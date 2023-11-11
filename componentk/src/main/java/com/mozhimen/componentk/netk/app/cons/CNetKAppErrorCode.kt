package com.mozhimen.componentk.netk.app.cons

/**
 * @ClassName CAppDownloadErrorCode
 * @Description TODO
 * @Author Mozhimen
 * @Date 2023/11/7 13:58
 * @Version 1.0
 */
object CNetKAppErrorCode {
    const val CODE_TASK_NEED_MEMORY_APK = 0//"存储空间不足，请清理内存后再试"
    const val CODE_TASK_NEED_MEMORY_NPK = 1//"存储空间不足，可能会导致安装失败,是否继续下载？"
    const val CODE_TASK_CANCEL_FAIL_ON_UNZIPING = 2//正在解压, 无法删除
    const val CODE_DOWNLOAD_PATH_NOT_EXIST =10//"下载路径不存在"
    const val CODE_DOWNLOAD_CANT_FIND_TASK = 11//"未找到下载任务！"
}