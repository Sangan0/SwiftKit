package com.mozhimen.basicsk.basek

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.mozhimen.basicsk.basek.commons.IBaseKLayout

/**
 * @ClassName ILayout
 * @Description TODO
 * @Author Kolin Zhao / Mozhimen
 * @Date 2021/12/24 14:39
 * @Version 1.0
 */
abstract class BaseKLayoutLinear @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attrs, defStyleAttr), IBaseKLayout {

    var TAG = "${this.javaClass.simpleName}>>>>>"

    override fun initAttrs(attrs: AttributeSet?, defStyleAttr: Int) {}
    override fun initView() {}
}