package com.mozhimen.basick.utilk.androidx.databinding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.mozhimen.basick.elemk.kotlin.cons.CSuppress
import com.mozhimen.basick.utilk.java.lang.UtilKReflectGenericKotlin
import com.mozhimen.basick.utilk.kotlin.printlog
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.Exception

/**
 * @ClassName UtilKViewDataBinding
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/10/22 22:19
 * @Version 1.0
 */
object UtilKViewDataBinding {
    @JvmStatic
    @Suppress(CSuppress.UNCHECKED_CAST)
    fun <VB : ViewDataBinding> get(clazz: Class<*>, inflater: LayoutInflater/*, index: Int = 0*/): VB =
        getViewDataBindingGenericTypeClazz(clazz)?.run {
            getDeclaredMethod("inflate", LayoutInflater::class.java).invoke(null, inflater) as VB
        } ?: throw Exception("inflate activity vb fail!")

    @JvmStatic
    @Suppress(CSuppress.UNCHECKED_CAST)
    fun <VB : ViewDataBinding> get(clazz: Class<*>, inflater: LayoutInflater, container: ViewGroup?/*, index: Int = 0*/): VB =
        getViewDataBindingGenericTypeClazz(clazz)?.run {
            getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java).invoke(null, inflater, container, false) as VB
        } ?: throw Exception("inflate fragment vb fail!")

    ///////////////////////////////////////////////////////////////////////////////////////

    @JvmStatic
    fun getViewDataBindingGenericTypeClazz(clazz: Class<*>): Class<*>? =
        getViewDataBindingGenericType(clazz) as? Class<*>?

    @JvmStatic
    fun getViewDataBindingGenericType(clazz: Class<*>): Type? {
        val superClazz: Class<*>? = clazz.superclass
        val genericSuperclass: Type? = clazz.genericSuperclass
        if (genericSuperclass !is ParameterizedType) {//当继承类不是参数化类型,就从父类中寻找
            return if (superClazz != null) {
                getViewDataBindingGenericType(superClazz)//当我们继承多层BaseActivity时递归查找泛型
            } else
                null
        }
        genericSuperclass.actualTypeArguments.filterIsInstance<Class<*>>()
            .run {
                this.printlog()
                if (this.isNotEmpty()) {
                    for (clz in this) {
                        if (clz.simpleName.endsWith("Binding"))
                            return clz
                    }
                }
                if (superClazz != null)
                    return getViewDataBindingGenericType(superClazz)
                else
                    return null
            }
    }

//    fun <VM : ViewDataBinding> ComponentActivity.createViewModel(position: Int): VM {
//        val vbClass = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.filterIsInstance<Class<*>>()
//        val viewModel = vbClass[position] as Class<VM>
//        return ViewModelProvider(this).get(viewModel)
//    }

//    fun <VB : ViewDataBinding> Any.getViewBinding(inflater: LayoutInflater, position: Int = 0): VB {
//        val vbClass = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.filterIsInstance<Class<*>>()
//        val inflate = vbClass[position].getDeclaredMethod("inflate", LayoutInflater::class.java)
//        return inflate.invoke(null, inflater) as VB
//    }
//
//
//    fun <VB : ViewDataBinding> Any.getViewBinding(inflater: LayoutInflater, container: ViewGroup?, position: Int = 0): VB {
//        val vbClass = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.filterIsInstance<Class<VB>>()
//        val inflate = vbClass[position].getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
//        return inflate.invoke(null, inflater, container, false) as VB
//    }
//
//    fun <VM : ViewDataBinding> ComponentActivity.createViewModel(position: Int): VM {
//        val vbClass = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments.filterIsInstance<Class<*>>()
//        val viewModel = vbClass[position] as Class<VM>
//        return ViewModelProvider(this).get(viewModel)
//    }
}