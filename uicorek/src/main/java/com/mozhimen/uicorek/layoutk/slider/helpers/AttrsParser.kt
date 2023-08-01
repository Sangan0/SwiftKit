package com.mozhimen.uicorek.layoutk.slider.helpers

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.mozhimen.basick.utilk.android.content.UtilKRes
import com.mozhimen.basick.utilk.android.util.dp2px
import com.mozhimen.uicorek.R
import com.mozhimen.uicorek.layoutk.slider.mos.MSliderAttrs

/**
 * @ClassName LayoutKSliderParser
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/9/17 14:19
 * @Version 1.0
 */
internal object AttrsParser {

    //const
    val DEFAULT_PADDING_VERTICAL = 4f.dp2px().toInt()
    val SLIDER_WIDTH = 100f.dp2px()
    val SLIDER_HEIGHT = 10f.dp2px()
    val SLIDER_HEIGHT_INSIDE = 16f.dp2px()
    val SLIDER_ROD_LEFT_COLOR = UtilKRes.getColor(R.color.ui_blue_650)
    val SLIDER_ROD_RIGHT_COLOR = UtilKRes.getColor(R.color.ui_gray_200)
    val ROD_SCROLL_ENABLE = true
    val ROD_COLOR = Color.WHITE
    val ROD_COLOR_INSIDE = UtilKRes.getColor(R.color.ui_blue_650)
    val ROD_IS_INSIDE = false
    val ROD_MIN_VAL = 0f
    val ROD_MAX_VAL = 100f
    val ROD_DEFAULT_PERCENT = 0f

    fun parseAttrs(context: Context, attrs: AttributeSet?): MSliderAttrs {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LayoutKSlider)
        val rodIsInside: Boolean =
            typedArray.getBoolean(R.styleable.LayoutKSlider_layoutKSlider_rodIsInside, ROD_IS_INSIDE)
        val sliderHeight: Float =
            typedArray.getDimension(R.styleable.LayoutKSlider_layoutKSlider_sliderHeight, if (!rodIsInside) SLIDER_HEIGHT else SLIDER_HEIGHT_INSIDE)
        val sliderRodLeftColor: Int =
            typedArray.getColor(R.styleable.LayoutKSlider_layoutKSlider_sliderRodLeftColor, SLIDER_ROD_LEFT_COLOR)
        val sliderRodLeftGradientColor: Int =
            typedArray.getColor(R.styleable.LayoutKSlider_layoutKSlider_sliderRodLeftGradientColor, SLIDER_ROD_LEFT_COLOR)
        val sliderRodRightColor: Int =
            typedArray.getColor(R.styleable.LayoutKSlider_layoutKSlider_sliderRodRightColor, SLIDER_ROD_RIGHT_COLOR)
        val rodScrollEnable: Boolean =
            typedArray.getBoolean(R.styleable.LayoutKSlider_layoutKSlider_rodScrollEnable, ROD_SCROLL_ENABLE)
        val rodColor: Int =
            typedArray.getColor(R.styleable.LayoutKSlider_layoutKSlider_rodColor, ROD_COLOR)
        val rodColorInside: Int =
            typedArray.getColor(R.styleable.LayoutKSlider_layoutKSlider_rodColorInside, ROD_COLOR_INSIDE)
        val rodRadius: Float =
            typedArray.getDimension(R.styleable.LayoutKSlider_layoutKSlider_rodRadius, if (!rodIsInside) sliderHeight / 2f * 2f else sliderHeight / 2f * 0.7f)
        val rodRadiusInside: Float =
            typedArray.getDimension(R.styleable.LayoutKSlider_layoutKSlider_rodRadiusInside, if (!rodIsInside) rodRadius * 0.7f else rodRadius * 0.5f)
        val rodMinVal =
            typedArray.getFloat(R.styleable.LayoutKSlider_layoutKSlider_rodMinVal, ROD_MIN_VAL)
        val rodMaxVal =
            typedArray.getFloat(R.styleable.LayoutKSlider_layoutKSlider_rodMaxVal, ROD_MAX_VAL)
        val rodDefaultPercent =
            typedArray.getFloat(R.styleable.LayoutKSlider_layoutKSlider_rodDefaultPercent, ROD_DEFAULT_PERCENT)
        typedArray.recycle()

        return MSliderAttrs(
            sliderHeight,
            sliderRodLeftColor,
            sliderRodLeftGradientColor,
            sliderRodRightColor,
            rodScrollEnable,
            rodColor,
            rodColorInside,
            rodIsInside,
            rodRadius,
            rodRadiusInside,
            rodMinVal,
            rodMaxVal,
            rodDefaultPercent
        )
    }
}