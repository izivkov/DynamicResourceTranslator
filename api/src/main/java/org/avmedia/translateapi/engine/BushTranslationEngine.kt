package org.avmedia.translateapi.engine

import me.bush.translator.Language
import me.bush.translator.Translator
import java.util.Locale

class BushTranslationEngine (
) : ITranslationEngine {
    private val translator: Translator = Translator()

    override fun useLanguageSpecificResourceFiles(): Boolean {
        return true
    }

    override suspend fun translate(
        text: String,
        target: Locale,
    ): String {
        return translator.translate(
            text,
            Language(target.language),
            Language.AUTO,
        ).translatedText
    }

    override suspend fun translateCatching(text: String, target: Locale): String? {
        return translator.translateCatching(
            text,
            Language(target.language),
            Language.AUTO,
        ).getOrNull() as String?
    }

    override fun translateBlocking(
        text: String,
        target: Locale,
    ): String {
        return translator.translateBlocking(
            text,
            Language(target.language),
            Language.AUTO,
        ).translatedText
    }

    override fun translateBlockingCatching(
        text: String,
        target: Locale,
    ): String? {
        return translator.translateBlockingCatching(
            text,
            Language(target.language),
            Language.AUTO,
        ).getOrNull() as String?
    }
}
