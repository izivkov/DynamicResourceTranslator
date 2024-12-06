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
        val language = curLocale.language.lowercase()

        if (!isValidLanguageCode (language)) {
            return "Invalid Language code [${language}] provided!" as T
        }

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
            resolvedLanguage = Language(language)
        } catch (e: NullPointerException) {
            println("Could not handle ${language}: $e")
            return context.getString(resId, *formatArgs) as T
        }

        val formattedString = getStringByLocal(context, resId, language)

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

    private fun testLanguages() {
        val languages = listOf(
            "aa", "ab", "ae", "af", "ak", "am", "an", "ar", "as", "av", "ay", "az",
            "ba", "be", "bg", "bh", "bi", "bm", "bn", "bo", "br", "bs", "ca", "ce",
            "ch", "co", "cr", "cs", "cu", "cv", "cy", "da", "de", "dv", "dz", "ee",
            "el", "en", "eo", "es", "et", "eu", "fa", "ff", "fi", "fj", "fo", "fr",
            "fy", "ga", "gd", "gl", "gn", "gu", "gv", "ha", "he", "hi", "ho", "hr",
            "ht", "hu", "hy", "hz", "ia", "id", "ie", "ig", "ii", "ik", "io", "is",
            "it", "iu", "ja", "jv", "ka", "kg", "ki", "kj", "kk", "kl", "km", "kn",
            "ko", "kr", "ks", "ku", "kv", "kw", "ky", "la", "lb", "lg", "li", "ln",
            "lo", "lt", "lu", "lv", "mg", "mh", "mi", "mk", "ml", "mn", "mr", "ms",
            "mt", "my", "na", "nb", "nd", "ne", "ng", "nl", "nn", "no", "nr", "nv",
            "ny", "oc", "oj", "om", "or", "os", "pa", "pi", "pl", "ps", "pt", "qu",
            "rm", "rn", "ro", "ru", "rw", "sa", "sc", "sd", "se", "sg", "si", "sk",
            "sl", "sm", "sn", "so", "sq", "sr", "ss", "st", "su", "sv", "sw", "ta",
            "te", "tg", "th", "ti", "tk", "tl", "tn", "to", "tr", "ts", "tt", "tw",
            "ty", "ug", "uk", "ur", "uz", "ve", "vi", "vo", "wa", "wo", "xh", "yi",
            "yo", "za", "zh", "zu"
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

    fun isValidLanguageCode(input: String): Boolean {
        val languageCodes = arrayOf(
            "aa", "ab", "ae", "af", "ak", "am", "an", "ar", "as", "av", "ay", "az",
            "ba", "be", "bg", "bh", "bi", "bm", "bn", "bo", "br", "bs", "ca", "ce",
            "ch", "co", "cr", "cs", "cu", "cv", "cy", "da", "de", "dv", "dz", "ee",
            "el", "en", "eo", "es", "et", "eu", "fa", "ff", "fi", "fj", "fo", "fr",
            "fy", "ga", "gd", "gl", "gn", "gu", "gv", "ha", "he", "hi", "ho", "hr",
            "ht", "hu", "hy", "hz", "ia", "id", "ie", "ig", "ii", "ik", "io", "is",
            "it", "iu", "ja", "jv", "ka", "kg", "ki", "kj", "kk", "kl", "km", "kn",
            "ko", "kr", "ks", "ku", "kv", "kw", "ky", "la", "lb", "lg", "li", "ln",
            "lo", "lt", "lu", "lv", "mg", "mh", "mi", "mk", "ml", "mn", "mr", "ms",
            "mt", "my", "na", "nb", "nd", "ne", "ng", "nl", "nn", "no", "nr", "nv",
            "ny", "oc", "oj", "om", "or", "os", "pa", "pi", "pl", "ps", "pt", "qu",
            "rm", "rn", "ro", "ru", "rw", "sa", "sc", "sd", "se", "sg", "si", "sk",
            "sl", "sm", "sn", "so", "sq", "sr", "ss", "st", "su", "sv", "sw", "ta",
            "te", "tg", "th", "ti", "tk", "tl", "tn", "to", "tr", "ts", "tt", "tw",
            "ty", "ug", "uk", "ur", "uz", "ve", "vi", "vo", "wa", "wo", "xh", "yi",
            "yo", "za", "zh", "zu"
        )
        return input in languageCodes
    }
}
