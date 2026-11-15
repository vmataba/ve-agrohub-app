package com.tabaapps.ve_agrohub_app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Visual Brand Badge & Product Logo component.
 * Instantly identifies product brand (Koudijs / Irvine's) and packaging for shopkeepers & refillers.
 */
@Composable
fun BrandBadge(
    brand: String,
    modifier: Modifier = Modifier,
    unitSize: String? = null
) {
    val isKoudijs = brand.contains("Koudijs", ignoreCase = true)
    val isIrvines = brand.contains("Irvine", ignoreCase = true)

    val (badgeBg, textColor, displayBrand, icon) = when {
        isKoudijs -> Quadruple(
            Color(0xFFE3F2FD), // Light Dutch Blue
            Color(0xFF0D47A1), // Deep Koudijs Blue
            "KOUDIJS 🇳🇱",
            Icons.Default.Verified
        )
        isIrvines -> Quadruple(
            Color(0xFFFFF3E0), // Soft Irvine's Orange
            Color(0xFFE65100), // Irvine's Deep Orange
            "IRVINE'S 🐔",
            Icons.Default.Pets
        )
        else -> Quadruple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            brand.ifBlank { "GENERAL" }.uppercase(),
            Icons.Default.Store
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main Brand Logo Chip
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = badgeBg
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = brand,
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = displayBrand,
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Optional Packaging Unit Badge
        if (!unitSize.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = unitSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
