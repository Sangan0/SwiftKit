package com.mozhimen.xmlk.bases

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.mozhimen.xmlk.commons.ILayoutK

/**
 * @ClassName LayoutKFrame
 * @Description TODO
 * @Author Kolin Zhao / Mozhimen
 * @Version 1.0
 */
abstract class BaseLayoutKFrame @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr), ILayoutK {
}