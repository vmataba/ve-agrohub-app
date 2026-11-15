package com.tabaapps.ve_agrohub_app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tabaapps.ve_agrohub_app.data.model.AllowedUser
import com.tabaapps.ve_agrohub_app.data.model.ProductType
import com.tabaapps.ve_agrohub_app.data.model.Store

/**
 * Unified Admin Configuration & Settings Screen for ve-stock.
 *
 * Separates master configuration options from everyday stock-taking operations:
 * 1. User Whitelist & RBAC Security (ADMIN access control)
 * 2. Store Branches & Warehouses Setup
 * 3. Product Catalog & TZS Pricing Setup
 */
@Composable
fun AdminSettingsScreen(
    isAdmin: Boolean,
    allowedUsers: List<AllowedUser>,
    stores: List<Store>,
    productTypes: List<ProductType>,
    onAddAllowedUser: (email: String, role: String) -> Unit,
    onRemoveAllowedUser: (email: String) -> Unit,
    onAddStore: (name: String, location: String) -> Unit,
    onDeleteStore: (id: String) -> Unit,
    onAddProductType: (name: String, category: String, priceTzs: Long, brand: String, unitSize: String) -> Unit,
    onDeleteProductType: (id: String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = if (isAdmin) {
        listOf(
            Triple("Users & RBAC", Icons.Default.Group, 0),
            Triple("Store Branches", Icons.Default.Store, 1),
            Triple("Product Catalog", Icons.Default.Category, 2)
        )
    } else {
        listOf(
            Triple("Store Branches", Icons.Default.Store, 0),
            Triple("Product Catalog", Icons.Default.Category, 1)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Configuration Header Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAdmin) "Admin Master Configuration" else "System Reference Catalog",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isAdmin)
                    "Manage security whitelist, branch locations, and product catalog pricing."
                else
                    "Read-only catalog and store branch directory.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Sub-Navigation Tab Row
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, (label, icon, _) ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        // Content Area
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "AdminTabTransition"
            ) { tabIndex ->
                if (isAdmin) {
                    when (tabIndex) {
                        0 -> UserManagementScreen(
                            allowedUsers = allowedUsers,
                            onAddAllowedUser = onAddAllowedUser,
                            onRemoveAllowedUser = onRemoveAllowedUser
                        )
                        1 -> StoreManagementScreen(
                            stores = stores,
                            isAdmin = true,
                            onAddStore = onAddStore,
                            onDeleteStore = onDeleteStore
                        )
                        2 -> ProductTypesScreen(
                            productTypes = productTypes,
                            isAdmin = true,
                            onAddProductType = onAddProductType,
                            onDeleteProductType = onDeleteProductType
                        )
                    }
                } else {
                    when (tabIndex) {
                        0 -> StoreManagementScreen(
                            stores = stores,
                            isAdmin = false,
                            onAddStore = onAddStore,
                            onDeleteStore = onDeleteStore
                        )
                        1 -> ProductTypesScreen(
                            productTypes = productTypes,
                            isAdmin = false,
                            onAddProductType = onAddProductType,
                            onDeleteProductType = onDeleteProductType
                        )
                    }
                }
            }
        }
    }
}
