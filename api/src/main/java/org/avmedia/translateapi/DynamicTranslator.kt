package org.avmedia.translateapi

import android.content.Context
import me.bush.translator.Language
import me.bush.translator.Translator
import java.util.Locale

class DynamicTranslator {
    private val translator = Translator()
    private var locale = Locale.getDefault()
    private var language:Language = Language(locale.language)

    fun setLanguage(locale: Locale) {
        this.locale = locale
        this.language = Language.invoke(locale.language)
    }

    fun getString(context: Context, resId: Int, vararg formatArgs: Any, locale: Locale? = null): String {
        val lang = locale?.language ?: this.language
        val resolvedLanguage = Language( lang.toString() )
        return translate(context.getString(resId, *formatArgs), resolvedLanguage)
    }

    suspend fun getStringAsync(context: Context, resId: Int, vararg formatArgs: Any, locale: Locale? = null): String {
        val lang = locale?.language ?: this.language
        val resolvedLanguage = Language( lang.toString() )
        return translateAsync(context.getString(resId, *formatArgs), resolvedLanguage)
    }

    fun stringResource(context: Context, resId: Int, vararg formatArgs: Any, locale: Locale? = null): String {
        val lang = locale?.language ?: this.language
        val resolvedLanguage = Language( lang.toString() )
        return translate(context.getString(resId, *formatArgs), resolvedLanguage)
    }

    private fun translate(inText: String, resolvedLanguage: Language): String {
        return translator.translateBlocking(inText, resolvedLanguage).translatedText
    }

    private suspend fun translateAsync(inText: String, resolvedLanguage: Language): String {
        return translator.translate(inText, resolvedLanguage).translatedText
    }
}
