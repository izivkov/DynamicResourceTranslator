package org.avmedia.translateapi

import android.content.Context
import android.content.res.Configuration
import org.avmedia.translateapi.engine.BushTranslationEngine
import org.avmedia.translateapi.engine.ITranslationEngine
import java.util.Locale

class DynamicTranslator (
    private var translator: ITranslationEngine = BushTranslationEngine()
) : IDynamicTranslator {
    override var locale = Locale.getDefault()
    override val translationOverwrites = TranslationOverwrites()
    override val networkConnectionChecker = NetworkConnectionChecker ()

    override fun init(): DynamicTranslator {
        // Do initialization here...
        return this
    }

    override fun setLanguage(locale: Locale): DynamicTranslator {
        this.locale = locale
        return this
    }

    override fun setOverwrites(entries: Array<Pair<ResourceLocaleKey, String>>): DynamicTranslator {
        translationOverwrites.addAll(entries)
        return this
    }

    override fun getString(
        context: Context,
        resId: Int,
        vararg formatArgs: Any,
        locale: Locale?,
    ): String {
        return if (translator.isInline()) {
            computeValueInline(
                context = context,
                resId = resId,
                formatArgs = formatArgs,
                locale = locale
            ) { text: String, language: Locale -> translate(text, language) }
        } else {
            computeValue (
                context = context,
                resId = resId,
                formatArgs = formatArgs,
                locale = locale
            ) { text: String, language: Locale -> translate(text, language) }
        }
    }

    override fun stringResource(
        context: Context,
        resId: Int,
        vararg formatArgs: Any,
        locale: Locale?
    ): String {
        return if (translator.isInline()) {
            computeValueInline(
                context = context,
                resId = resId,
                formatArgs = formatArgs,
                locale = locale
            ) { text: String, language: Locale -> translate(text, language) }
        } else {
            computeValue(
                context = context,
                resId = resId,
                formatArgs = formatArgs,
                locale = locale
            ) { text: String, language: Locale -> translate(text, language) }
        }
    }

    override suspend fun stringResourceAsync(
        context: Context,
        resId: Int,
        vararg formatArgs: Any,
        locale: Locale?
    ): String {
        return if (translator.isInline()) {
            computeValueInline(
                context = context,
                resId = resId,
                formatArgs = formatArgs,
                locale = locale
            ) { text: String, language: Locale -> translateAsync(text, language) }
        } else {
            computeValue(
                context = context,
                resId = resId,
                formatArgs = formatArgs,
                locale = locale
            ) { text: String, language: Locale -> translateAsync(text, language) }
        }
    }

    // This function is used by translators which do all translation inline,
    // i.e. do not look at any language-specific resource files and do not use external translators.
    private inline fun <T> computeValueInline(
        context: Context,
        resId: Int,
        formatArgs: Array<out Any>,
        locale: Locale?,
        translator: (String, Locale) -> T
    ): T {
        val curLocale = locale ?: this.locale
        val origString = readStringFromDefaultFile(context, resId, formatArgs)
        return translator(origString, curLocale)
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <T> computeValue(
        context: Context,
        resId: Int,
        formatArgs: Array<out Any>,
        locale: Locale?,
        translator: (String, Locale) -> T
    ): T {
        val curLocale = locale ?: this.locale
        val resourceKey = ResourceLocaleKey(resId, curLocale.language)
        val language = curLocale.language.lowercase()

        require(isValidLanguageCode(language)) { return "Invalid Language code [${language}] provided!" as T }

        // check if string in the overwritten table
        val overWrittenValue = translationOverwrites[ResourceLocaleKey(resId, curLocale.language)]
        if (overWrittenValue != null) {
            return String.format(overWrittenValue, *formatArgs) as T
        }

        // try to get from local storage.
        val storedValue = LocalDataStorage.getResource(context, resourceKey)
        if (storedValue != null) {
            return storedValue as T
        }

        val formattedString = getStringByLocal(context, resId, formatArgs, language)

        // if the value exists in the strings.xml for this locale, just return it without translation
        if (isResourceAvailableForLocale(context, resId, formatArgs, curLocale)) {
            return formattedString as T
        }

        // If we do not have network connection, do not try to translate, return the original string.
        if (!networkConnectionChecker.isConnected(context)) {
            return formattedString as T
        }

        val translatedValue = translator(formattedString, curLocale)

        LocalDataStorage.putResource(context, resourceKey, translatedValue.toString())
        return translatedValue
    }

    override fun translate(inText: String, locale: Locale): String {
        return translator.translate(inText, locale)
    }

    override suspend fun translateAsync(inText: String, locale: Locale): String {
        return translator.translateAsync(inText, locale)
    }

    override fun isResourceAvailableForLocale(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
        locale: Locale,
    ): Boolean {
        /*
        We compare a string from the default string.xml with the target locale string,
        and if identical, this means the target is also using default, and
        there is no string.xml for it. This tess us if we should translate the string.
        */

        val localStr = getStringByLocal(context, id, formatArgs, locale.language)
        val defaultStr = readStringFromDefaultFile(context, id, formatArgs)

        return localStr != defaultStr
    }

    override fun readStringFromDefaultFile(
        context: Context,
        resId: Int,
        formatArgs: Array<out Any>,
    ): String {
        /*
        This is a hack, but it allows us to determine if the string is read from the language-specific string.xml,
        or from default string.xml. We are using a uncommon Locale, "kv" which should nor have its own string.xml, and will use the
        default.
         */

        return getStringByLocal(context, resId, formatArgs, Locale("kv").language)
    }

    override fun getStringByLocal(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
        locale: String?
    ): String {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale?.let { Locale(it) })
        return context.createConfigurationContext(configuration).resources.getString(
            id,
            *formatArgs
        )
    }

    override fun isValidLanguageCode(input: String): Boolean {
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
