package org.avmedia.translateapi

import android.content.Context
import java.util.Locale

interface IDynamicTranslator {
    var locale: Locale?
    val translationOverwrites: TranslationOverwrites
    val networkConnectionChecker: NetworkConnectionChecker
    fun init(): DynamicTranslator
    fun setLanguage(locale: Locale): DynamicTranslator
    fun setOverwrites(entries: Array<Pair<ResourceLocaleKey, String>>): DynamicTranslator
    fun getString(
        context: Context,
        resId: Int,
        vararg formatArgs: Any,
        locale: Locale? = null,
    ): String

    fun stringResource(
        context: Context,
        id: Int,
        vararg formatArgs: Any,
        locale: Locale? = null
    ): String

    suspend fun stringResourceAsync(
        context: Context,
        id: Int,
        vararg formatArgs: Any,
        locale: Locale? = null
    ): String

    fun translate(inText: String, locale: Locale): String

    suspend fun translateAsync(inText: String, locale: Locale): String
    fun isResourceAvailableForLocale(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
        locale: Locale,
    ): Boolean

    fun readStringFromDefaultFile(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
    ): String

    fun getStringByLocal(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
        locale: String?
    ): String

    fun isValidLanguageCode(input: String): Boolean
}