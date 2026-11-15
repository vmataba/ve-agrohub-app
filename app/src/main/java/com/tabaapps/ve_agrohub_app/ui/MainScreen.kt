package com.tabaapps.ve_agrohub_app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tabaapps.ve_agrohub_app.R
import com.tabaapps.ve_agrohub_app.data.model.AuthStatus
import com.tabaapps.ve_agrohub_app.data.model.UserRole
import com.tabaapps.ve_agrohub_app.ui.screens.AccessDeniedScreen
import com.tabaapps.ve_agrohub_app.ui.screens.AdminSettingsScreen
import com.tabaapps.ve_agrohub_app.ui.screens.AuthScreen
import com.tabaapps.ve_agrohub_app.ui.screens.DashboardScreen
import com.tabaapps.ve_agrohub_app.ui.screens.InventoryScreen
import com.tabaapps.ve_agrohub_app.ui.screens.RecordSalesScreen
import com.tabaapps.ve_agrohub_app.ui.screens.StockLogScreen
import kotlinx.coroutines.launch

/**
 * Main App Navigation Shell for ve-stock application with Side Bar (Drawer) and Bottom Navigation.
 *
 * Java Educational Note:
 * Analogous to a Java Swing `JFrame` or Android Activity containing a DrawerLayout + BottomNavigationView.
 * Reactive state collection from ViewModel is done via `viewModel.flow.collectAsState()`,
 * matching RxJava `subscribe()` or LiveData `observe()` in traditional Java Android applications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val authStatus by viewModel.authStatus.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val productTypes by viewModel.productTypes.collectAsState()
    val inventoryItems by viewModel.inventoryItems.collectAsState()
    val stockLogs by viewModel.stockLogs.collectAsState()
    val allowedUsers by viewModel.allowedUsers.collectAsState()
    val customerSuggestions by viewModel.customerSuggestions.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var currentScreen by remember { mutableStateOf(VeStockScreen.RECORD_SALES) }

    val isAdmin = currentUserRole == UserRole.ADMIN

    when (authStatus) {
        AuthStatus.UNAUTHENTICATED -> {
            AuthScreen(
                onSignInWithGoogle = { email -> viewModel.authRepository.verifyUserAccess(email) },
                onSelectDemoMode = { demoType -> viewModel.authRepository.setGuestDemoUser(demoType) }
            )
        }

        AuthStatus.ACCESS_DENIED -> {
            AccessDeniedScreen(
                userEmail = currentUserEmail,
                onRefreshAccess = {
                    currentUserEmail?.let { viewModel.authRepository.verifyUserAccess(it) }
                },
                onSignOutOrSwitch = { viewModel.authRepository.signOut() }
            )
        }

        AuthStatus.AUTHENTICATED -> {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Header
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_ve_agro_logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(id = R.string.app_name),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = currentUserEmail ?: "",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Role Badge
                            val badgeBg = if (isAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                            val badgeContent = if (isAdmin) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = badgeContent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Role: ${currentUserRole.name}",
                                        color = badgeContent,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Navigation & Management",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )

                            // Admin Config in Side Bar!
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Admin Config") },
                                label = { Text(if (isAdmin) "Admin Config" else "Catalog Settings") },
                                selected = currentScreen == VeStockScreen.ADMIN_SETTINGS,
                                onClick = {
                                    currentScreen = VeStockScreen.ADMIN_SETTINGS
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Record Sales") },
                                label = { Text("Record Sales") },
                                selected = currentScreen == VeStockScreen.RECORD_SALES,
                                onClick = {
                                    currentScreen = VeStockScreen.RECORD_SALES
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Dashboard") },
                                selected = currentScreen == VeStockScreen.DASHBOARD,
                                onClick = {
                                    currentScreen = VeStockScreen.DASHBOARD
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                                label = { Text("Inventory") },
                                selected = currentScreen == VeStockScreen.INVENTORY,
                                onClick = {
                                    currentScreen = VeStockScreen.INVENTORY
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Events") },
                                label = { Text("Events") },
                                selected = currentScreen == VeStockScreen.STOCK_LOGS,
                                onClick = {
                                    currentScreen = VeStockScreen.STOCK_LOGS
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            Spacer(modifier = Modifier.weight(1f))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            // Sync Action
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = MaterialTheme.colorScheme.primary) },
                                label = { Text("Sync Data") },
                                selected = false,
                                onClick = {
                                    viewModel.triggerSync()
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                            // Sign Out Action
                            NavigationDrawerItem(
                                icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.error) },
                                label = { Text("Sign Out", color = MaterialTheme.colorScheme.error) },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    viewModel.authRepository.signOut()
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Open Side Menu",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_ve_agro_logo),
                                        contentDescription = "Logo",
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = stringResource(id = R.string.app_name),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = currentUserEmail ?: "",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    }
                                }
                            },
                            actions = {
                                // Role Badge
                                val badgeBg = if (isAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                val badgeContent = if (isAdmin) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = badgeContent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currentUserRole.name,
                                            color = badgeContent,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Sync Action
                                IconButton(
                                    onClick = { viewModel.triggerSync() },
                                    enabled = !isSyncing
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Sync",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Sign Out
                                IconButton(onClick = { viewModel.authRepository.signOut() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = "Sign Out",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            // 1. DEDICATED RECORD SALES MENU ITEM
                            NavigationBarItem(
                                selected = currentScreen == VeStockScreen.RECORD_SALES,
                                onClick = { currentScreen = VeStockScreen.RECORD_SALES },
                                icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Record Sales") },
                                label = { Text("Record Sales", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )

                            // 2. DASHBOARD SECOND
                            NavigationBarItem(
                                selected = currentScreen == VeStockScreen.DASHBOARD,
                                onClick = { currentScreen = VeStockScreen.DASHBOARD },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                label = { Text("Dashboard", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )

                            // 3. INVENTORY
                            NavigationBarItem(
                                selected = currentScreen == VeStockScreen.INVENTORY,
                                onClick = { currentScreen = VeStockScreen.INVENTORY },
                                icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                                label = { Text("Inventory", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )

                            // 4. EVENTS
                            NavigationBarItem(
                                selected = currentScreen == VeStockScreen.STOCK_LOGS,
                                onClick = { currentScreen = VeStockScreen.STOCK_LOGS },
                                icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "System Events") },
                                label = { Text("Events", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                            },
                            label = "ScreenTransition"
                        ) { targetScreen ->
                            when (targetScreen) {
                                VeStockScreen.RECORD_SALES -> RecordSalesScreen(
                                    stores = stores,
                                    productTypes = productTypes,
                                    inventoryItems = inventoryItems,
                                    stockLogs = stockLogs,
                                    customerSuggestions = customerSuggestions,
                                    onRecordTransaction = { storeId, productId, type, delta, customerName, notes ->
                                        viewModel.recordTransaction(storeId, productId, type, delta, customerName, notes)
                                    }
                                )

                                VeStockScreen.DASHBOARD -> DashboardScreen(
                                    stores = stores,
                                    productTypes = productTypes,
                                    inventoryItems = inventoryItems,
                                    customerSuggestions = customerSuggestions,
                                    onRecordTransaction = { storeId, productId, type, delta, customerName, notes ->
                                        viewModel.recordTransaction(storeId, productId, type, delta, customerName, notes)
                                    }
                                )

                                VeStockScreen.INVENTORY -> InventoryScreen(
                                    stores = stores,
                                    productTypes = productTypes,
                                    inventoryItems = inventoryItems,
                                    onUpdateQuantity = { itemId, newQty ->
                                        viewModel.updateInventoryQuantity(itemId, newQty)
                                    },
                                    onIncrementImageCaptureCount = { itemId ->
                                        viewModel.incrementImageCaptureCount(itemId)
                                    },
                                    onRecordRefill = { storeId, productId, qty, notes ->
                                        viewModel.recordTransaction(storeId, productId, "REFILL", qty, "", notes)
                                    }
                                )

                                VeStockScreen.STOCK_LOGS -> StockLogScreen(
                                    stockLogs = stockLogs
                                )

                                VeStockScreen.ADMIN_SETTINGS -> AdminSettingsScreen(
                                    isAdmin = isAdmin,
                                    allowedUsers = allowedUsers,
                                    stores = stores,
                                    productTypes = productTypes,
                                    onAddAllowedUser = { email, role ->
                                        viewModel.addAllowedUser(email, role)
                                    },
                                    onRemoveAllowedUser = { email ->
                                        viewModel.removeAllowedUser(email)
                                    },
                                    onAddStore = { name, location ->
                                        viewModel.addStore(name, location)
                                    },
                                    onDeleteStore = { id ->
                                        viewModel.deleteStore(id)
                                    },
                                    onAddProductType = { name, cat, price, brand, unitSize ->
                                        viewModel.addProductType(name, cat, price, brand, unitSize)
                                    },
                                    onDeleteProductType = { id ->
                                        viewModel.deleteProductType(id)
                                    }
                                )

                                else -> RecordSalesScreen(
                                    stores = stores,
                                    productTypes = productTypes,
                                    inventoryItems = inventoryItems,
                                    stockLogs = stockLogs,
                                    customerSuggestions = customerSuggestions,
                                    onRecordTransaction = { storeId, productId, type, delta, customerName, notes ->
                                        viewModel.recordTransaction(storeId, productId, type, delta, customerName, notes)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
