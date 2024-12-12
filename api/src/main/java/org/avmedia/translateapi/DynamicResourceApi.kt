package org.avmedia.translateapi

import org.avmedia.translateapi.engine.BushTranslationEngine
import org.avmedia.translateapi.engine.ITranslationEngine
import java.util.Locale

object DynamicResourceApi {
    private lateinit var translator: DynamicTranslator

    fun init(): DynamicResourceApi {
        translator.init()
        return this
    }

    fun setLanguage(locale: Locale): DynamicResourceApi {
        getApi().setLanguage(locale)
        return this
    }

    fun setOverwrites(entries: Array<Pair<ResourceLocaleKey, String>>): DynamicResourceApi {
        getApi().setOverwrites(entries)
        return this
    }

    fun setEngine(engine: ITranslationEngine): DynamicResourceApi {
        getApi().setEngine(engine)
        return this
    }

    fun getApi(): DynamicTranslator {
        require(::translator.isInitialized) {"DynamicResourceApi not initialized. Call init() first."}
        return translator
    }
}