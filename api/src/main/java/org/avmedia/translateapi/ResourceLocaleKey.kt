package org.avmedia.translateapi

import java.util.Locale

data class ResourceLocaleKey(val resourceId: Int, val locale: Locale) {
    override fun hashCode(): Int {
        val asString = "$resourceId.${locale.language.lowercase()}.${locale.country.uppercase()}"
        return asString.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResourceLocaleKey) return false
        return resourceId == other.resourceId &&
                locale.language.equals(other.locale.language, ignoreCase = true) &&
                locale.country.equals(other.locale.country, ignoreCase = true)
    }
}
