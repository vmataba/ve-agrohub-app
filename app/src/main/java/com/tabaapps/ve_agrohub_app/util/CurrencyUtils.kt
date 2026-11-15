package com.tabaapps.ve_agrohub_app.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Currency Utility Functions for VE AGRO HUB ve-stock app.
 *
 * Java Educational Note:
 * Utility classes in Java were often marked `final` with private constructors, containing `public static` methods.
 * In Kotlin, top-level functions or `object` declarations are used instead.
 * `java.text.NumberFormat` is the standard Java SE formatting API used across Java and Kotlin Android apps.
 */
object CurrencyUtils {

    /**
     * Formats a raw numerical amount into a Tanzanian Shilling (TZS) display string.
     * Example: 50000L -> "TZS 50,000"
     *
     * @param amount The monetary value in Tanzanian Shillings.
     * @return Formatted string representation starting with "TZS ".
     *
     * Java Analogy:
     * public static String formatTzsAmount(long amount) { ... }
     */
    fun formatTzsAmount(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return "TZS ${formatter.format(amount)}"
    }
}
