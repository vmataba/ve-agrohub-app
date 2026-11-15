package com.tabaapps.ve_agrohub_app.util

import java.util.Locale

/**
 * Text Utility Functions for VE AGRO HUB ve-stock app.
 *
 * Java Educational Note:
 * Equivalent to Java String utility methods (e.g. Apache Commons `WordUtils.capitalizeFully`).
 */
object StringUtils {

    /**
     * Formats a raw input string into proper Title Case.
     * Example: "john doe" -> "John Doe", "JANE SMITH" -> "Jane Smith"
     */
    fun toTitleCase(input: String): String {
        if (input.isBlank()) return ""
        return input.trim().split("\\s+".toRegex()).joinToString(" ") { word ->
            word.lowercase(Locale.getDefault()).replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }
    }
}
