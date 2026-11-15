package com.tabaapps.ve_agrohub_app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tabaapps.ve_agrohub_app.data.model.InventoryItemWithDetails
import com.tabaapps.ve_agrohub_app.data.model.ProductType
import com.tabaapps.ve_agrohub_app.data.model.Store
import com.tabaapps.ve_agrohub_app.ui.components.BrandBadge
import com.tabaapps.ve_agrohub_app.ui.theme.StockAlertRed
import com.tabaapps.ve_agrohub_app.ui.theme.StockSuccessGreen
import com.tabaapps.ve_agrohub_app.ui.theme.StockWarningAmber

/**
 * Enhanced Inventory Management Screen with full-width search bar, dropdown store/category filters,
 * distinct quick adjustment vs formal refill controls, audit photo clarification, and a redesigned Refill Dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    stores: List<Store>,
    productTypes: List<ProductType>,
    inventoryItems: List<InventoryItemWithDetails>,
    onUpdateQuantity: (itemId: String, newQty: Int) -> Unit,
    onIncrementImageCaptureCount: (itemId: String) -> Unit,
    onRecordRefill: (storeId: String, productId: String, qty: Int, notes: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStoreId by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Dialog state for Stock Refill
    var showRefillDialog by remember { mutableStateOf(false) }
    var targetRefillItem by remember { mutableStateOf<InventoryItemWithDetails?>(null) }

    val categories = productTypes.map { it.category }.distinct()

    val filteredItems = inventoryItems.filter { item ->
        val matchesStore = selectedStoreId == null || item.item.storeId == selectedStoreId
        val matchesCategory = selectedCategory == null || item.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                item.productName.contains(searchQuery, ignoreCase = true) ||
                item.storeName.contains(searchQuery, ignoreCase = true) ||
                item.brand.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)

        matchesStore && matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. FULL-WIDTH SEARCH BAR ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search product name, brand, category, store...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // --- 2 & 3. DROPDOWN FILTERS (STORES & CATEGORIES) & ACTION ROW ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Store Dropdown Filter
            var isStoreFilterExpanded by remember { mutableStateOf(false) }
            val selectedStoreName = stores.find { it.id == selectedStoreId }?.name ?: "All Stores"

            ExposedDropdownMenuBox(
                expanded = isStoreFilterExpanded,
                onExpandedChange = { isStoreFilterExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedStoreName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Store Filter") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStoreFilterExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenu(
                    expanded = isStoreFilterExpanded,
                    onDismissRequest = { isStoreFilterExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Stores / Warehouses", fontWeight = if (selectedStoreId == null) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            selectedStoreId = null
                            isStoreFilterExpanded = false
                        }
                    )
                    stores.forEach { store ->
                        DropdownMenuItem(
                            text = { Text(store.name, fontWeight = if (selectedStoreId == store.id) FontWeight.Bold else FontWeight.Normal) },
                            onClick = {
                                selectedStoreId = store.id
                                isStoreFilterExpanded = false
                            }
                        )
                    }
                }
            }

            // Category Dropdown Filter
            var isCategoryFilterExpanded by remember { mutableStateOf(false) }
            val selectedCategoryName = selectedCategory ?: "All Categories"

            ExposedDropdownMenuBox(
                expanded = isCategoryFilterExpanded,
                onExpandedChange = { isCategoryFilterExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category Filter") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryFilterExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenu(
                    expanded = isCategoryFilterExpanded,
                    onDismissRequest = { isCategoryFilterExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Categories", fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal) },
                        onClick = {
                            selectedCategory = null
                            isCategoryFilterExpanded = false
                        }
                    )
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) },
                            onClick = {
                                selectedCategory = cat
                                isCategoryFilterExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // --- GLOBAL REFILL ACTION BANNER ---
        Button(
            onClick = {
                targetRefillItem = null
                showRefillDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record General Stock Restock / Refill", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        // --- INVENTORY LIST ---
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matching inventory items found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems, key = { it.item.id }) { item ->
                    InventoryItemCard(
                        item = item,
                        onQuantityChanged = { newQty -> onUpdateQuantity(item.item.id, newQty) },
                        onCaptureImage = { onIncrementImageCaptureCount(item.item.id) },
                        onOpenRefillDialog = {
                            targetRefillItem = item
                            showRefillDialog = true
                        }
                    )
                }
            }
        }
    }

    // --- REDESIGNED REFILL STOCK DIALOG ---
    if (showRefillDialog) {
        StockRefillDialog(
            preselectedItem = targetRefillItem,
            stores = stores,
            productTypes = productTypes,
            onDismiss = { showRefillDialog = false },
            onConfirmRefill = { storeId, productId, qty, notes ->
                onRecordRefill(storeId, productId, qty, notes)
                showRefillDialog = false
            }
        )
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItemWithDetails,
    onQuantityChanged: (Int) -> Unit,
    onCaptureImage: () -> Unit,
    onOpenRefillDialog: () -> Unit
) {
    val context = LocalContext.current

    val (statusText, statusBgColor, statusTextColor) = when {
        item.item.quantity == 0 -> Triple("Out of Stock", StockAlertRed, Color.White)
        item.isLowStock -> Triple("Low Stock Alert", StockWarningAmber, Color.White)
        else -> Triple("In Stock", StockSuccessGreen, Color.White)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Brand Badge, Product Title, & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BrandBadge(brand = item.brand, unitSize = item.unitSize)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${item.storeName} • ${item.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBgColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusTextColor,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Value Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Unit Price: ${item.formattedUnitPrice}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total Value: ${item.formattedTotalValue}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- DIFFERENTIATED CONTROLS: 1) QUICK ADJUSTMENT vs 2) REFILL vs 3) AUDIT PHOTOS ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Control 1: Quick Physical Count Adjustment
                    Column {
                        Text(
                            text = "Quick Adjustment (+/-)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onQuantityChanged(item.item.quantity - 1) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.error)
                            }

                            Text(
                                text = "${item.item.quantity} Units",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = { onQuantityChanged(item.item.quantity + 1) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Control 2: Formal Stock Arrival / Refill Button (Distinct Primary Blue Styling)
                    Button(
                        onClick = onOpenRefillDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Refill Stock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Control 3: Clarified Audit Photo Captures Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            Toast.makeText(
                                context,
                                "Audit Camera: Logged ${item.item.imageCaptureCount} physical stock verification photos for audit tracking.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Audit Verification Photos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            onCaptureImage()
                            Toast.makeText(
                                context,
                                "Photo capture logged for audit trail (${item.item.imageCaptureCount + 1} total photos)",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Audit Camera", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("📷 ${item.item.imageCaptureCount} Photos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Redesigned & Modern Stock Refill Dialog for recording incoming shipment arrivals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockRefillDialog(
    preselectedItem: InventoryItemWithDetails?,
    stores: List<Store>,
    productTypes: List<ProductType>,
    onDismiss: () -> Unit,
    onConfirmRefill: (storeId: String, productId: String, qty: Int, notes: String) -> Unit
) {
    var selectedStoreId by remember { mutableStateOf(preselectedItem?.item?.storeId ?: stores.firstOrNull()?.id ?: "") }
    var selectedProductId by remember { mutableStateOf(preselectedItem?.item?.productTypeId ?: productTypes.firstOrNull()?.id ?: "") }
    var refillQuantity by remember { mutableIntStateOf(10) }
    var notesInput by remember { mutableStateOf("") }

    var isStoreDropdownExpanded by remember { mutableStateOf(false) }
    var isProductDropdownExpanded by remember { mutableStateOf(false) }

    val selectedStore = stores.find { it.id == selectedStoreId } ?: stores.firstOrNull()
    val selectedProduct = productTypes.find { it.id == selectedProductId } ?: productTypes.firstOrNull()

    val currentQty = preselectedItem?.item?.quantity ?: 0
    val newTotalQty = currentQty + refillQuantity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1565C0).copy(alpha = 0.12f))
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Stock Arrival & Restock", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Record incoming inventory shipment", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pre-selected Item Product Header (if opened from item card)
                selectedProduct?.let { prod ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            BrandBadge(brand = prod.brand, unitSize = prod.unitSize)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(prod.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "Unit Price: ${prod.formattedPrice}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Store Location Dropdown
                ExposedDropdownMenuBox(
                    expanded = isStoreDropdownExpanded,
                    onExpandedChange = { isStoreDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedStore?.name ?: "Select Store",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Destination Store Location") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStoreDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = isStoreDropdownExpanded,
                        onDismissRequest = { isStoreDropdownExpanded = false }
                    ) {
                        stores.forEach { store ->
                            DropdownMenuItem(
                                text = { Text(store.name) },
                                onClick = {
                                    selectedStoreId = store.id
                                    isStoreDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Product Dropdown (if pre-selection is null)
                if (preselectedItem == null) {
                    ExposedDropdownMenuBox(
                        expanded = isProductDropdownExpanded,
                        onExpandedChange = { isProductDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedProduct?.let { "${it.name} • ${it.unitSize}" } ?: "Select Product",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Product Item") },
                            leadingIcon = selectedProduct?.let { prod ->
                                { BrandBadge(brand = prod.brand) }
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isProductDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = isProductDropdownExpanded,
                            onDismissRequest = { isProductDropdownExpanded = false }
                        ) {
                            productTypes.forEach { prod ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            BrandBadge(brand = prod.brand, unitSize = prod.unitSize)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(prod.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    },
                                    onClick = {
                                        selectedProductId = prod.id
                                        isProductDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // --- REFILL QUANTITY SELECTOR & QUICK PRESETS ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0).copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Refill Quantity Units", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (refillQuantity > 1) refillQuantity-- },
                                    modifier = Modifier
                                        .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.error)
                                }

                                Text(
                                    text = "$refillQuantity",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)),
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )

                                IconButton(
                                    onClick = { refillQuantity++ },
                                    modifier = Modifier
                                        .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        // Quick Increment Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(5, 10, 50, 100).forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (refillQuantity == preset) Color(0xFF1565C0) else MaterialTheme.colorScheme.surface)
                                        .clickable { refillQuantity = preset }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+$preset",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (refillQuantity == preset) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Stock Total Calculation Preview
                        if (preselectedItem != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Current: $currentQty units", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                Text("New Total: $newTotalQty units", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                            }
                        }
                    }
                }

                // Notes / Supplier info
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Supplier / Waybill Notes (Optional)") },
                    placeholder = { Text("e.g. Waybill #8402 from Koudijs Depot") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedStoreId.isNotBlank() && selectedProductId.isNotBlank()) {
                        onConfirmRefill(selectedStoreId, selectedProductId, refillQuantity, notesInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirm Stock Refill (+$refillQuantity Units)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
