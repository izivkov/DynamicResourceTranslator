package org.avmedia.translateapi

import android.content.Context
import android.content.res.Configuration
import me.bush.translator.Language
import me.bush.translator.Translator
import java.util.Locale

class DynamicTranslator {
    private val translator = Translator()
    private var locale = Locale.getDefault()
    private val translationOverwrites = TranslationOverwrites()

    fun init(): DynamicTranslator {
        // testLanguages()
        return this
    }

    fun setLanguage(locale: Locale): DynamicTranslator {
        this.locale = locale
        return this
    }

    fun setEngine(): DynamicTranslator {
        return this
    }

    fun setOverwrites(entries: Array<Pair<ResourceLocaleKey, String>>): DynamicTranslator {
        translationOverwrites.addAll(entries)
        return this
    }

    fun getString(
        context: Context,
        resId: Int,
        vararg formatArgs: Any,
        locale: Locale? = null
    ): String {
        return computeValue(
            context = context,
            resId = resId,
            formatArgs = formatArgs,
            locale = locale
        ) { text, language -> translate(text, language) }
    }

    suspend fun getStringAsync(
        context: Context,
        resId: Int,
        vararg formatArgs: Any,
        locale: Locale? = null
    ): String {
        return computeValue(
            context = context,
            resId = resId,
            formatArgs = formatArgs,
            locale = locale
        ) { text, language -> translateAsync(text, language) }
    }

    fun stringResource(
        context: Context,
        resId: Int,
        vararg formatArgs: Any,
        locale: Locale? = null
    ): String {
        return computeValue(
            context = context,
            resId = resId,
            formatArgs = formatArgs,
            locale = locale
        ) { text, language -> translate(text, language) }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <T> computeValue(
        context: Context,
        resId: Int,
        formatArgs: Array<out Any>,
        locale: Locale?,
        translator: (String, Language) -> T
    ): T {
        val curLocale = locale ?: this.locale
        val resourceKey = ResourceLocaleKey(resId, curLocale)
        val resolvedLanguage:Language

        // check if in the overwritten table
        val overWrittenValue = translationOverwrites[ResourceLocaleKey(resId, curLocale)]
        if (overWrittenValue != null) {
            return overWrittenValue as T
        }

        val storedValue = LocalDataStorage.getResource(context, resourceKey)
        if (storedValue != null) {
            return storedValue as T
        }

        try {
            resolvedLanguage = Language(curLocale.language)
        } catch (e: NullPointerException) {
            println("Could not handle ${curLocale.language}: $e")
            return context.getString(resId, *formatArgs, curLocale) as T
        }

        val formattedString = context.getString(resId, *formatArgs, curLocale)

        // if the value exists in the strings.xml for this locale, just return it without translation
        if (isResourceAvailableForLocale(context, resId, curLocale)) {
            return formattedString as T
        }

        val translatedValue = translator(formattedString, resolvedLanguage)
        // LocalDataStorage.putResource(context, resourceKey, translatedValue.toString())
        return translatedValue
    }

    private fun translate(inText: String, resolvedLanguage: Language): String {
        return translator.translateBlocking(inText, resolvedLanguage).translatedText
    }

    private suspend fun translateAsync(inText: String, resolvedLanguage: Language): String {
        return translator.translate(inText, resolvedLanguage).translatedText
    }

    private fun isResourceAvailableForLocale(
        context: Context,
        resId: Int,
        locale: Locale,
    ): Boolean {
        /*
        This is a hack, but it allows us to determine if the string is read from the language-specific string.xml,
        or from default string.xml. We are using a uncommon Locale, "kv" which should nor have its own string.xml, and will use the
        default. So, we can compare with the target locale string, and if identical, this means the target is also using default, and
        there is no string.xml for it. We therefore must translate the string, instead of taking it from the default resource.
         */
        val localStr = getStringByLocal(context, resId, locale.language)
        val defaultStr = getStringByLocal(context, resId, Locale("kv").language)

        return localStr != defaultStr
    }

    private fun getStringByLocal(context: Context, id: Int, locale: String?): String {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale?.let { Locale(it) })
        return context.createConfigurationContext(configuration).resources.getString(id)
    }

    fun testLanguages() {
        val languages = listOf(
            "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "cs", "cy",
            "da", "de", "el", "en", "es", "et", "eu", "fa", "fi", "fr", "ga",
            "gl", "gu", "he", "hi", "hr", "hu", "hy", "id", "is", "it", "ja",
            "ka", "kk", "km", "kn", "ko", "ky", "lo", "lt", "lv", "mk", "ml",
            "mn", "mr", "ms", "my", "nb", "ne", "nl", "pa", "pl", "pt", "ro",
            "ru", "si", "sk", "sl", "sq", "sr", "sv", "sw", "ta", "te", "th",
            "tl", "tr", "uk", "ur", "uz", "vi", "zh"
        )

        languages.forEach { code ->
            println("Language(\"$code\")")
            try {
                Language(code)
            } catch (e: NullPointerException) {
                println("Could not handle $code: $e")
            }
        }
    }
}
