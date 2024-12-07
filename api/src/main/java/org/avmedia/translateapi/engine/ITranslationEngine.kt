package org.avmedia.translateapi.engine

import java.util.Locale

interface ITranslationEngine {
    fun useLanguageSpecificResourceFiles (): Boolean

    suspend fun translate(
        text: String,
        target: Locale,
    ): String

    suspend fun translateCatching(
        text: String,
        target: Locale,
    ): String?

    fun translateBlocking(
        text: String,
        target: Locale,
    ): String

    fun translateBlockingCatching(
        text: String,
        target: Locale,
    ): String?
}