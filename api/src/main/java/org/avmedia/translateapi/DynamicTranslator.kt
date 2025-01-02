package org.avmedia.translateapi

import android.content.Context
import android.content.res.Configuration
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import me.bush.translator.TranslationException
import org.avmedia.translateapi.engine.BushTranslationEngine
import org.avmedia.translateapi.engine.ITranslationEngine
import java.util.Locale

class DynamicTranslator : IDynamicTranslator {
    override var appLocale = Locale("en")
    override val translationOverwrites = TranslationOverwrites()
    override val networkConnectionChecker = NetworkConnectionChecker()
    private var translatorEngines: MutableList<ITranslationEngine> =
        mutableListOf(BushTranslationEngine())

    private var safeMode = false
    override fun setSafeMode(safeMode: Boolean): DynamicTranslator {
        this.safeMode = safeMode
        return this
    }

    override fun init(): DynamicTranslator {
        // Do initialization here...
        return this
    }

    override fun setAppLocale(locale: Locale): DynamicTranslator {
        this.appLocale = locale
        return this
    }

    override fun setEngine(engine: ITranslationEngine): DynamicTranslator {
        translatorEngines.clear()
        translatorEngines.add(engine)
        return this
    }

    override fun addEngine(engine: ITranslationEngine): DynamicTranslator {
        translatorEngines.add(engine)
        return this
    }

    override fun addEngines(engines: Collection<ITranslationEngine>): DynamicTranslator {
        translatorEngines.addAll(engines)
        return this
    }

    override fun setOverwrites(entries: Array<Pair<ResourceLocaleKey, () -> String>>): DynamicTranslator {
        translationOverwrites.clear()
        translationOverwrites.addAll(entries)
        return this
    }

    override fun addOverwrites(entries: Array<Pair<ResourceLocaleKey, () -> String>>) {
        translationOverwrites.addAll(entries)
    }

    override fun addOverwrite(overWrite: Pair<ResourceLocaleKey, () -> String>) {
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
        if (safeMode) {
            return context.getString(id, *formatArgs)
        }
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
        if (safeMode) {
            return context.getString(id, *formatArgs)
        }
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
        if (safeMode) {
            return context.getString(id, *formatArgs)
        }
        return computeValue(
            context = context,
            id = id,
            formatArgs = formatArgs,
        ) { text: String, language: Locale -> translateAsync(text, language) }
    }

    private fun translate(inText: String, locale: Locale): String {
        var currentText = inText

        translatorEngines.forEach { engine ->
            runCatching {
                runBlocking {
                    withTimeout(2000) {
                        engine.translate(currentText, locale) // Perform the translation
                    }
                }
            }.onSuccess { result ->
                if (result.isNotBlank()) {
                    currentText = result
                    println("Translated $inText -> $result")
                }
            }.onFailure { e ->
                when (e) {
                    is TranslationException -> println ("Translation Exception in engine: $engine")
                    is TimeoutCancellationException -> println("Translation timed out for engine: $engine")
                    else -> println("Error during translation with engine $engine: ${e.message}")
                }
                safeMode = true
            }
        }

        return currentText
    }

    private suspend fun translateAsync(inText: String, locale: Locale): String {
        var currentText = inText
        for (engine in translatorEngines) {
            val result = engine.translateAsync(currentText, locale)
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
        translateFunc: (String, Locale) -> T
    ): String {
        // Create a key for identifying the resource in the desired locale
        val resourceLocaleKey = ResourceLocaleKey(id, Locale.getDefault())

        // Pre-process the input string with formatting arguments
        val preProcessedResult = preProcess(context, id, formatArgs, resourceLocaleKey)

        // If further translation is needed, apply the translation function
        if (preProcessedResult.needsFurtherTranslation) {
            val translatedValue =
                translateFunc(preProcessedResult.preProcessedString, Locale.getDefault())

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

        val overWrittenValue = translationOverwrites[ResourceLocaleKey(id, defaultLocale)]
        if (overWrittenValue != null) {
            return PreprocessResult(String.format(overWrittenValue.invoke(), *formatArgs), false)
        }

        val storedValue = LocalDataStorage.getResource(context, resourceLocaleKey)
        if (storedValue != null) {
            return PreprocessResult(storedValue, false)
        }

        val resourceString = context.getString(id, *formatArgs)

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
        We compare a string from the default strings.xml file with the corresponding string in the target locale.
        If the two strings are identical, it indicates that the target locale is falling back to the default strings.xml
        because a locale-specific strings.xml file does not exist.
        This helps us determine whether the string needs translation.
        */

        val defaultLocale = Locale.getDefault()
        val defaultLanguage = defaultLocale.language
        val appLanguage = appLocale.language

        val localStr = getStringByLocale(context, id, formatArgs, defaultLanguage)
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
        This approach is a workaround, but it helps us identify whether the string is
        being sourced from a language-specific strings.xml file or the default strings.xml.
        We achieve this by using an uncommon locale, "kv", which is not expected to have
        its own strings.xml file and therefore falls back to the default.
        */

        return getStringByLocale(context, id, formatArgs, Locale("kv").language)
    }

    private fun getStringByLocale(
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

    data class PreprocessResult(
        val preProcessedString: String,
        val needsFurtherTranslation: Boolean
    )
}
