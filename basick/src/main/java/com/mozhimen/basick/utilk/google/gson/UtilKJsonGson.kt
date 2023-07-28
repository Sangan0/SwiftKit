package com.mozhimen.basick.utilk.google.gson

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.mozhimen.basick.utilk.android.util.et
import com.mozhimen.basick.utilk.bases.BaseUtilK

/**
 * @ClassName UtilKJsonGson
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/2/3 17:21
 * @Version 1.0
 */
fun Any.toJsonGson(): String =
    UtilKJsonGson.obj2Json(this)

fun <T> String.toTGson(token: TypeToken<T>): T =
    UtilKJsonGson.json2T(this, token)

fun <T> String.toTGson(clazz: Class<T>): T? =
    UtilKJsonGson.json2T(this, clazz)

fun Any.toJsonWithExposeGson(): String =
    UtilKJsonGson.obj2JsonWithExpose(this)

fun <T> String.toTWithExposeGson(clazz: Class<T>): T? =
    UtilKJsonGson.json2TWithExpose(this, clazz)

object UtilKJsonGson : BaseUtilK() {
    private val _gson by lazy { Gson() }
    private val _gsonWithField by lazy { GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create() }
    private val _gsonWithExpose by lazy { GsonBuilder().excludeFieldsWithoutExposeAnnotation().create() }

    @JvmStatic
    fun <T> t2Json(t: T): String =
        _gson.toJson(t)

    @JvmStatic
    fun obj2Json(obj: Any): String =
        _gson.toJson(obj)

    @JvmStatic
    fun <T> json2T(json: String, typeToken: TypeToken<T>): T =
        _gson.fromJson(json, typeToken.type)

    @JvmStatic
    fun <T> json2T(json: String, clazz: Class<T>): T? =
        _gson.fromJson(json, clazz)

    @JvmStatic
    fun obj2JsonWithField(obj: Any): String =
        _gsonWithField.toJson(obj)

    @JvmStatic
    fun <T> json2TWithField(json: String, clazz: Class<T>): T =
        _gsonWithField.fromJson(json, clazz)

    @JvmStatic
    fun obj2JsonWithExpose(obj: Any): String =
        _gsonWithExpose.toJson(obj)

    @JvmStatic
    fun <T> json2TWithExpose(json: String, clazz: Class<T>): T? =
        _gsonWithExpose.fromJson(json, clazz)

    @JvmStatic
    fun json2JsonElement(json: String): JsonElement? = try {
        json2T(json.trim { it <= ' ' }, JsonElement::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        e.message?.et(TAG)
        null
    }
}