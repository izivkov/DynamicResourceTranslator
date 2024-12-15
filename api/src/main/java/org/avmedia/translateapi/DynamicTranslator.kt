package org.avmedia.translateapi

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import org.avmedia.translateapi.engine.BushTranslationEngine
import org.avmedia.translateapi.engine.ITranslationEngine
import java.util.Locale

class DynamicTranslator : IDynamicTranslator {
    override var locale = Locale.getDefault()
    override val translationOverwrites = TranslationOverwrites()
    override val networkConnectionChecker = NetworkConnectionChecker()
    private var translatorEngines: MutableList<ITranslationEngine> = mutableListOf(BushTranslationEngine())

    override fun init(): DynamicTranslator {
        // Do initialization here...
        return this
    }

    override fun setLanguage(locale: Locale): DynamicTranslator {
        this.locale = locale
        return this
    }

    override fun setEngine(engine: ITranslationEngine): DynamicTranslator {
        translatorEngines.clear()
        translatorEngines.add(engine)
        return this
    }

    override fun setEngines(engines: Collection<ITranslationEngine>): DynamicTranslator {
        translatorEngines.clear()
        translatorEngines.addAll(engines)
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
     * @param resId The resource ID of the string to translate.
     * @param formatArgs optional parameters if your resource string takes parameters like "Hello $1%s"
     * @param locale optional parameters if you like to translate into a specific language. If not provided, the default phone language will be used, set in Android System configuration.
     *
     * @return A [String] containing the translated text.
     */
    override fun getString(
        context: Context,
        id: Int,
        vararg formatArgs: Any,
        locale: Locale?,
    ): String {
        return computeValue(
            context = context,
            id = id,
            formatArgs = formatArgs,
            locale = locale
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
     * @param locale optional parameters if you like to translate into a specific language. If not provided, the default phone language will be used, set in Android System configuration.
     *
     * @return A [String] containing the translated text.
     */
    override fun stringResource(
        context: Context,
        id: Int,
        vararg formatArgs: Any,
        locale: Locale?
    ): String {
        return computeValue(
            context = context,
            id = id,
            formatArgs = formatArgs,
            locale = locale
        ) { text: String, language: Locale -> translate(text, language) }
    }

    /**
     *  same as [stringResource], but suspended
     */
    override suspend fun stringResourceAsync(
        context: Context,
        id: Int,
        vararg formatArgs: Any,
        locale: Locale?
    ): String {
        return computeValue(
            context = context,
            id = id,
            formatArgs = formatArgs,
            locale = locale
        ) { text: String, language: Locale -> translateAsync(text, language) }
    }

    private fun translate(inText: String, locale: Locale): String {
        var currentText = inText
        for (engine in translatorEngines) {
            val result = engine.translate(currentText, locale)
            if (result.isNotBlank()) {
                currentText = result
            }
        }
        return currentText
    }

    private suspend fun translateAsync(inText: String, locale: Locale): String {
        var currentText = inText
        for (engine in translatorEngines) {
            val result = engine.translate(currentText, locale)
            if (result.isNotBlank()) {
                currentText = result
            }
        }
        return currentText
    }

    private inline fun <T> computeValue(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
        locale: Locale?,
        translateFunc: (String, Locale) -> T
    ): String {
        val curLocale = locale ?: this.locale
        val resourceLocaleKey = ResourceLocaleKey(id, curLocale)

        val preProcessedResult =
            preProcess(context, id, formatArgs, curLocale, resourceLocaleKey)

        if (preProcessedResult.needsFurtherTranslation) {
            val translatedValue = translateFunc(preProcessedResult.preProcessedString, curLocale)
            postProcess(context, translatedValue as String, resourceLocaleKey)
        }

        return preProcessedResult.preProcessedString
    }

    private fun preProcess(
        context: Context,
        id: Int,
        formatArgs: Array<out Any>,
        locale: Locale,
        resourceLocaleKey: ResourceLocaleKey
    ): PreprocessResult {
        val language = locale.language.lowercase()
        require(isValidLanguageCode(language)) { return PreprocessResult("Invalid Language code [${language}] provided!", false) }

        val overWrittenValue = translationOverwrites[ResourceLocaleKey(id, locale)]
        if (overWrittenValue != null) {
            return PreprocessResult(String.format(overWrittenValue, *formatArgs), false)
        }

        val storedValue = LocalDataStorage.getResource(context, resourceLocaleKey)
        if (storedValue != null) {
            return PreprocessResult(storedValue, false)
        }

        val resourceString = getStringByLocal(context, id, formatArgs, language)

        if (isResourceAvailableForLocale(context, id, formatArgs, locale)) {
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
        LocalDataStorage.putResource(context, resourceLocaleKey, translatedValue.toString())
    }

    private fun isResourceAvailableForLocale(
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
        locale: String?
    ): String {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale?.let { Locale(it) })
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

    data class PreprocessResult(val preProcessedString: String, val needsFurtherTranslation: Boolean)
}
