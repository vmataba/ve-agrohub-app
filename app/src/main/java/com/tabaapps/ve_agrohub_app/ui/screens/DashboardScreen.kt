package com.tabaapps.ve_agrohub_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tabaapps.ve_agrohub_app.data.model.InventoryItemWithDetails
import com.tabaapps.ve_agrohub_app.data.model.ProductType
import com.tabaapps.ve_agrohub_app.ui.components.BrandBadge
import com.tabaapps.ve_agrohub_app.data.model.Store
import com.tabaapps.ve_agrohub_app.ui.theme.StockAlertRed
import com.tabaapps.ve_agrohub_app.ui.theme.StockWarningAmber
import com.tabaapps.ve_agrohub_app.util.CurrencyUtils
import com.tabaapps.ve_agrohub_app.util.StringUtils

/**
 * Dashboard Screen for ve-stock app (Streamlined Operations Focus).
 *
 * Designed for extreme ease of use:
 * 1. Immediate Quick Transaction Card at top (Default: Quick Sale) with store & product dropdowns
 * 2. Smart customer tracking with auto-title-casing and autocomplete suggestions
 * 3. Clear distinction between Paid Sale, Credit / Pending Payment, and Stock Refill
 * 4. Overall Inventory TZS Value & Low Stock Alerts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    stores: List<Store>,
    productTypes: List<ProductType>,
    inventoryItems: List<InventoryItemWithDetails>,
    customerSuggestions: List<String> = emptyList(),
    onRecordTransaction: (storeId: String, productId: String, type: String, delta: Int, customerName: String, notes: String) -> Unit
) {
    var selectedStoreIdFilter by remember { mutableStateOf<String?>(null) } // null = All Stores
    var showFilterStoreDropdown by remember { mutableStateOf(false) }
    var transactionSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Filter items for header metrics
    val filteredItems = if (selectedStoreIdFilter == null) {
        inventoryItems
    } else {
        inventoryItems.filter { it.item.storeId == selectedStoreIdFilter }
    }

    val totalValueTzs = filteredItems.sumOf { it.totalValueTzs }
    val lowStockItems = filteredItems.filter { it.isLowStock }
    val selectedFilterStoreName = stores.find { it.id == selectedStoreIdFilter }?.name ?: "All Stores / Warehouses"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. PROMINENT QUICK TRANSACTION CARD (IMMEDIATE SALE OPTION) ---
        item {
            QuickTransactionCard(
                stores = stores,
                productTypes = productTypes,
                inventoryItems = inventoryItems,
                customerSuggestions = customerSuggestions,
                onSubmitTransaction = { storeId, productId, type, delta, customerName, notes ->
                    onRecordTransaction(storeId, productId, type, delta, customerName, notes)
                    val formattedCustomer = if (customerName.isNotBlank()) " for $customerName" else ""
                    val actionLabel = when (type) {
                        "SALE" -> "Paid Sale recorded successfully$formattedCustomer!"
                        "PENDING_PAYMENT" -> "Pending Credit Sale recorded$formattedCustomer!"
                        "REFILL" -> "Stock Refill recorded successfully!"
                        else -> "Transaction recorded!"
                    }
                    transactionSuccessMessage = actionLabel
                }
            )
        }

        // Success Confirmation Banner
        transactionSuccessMessage?.let { msg ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            color = Color(0xFF1B5E20),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // --- 2. ACTIVE STORE FILTER & INVENTORY VALUE HEADER ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Inventory Value (TZS)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        )

                        // Store Location Dropdown Filter
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                                    .clickable { showFilterStoreDropdown = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedFilterStoreName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            DropdownMenu(
                                expanded = showFilterStoreDropdown,
                                onDismissRequest = { showFilterStoreDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Stores / Warehouses", fontWeight = if (selectedStoreIdFilter == null) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        selectedStoreIdFilter = null
                                        showFilterStoreDropdown = false
                                    }
                                )
                                stores.forEach { store ->
                                    DropdownMenuItem(
                                        text = { Text(store.name, fontWeight = if (selectedStoreIdFilter == store.id) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            selectedStoreIdFilter = store.id
                                            showFilterStoreDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = CurrencyUtils.formatTzsAmount(totalValueTzs),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${filteredItems.size} Product Stock Records • Active: $selectedFilterStoreName",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    )
                }
            }
        }

        // --- 3. LOW STOCK WARNING ALERTS ---
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StockAlertRed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Low Stock Alerts (${lowStockItems.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StockAlertRed
                        )
                    )
                }
            }
        }

        if (lowStockItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "All stock levels are optimal above safety thresholds.",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(lowStockItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
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
                                .background(StockAlertRed)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${item.item.quantity} Left",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Ultra-Simple Quick Stock Action Card surfacing instant sales & refills with smart customer tracking.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTransactionCard(
    stores: List<Store>,
    productTypes: List<ProductType>,
    inventoryItems: List<InventoryItemWithDetails>,
    customerSuggestions: List<String>,
    onSubmitTransaction: (storeId: String, productId: String, type: String, delta: Int, customerName: String, notes: String) -> Unit
) {
    var transactionType by remember { mutableStateOf("SALE") } // "SALE", "PENDING_PAYMENT", "REFILL"

    var selectedStoreId by remember { mutableStateOf(stores.firstOrNull()?.id ?: "") }
    var selectedProductId by remember { mutableStateOf(productTypes.firstOrNull()?.id ?: "") }
    var quantity by remember { mutableIntStateOf(1) }
    var customerNameInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    var isTypeDropdownExpanded by remember { mutableStateOf(false) }
    var isStoreDropdownExpanded by remember { mutableStateOf(false) }
    var isProductDropdownExpanded by remember { mutableStateOf(false) }
    var isCustomerDropdownExpanded by remember { mutableStateOf(false) }

    val transactionTypeOptions = remember {
        listOf(
            Triple("SALE", "Paid Sale", Icons.Default.PointOfSale),
            Triple("PENDING_PAYMENT", "Pending Credit Sale", Icons.Default.Payment)
        )
    }

    val selectedTypeOption = transactionTypeOptions.find { it.first == transactionType } ?: transactionTypeOptions.first()

    val selectedStore = stores.find { it.id == selectedStoreId } ?: stores.firstOrNull()
    val selectedProduct = productTypes.find { it.id == selectedProductId } ?: productTypes.firstOrNull()

    val currentStockItem = inventoryItems.find {
        it.item.storeId == selectedStoreId && it.item.productTypeId == selectedProductId
    }
    val availableStockQty = currentStockItem?.item?.quantity ?: 0

    val unitPrice = selectedProduct?.priceTzs ?: 0L
    val totalAmountTzs = unitPrice * quantity

    // Filter customer suggestions based on input
    val filteredCustomerSuggestions = if (customerNameInput.isBlank()) {
        customerSuggestions
    } else {
        customerSuggestions.filter {
            it.contains(customerNameInput, ignoreCase = true)
        }
    }

    // Colors per transaction mode
    val (primaryColor, actionButtonLabel) = when (transactionType) {
        "SALE" -> Pair(Color(0xFF2E7D32), "Complete Paid Sale")
        "PENDING_PAYMENT" -> Pair(StockWarningAmber, "Save Pending Credit Sale")
        else -> Pair(MaterialTheme.colorScheme.primary, "Submit Sale")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = primaryColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = selectedTypeOption.third,
                        contentDescription = null,
                        tint = primaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quick Transaction / Sale",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Small Stock Available Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Available: $availableStockQty units",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = primaryColor
                    )
                }
            }

            // --- TRANSACTION TYPE DROPDOWN ---
            ExposedDropdownMenuBox(
                expanded = isTypeDropdownExpanded,
                onExpandedChange = { isTypeDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTypeOption.second,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Transaction Type") },
                    leadingIcon = {
                        Icon(
                            imageVector = selectedTypeOption.third,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenu(
                    expanded = isTypeDropdownExpanded,
                    onDismissRequest = { isTypeDropdownExpanded = false }
                ) {
                    transactionTypeOptions.forEach { (typeKey, label, icon) ->
                        val itemColor = when (typeKey) {
                            "SALE" -> Color(0xFF2E7D32)
                            "PENDING_PAYMENT" -> StockWarningAmber
                            "REFILL" -> Color(0xFF1565C0)
                            else -> MaterialTheme.colorScheme.primary
                        }
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = itemColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = label,
                                        fontWeight = if (typeKey == transactionType) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            onClick = {
                                transactionType = typeKey
                                isTypeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // --- STORE DROPDOWN ---
            ExposedDropdownMenuBox(
                expanded = isStoreDropdownExpanded,
                onExpandedChange = { isStoreDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedStore?.name ?: "Select Store Location",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Store Location") },
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
                            text = { Text(store.name, fontWeight = if (store.id == selectedStoreId) FontWeight.Bold else FontWeight.Normal) },
                            onClick = {
                                selectedStoreId = store.id
                                isStoreDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // --- PRODUCT DROPDOWN ---
            ExposedDropdownMenuBox(
                expanded = isProductDropdownExpanded,
                onExpandedChange = { isProductDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                val productDisplay = selectedProduct?.let {
                    "${it.name} • ${it.unitSize}"
                } ?: "Select Product Item"

                OutlinedTextField(
                    value = productDisplay,
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        BrandBadge(brand = prod.brand, unitSize = prod.unitSize)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = prod.name,
                                            fontWeight = if (prod.id == selectedProductId) FontWeight.Bold else FontWeight.Normal,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(prod.formattedPrice, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

            // --- QUANTITY + TOTAL PRICE ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Quantity Units", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }

                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )

                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                // Live Total Price Banner
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Price", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyUtils.formatTzsAmount(totalAmountTzs),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    )
                }
            }

            // --- SMART CUSTOMER NAME AUTOCOMPLETE FIELD ---
            ExposedDropdownMenuBox(
                expanded = isCustomerDropdownExpanded && filteredCustomerSuggestions.isNotEmpty(),
                onExpandedChange = { isCustomerDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = customerNameInput,
                    onValueChange = { input ->
                        customerNameInput = input
                        isCustomerDropdownExpanded = true
                    },
                    label = { Text("Customer Name (Optional)") },
                    placeholder = { Text("e.g. John Doe") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    trailingIcon = {
                        if (customerNameInput.isNotBlank()) {
                            Text(
                                text = "Auto-Title",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                ExposedDropdownMenu(
                    expanded = isCustomerDropdownExpanded && filteredCustomerSuggestions.isNotEmpty(),
                    onDismissRequest = { isCustomerDropdownExpanded = false }
                ) {
                    filteredCustomerSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                customerNameInput = StringUtils.toTitleCase(suggestion)
                                isCustomerDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Notes / Invoice Reference
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Notes / Invoice Ref (Optional)") },
                placeholder = { Text("e.g. Invoice #1042") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // --- SUBMIT TRANSACTION BUTTON ---
            Button(
                onClick = {
                    if (selectedStoreId.isNotBlank() && selectedProductId.isNotBlank()) {
                        val titleCasedCustomer = StringUtils.toTitleCase(customerNameInput)
                        val delta = -quantity

                        onSubmitTransaction(
                            selectedStoreId,
                            selectedProductId,
                            transactionType,
                            delta,
                            titleCasedCustomer,
                            notesInput
                        )

                        // Reset inputs
                        quantity = 1
                        customerNameInput = ""
                        notesInput = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = actionButtonLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
