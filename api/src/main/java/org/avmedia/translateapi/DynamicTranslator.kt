package org.avmedia.translateapi

import android.content.Context
import android.content.res.Configuration
import org.avmedia.translateapi.engine.BushTranslationEngine
import org.avmedia.translateapi.engine.ITranslationEngine
import java.util.Locale

class DynamicTranslator : IDynamicTranslator {
    override var appLocale = Locale("en")
    override val translationOverwrites = TranslationOverwrites()
    override val networkConnectionChecker = NetworkConnectionChecker()
    private var translatorEngine: ITranslationEngine = BushTranslationEngine()

    override fun init(): DynamicTranslator {
        // Do initialization here...
        return this
    }

    override fun setAppLocale(locale: Locale): DynamicTranslator {
        this.appLocale = locale
        return this
    }

    override fun setEngine(engine: ITranslationEngine): DynamicTranslator {
        translatorEngine = engine
        return this
    }

    override fun setOverwrites(entries: Array<Pair<ResourceLocaleKey, String>>): DynamicTranslator {
        translationOverwrites.clear()
        translationOverwrites.addAll(entries)
        return this
    }

    override fun addOverwrites(entries: Array<Pair<ResourceLocaleKey, String>>) {
        translationOverwrites.addAll(entries)
    }

    override fun addOverwrite(overWrite: Pair<ResourceLocaleKey, String>) {
        translationOverwrites.add(overWrite.first, overWrite.second)
    }

    /**
     * Replace your context.getString() with this function.
     * ```
     * getString(context, R.strings.hello, "World", Locale("es"))
     * getString(context, R.strings.name)
     * ```
     * @param context   The context. Could be Application Context.
     * @param id The resource ID of the string to translate.
     * @param formatArgs optional parameters if your resource string takes parameters like "Hello $1%s"
     *
     * @return A [String] containing the translated text.
     */
    override fun getString(
        context: Context,
        id: Int,
        vararg formatArgs: Any,
    ): String {
        return computeValue(
            context = context,
            id = id,
            formatArgs = formatArgs,
        ) { text: String, language: Locale -> translate(text, language) }
    }

    /**
     * Replace your context.getString() with this function. Similar to [getString], but for Compose functions
     * ```
     * stringResource(LocalContext.current, R.strings.hello, "World", Locale("es"))
     * getString(LocalContext.current, R.strings.name)
     * ```
     * @param context The context. Usually set to `LocalContext.current`
     * @param id The resource ID of the string to translate.
     * @param formatArgs optional parameters if your resource string takes parameters like "Hello $1%s"
     *
     * @return A [String] containing the translated text.
     */
    override fun stringResource(
        context: Context,
        id: Int,
        vararg formatArgs: Any,
    ): String {
        return computeValue(
            context = context,
            id = id,
            formatArgs = formatArgs,
        ) { text: String, language: Locale -> translate(text, language) }
    }

    /**
     *  same as [stringResource], but suspended
     */
    override suspend fun stringResourceAsync(
        context: Context,
        id: Int,
        vararg formatArgs: Any,
    ): String {
        return computeValue(
            context = context,
            id = id,
            formatArgs = formatArgs,
        ) { text: String, language: Locale -> translateAsync(text, language) }
    }

    private fun translate(inText: String, locale: Locale): String {
        val result = translatorEngine.translate(inText, Locale.getDefault())
        println("Translated $inText -> $result")
        return result
    }

    private suspend fun translateAsync(inText: String, locale: Locale): String {
        val result = translatorEngine.translate(inText, locale)
        return result
    }

    private inline fun <T> computeValue(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
        translateFunc: (String, Locale) -> T
    ): String {
        // Create a key for identifying the resource in the desired locale
        val resourceLocaleKey = ResourceLocaleKey(id, Locale.getDefault())

        // Pre-process the input string with formatting arguments
        val preProcessedResult = preProcess(context, id, formatArgs, resourceLocaleKey)

        // If further translation is needed, apply the translation function
        if (preProcessedResult.needsFurtherTranslation) {
            val translatedValue = translateFunc(preProcessedResult.preProcessedString, Locale.getDefault())

            // Post-process the translated result and store it for caching or reuse
            postProcess(context, translatedValue.toString(), resourceLocaleKey)

            // Return the translated string
            return translatedValue.toString()
        }

        // If no further translation is needed, return the pre-processed string
        return preProcessedResult.preProcessedString
    }

    private fun preProcess(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
        resourceLocaleKey: ResourceLocaleKey
    ): PreprocessResult {
        val defaultLocale = Locale.getDefault()
        val language = defaultLocale.language.lowercase()

        val overWrittenValue = translationOverwrites[ResourceLocaleKey(id, defaultLocale)]
        if (overWrittenValue != null) {
            return PreprocessResult(String.format(overWrittenValue, *formatArgs), false)
        }

        val storedValue = LocalDataStorage.getResource(context, resourceLocaleKey)
        if (storedValue != null) {
            return PreprocessResult(storedValue, false)
        }

        val resourceString = context.getString(id, *formatArgs, language)

        if (isResourceAvailableForLocale(context, id, formatArgs)) {
            return PreprocessResult(resourceString, false)
        }

        if (!networkConnectionChecker.isConnected(context)) {
            return PreprocessResult(resourceString, false)
        }

        return PreprocessResult(resourceString, true)
    }

    private fun postProcess(
        context: Context,
        translatedValue: String,
        resourceLocaleKey: ResourceLocaleKey
    ) {
        LocalDataStorage.putResource(context, resourceLocaleKey, translatedValue)
    }

    private fun isResourceAvailableForLocale(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
    ): Boolean {
        /*
        We compare a string from the default string.xml with the target locale string,
        and if identical, this means the target is also using default, and
        there is no string.xml for it. This tess us if we should translate the string.
        */

        val defaultLocale = Locale.getDefault()
        val defaultLanguage = defaultLocale.language
        val appLanguage = appLocale.language

        val localStr = getStringByLocal(context, id, formatArgs, defaultLanguage)
        val defaultStr = readStringFromDefaultFile(context, id, formatArgs)

        return localStr != defaultStr
                || defaultLanguage == appLanguage
    }

    private fun readStringFromDefaultFile(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
    ): String {
        /*
        This is a hack, but it allows us to determine if the string is read from the language-specific string.xml,
        or from default string.xml. We are using a uncommon Locale, "kv" which should nor have its own string.xml, and will use the
        default.
         */

        return getStringByLocal(context, id, formatArgs, Locale("kv").language)
    }

    private fun getStringByLocal(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
        locale: String
    ): String {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale(locale))
        return context.createConfigurationContext(configuration).resources.getString(
            id,
            *formatArgs
        )
    }

    private fun isValidLanguageCode(input: String): Boolean {
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

    data class PreprocessResult(
        val preProcessedString: String,
        val needsFurtherTranslation: Boolean
    )
}
