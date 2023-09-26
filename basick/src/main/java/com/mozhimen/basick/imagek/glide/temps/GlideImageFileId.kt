package com.mozhimen.basick.imagek.glide.temps

/**
 * @ClassName GlideImageFileId
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/9/26 16:16
 * @Version 1.0
 */
class GlideImageFileId(
    var fileId: String
) {

    /**
     * fid对应的url
     */
    var url: String? = null

    /**
     *  需要重写equals和hashCode，用于从缓存中取出ImageFid
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val imageFid = other as GlideImageFileId
        return fileId == imageFid.fileId
    }

    override fun hashCode(): Int {
        return fileId.hashCode()
    }
}