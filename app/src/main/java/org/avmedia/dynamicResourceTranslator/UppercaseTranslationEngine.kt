package org.avmedia.dynamicResourceTranslator

import org.avmedia.translateapi.engine.ITranslationEngine

import java.util.Locale

class UppercaseTranslationEngine: ITranslationEngine {
    override fun translate(
        text: String,
        target: Locale,
    ): String {
        return text.uppercase()
    }
}