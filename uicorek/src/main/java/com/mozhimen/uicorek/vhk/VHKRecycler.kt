package com.mozhimen.uicorek.vhk

import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mozhimen.basick.elemk.kotlin.cons.CSuppress

/**
 * @ClassName RecyclerKViewHolder
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2021/12/25 15:35
 * @Version 1.0
 */
open class VHKRecycler(val containerView: View) : RecyclerView.ViewHolder(containerView) {

    private var _viewCaches = SparseArray<View>()

    /**
     * 根据资源ID找到View
     * @param viewId Int
     * @return T?
     */
    @Suppress(CSuppress.UNCHECKED_CAST)
    fun <VIEW : View> findViewById(viewId: Int): VIEW? {
        var view = _viewCaches.get(viewId)
        if (view == null) {
            view = itemView.findViewById<VIEW>(viewId)
            _viewCaches.put(viewId, view)
        }
        return view as? VIEW?
    }
}