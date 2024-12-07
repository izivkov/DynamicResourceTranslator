package org.avmedia.dynamicresourcetranslator

import org.avmedia.translateapi.engine.ITranslationEngine

import java.util.Locale

class UppercaseTranslator: ITranslationEngine {

    override fun useLanguageSpecificResourceFiles(): Boolean {
        return false
    }

    override suspend fun translate(
        text: String,
        target: Locale,
    ): String {
        return text.uppercase()
    }

    override suspend fun translateCatching(text: String, target: Locale): String {
        return text.uppercase()
    }

    override fun translateBlocking(text: String, target: Locale): String {
        return text.uppercase()
    }

    override fun translateBlockingCatching(text: String, target: Locale): String {
        return text.uppercase()
    }
}