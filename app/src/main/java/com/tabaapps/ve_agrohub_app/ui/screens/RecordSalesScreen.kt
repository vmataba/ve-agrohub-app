package com.tabaapps.ve_agrohub_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tabaapps.ve_agrohub_app.data.model.InventoryItemWithDetails
import com.tabaapps.ve_agrohub_app.data.model.ProductType
import com.tabaapps.ve_agrohub_app.data.model.StockLogWithDetails
import com.tabaapps.ve_agrohub_app.data.model.Store
import com.tabaapps.ve_agrohub_app.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Record Sales Screen - Dedicated interface for recording sales, credit sales, and stock transactions.
 */
@Composable
fun RecordSalesScreen(
    stores: List<Store>,
    productTypes: List<ProductType>,
    inventoryItems: List<InventoryItemWithDetails>,
    stockLogs: List<StockLogWithDetails> = emptyList(),
    customerSuggestions: List<String> = emptyList(),
    onRecordTransaction: (storeId: String, productId: String, type: String, delta: Int, customerName: String, notes: String) -> Unit
) {
    var transactionSuccessMessage by remember { mutableStateOf<String?>(null) }
    val dateFormat = remember { SimpleDateFormat("HH:mm, MMM dd", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. PROMINENT RECORD SALES CARD ---
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

        // --- 2. RECENT SALES / TRANSACTIONS SUMMARY ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Sales Activity",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "${stockLogs.take(5).size} recent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        val recentLogs = stockLogs.take(5)
        if (recentLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No sales recorded yet.",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(recentLogs) { item ->
                val log = item.log
                val isSale = log.type == "SALE"
                val isCredit = log.type == "PENDING_PAYMENT"

                val statusBg = when {
                    isSale -> Color(0xFFE8F5E9)
                    isCredit -> Color(0xFFFFF8E1)
                    else -> Color(0xFFE3F2FD)
                }
                val statusText = when {
                    isSale -> Color(0xFF2E7D32)
                    isCredit -> Color(0xFFF57F17)
                    else -> Color(0xFF1565C0)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.productName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val customerPart = if (log.customerName.isNotBlank()) " • ${log.customerName}" else ""
                            Text(
                                text = "${item.storeName}$customerPart",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .background(statusBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = log.type.replace("_", " "),
                                    color = statusText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyUtils.formatTzsAmount(log.totalTzsAmount),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
