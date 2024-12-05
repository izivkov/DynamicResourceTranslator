package org.avmedia.translateapi

import android.content.Context
import me.bush.translator.Language
import me.bush.translator.Translator
import java.util.Locale

class DynamicTranslator {
    private val translator = Translator()
    private var language:Language = Language(Locale.getDefault().language)

    fun setLanguage(locale: Locale) {
        this.language = Language.invoke(locale.language)
    }

    fun getString(context: Context, resId: Int, vararg formatArgs: Any): String {
        return translate(context.getString(resId, *formatArgs))
    }

    fun getStringResource(context: Context, resId: Int, vararg formatArgs: Any): String {
        return translate(context.getString(resId, *formatArgs))
    }

    suspend fun getStringAsync(context: Context, resId: Int, vararg formatArgs: Any): String {
        return translateAsync(context.getString(resId, *formatArgs))
    }

    private fun translate(inText: String): String {
        return translator.translateBlocking(inText, language).translatedText
    }

    private suspend fun translateAsync(inText: String): String {
        return translator.translate(inText, language).translatedText
    }
}
