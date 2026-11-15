package com.tabaapps.ve_agrohub_app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tabaapps.ve_agrohub_app.data.model.AllowedUser
import com.tabaapps.ve_agrohub_app.data.model.AuthStatus
import com.tabaapps.ve_agrohub_app.data.model.InventoryItemWithDetails
import com.tabaapps.ve_agrohub_app.data.model.ProductType
import com.tabaapps.ve_agrohub_app.data.model.StockLogWithDetails
import com.tabaapps.ve_agrohub_app.data.model.Store
import com.tabaapps.ve_agrohub_app.data.model.UserRole
import com.tabaapps.ve_agrohub_app.data.repository.AuthRepository
import com.tabaapps.ve_agrohub_app.data.repository.VeStockRepository
import com.tabaapps.ve_agrohub_app.data.sync.FirestoreSyncEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VeStockScreen {
    RECORD_SALES,
    DASHBOARD,
    INVENTORY,
    STOCK_LOGS,
    ADMIN_SETTINGS,
    AUTH,
    ACCESS_DENIED
}

/**
 * Main ViewModel for ve-stock application state management.
 *
 * Java Educational Note:
 * Android Architecture Components `ViewModel` survives configuration changes (such as device rotations).
 * In Java Android, ViewModels expose `LiveData<T>` or RxJava `BehaviorSubject<T>`.
 * In Kotlin Compose, `StateFlow<T>` created via `.stateIn(viewModelScope, ...)` provides lifecycle-aware,
 * hot reactive state streams to Composable UI functions.
 */
class MainViewModel(
    val authRepository: AuthRepository,
    val repository: VeStockRepository,
    val syncEngine: FirestoreSyncEngine
) : ViewModel() {

    val currentUserEmail: StateFlow<String?> = authRepository.currentUserEmail
    val currentUserRole: StateFlow<UserRole> = authRepository.currentUserRole
    val authStatus: StateFlow<AuthStatus> = authRepository.authStatus

    val stores: StateFlow<List<Store>> = repository.getStoresFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productTypes: StateFlow<List<ProductType>> = repository.getProductTypesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryItems: StateFlow<List<InventoryItemWithDetails>> = repository.getInventoryItemsWithDetailsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockLogs: StateFlow<List<StockLogWithDetails>> = repository.getStockLogsWithDetailsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allowedUsers: StateFlow<List<AllowedUser>> = repository.getAllowedUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerSuggestions: StateFlow<List<String>> = repository.getStockLogsWithDetailsFlow()
        .map { logs ->
            logs.map { it.log.customerName }
                .filter { it.isNotBlank() }
                .distinct()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSyncing: StateFlow<Boolean> = syncEngine.isSyncing

    fun triggerSync() {
        syncEngine.performFullSync()
    }

    // --- Actions ---
    fun updateInventoryQuantity(itemId: String, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateInventoryQuantity(itemId, newQuantity)
        }
    }

    fun incrementImageCaptureCount(itemId: String) {
        viewModelScope.launch {
            repository.incrementImageCaptureCount(itemId)
        }
    }

    fun recordTransaction(
        storeId: String,
        productTypeId: String,
        transactionType: String,
        quantityDelta: Int,
        customerName: String = "",
        notes: String = ""
    ) {
        val userEmail = currentUserEmail.value ?: "system@veagrohub.co.tz"
        viewModelScope.launch {
            repository.recordStockTransaction(
                storeId = storeId,
                productTypeId = productTypeId,
                transactionType = transactionType,
                quantityDelta = quantityDelta,
                userEmail = userEmail,
                customerName = customerName,
                notes = notes
            )
            triggerSync()
        }
    }

    fun addStore(name: String, location: String) {
        viewModelScope.launch {
            repository.addStore(name, location)
            triggerSync()
        }
    }

    fun deleteStore(storeId: String) {
        viewModelScope.launch {
            repository.deleteStore(storeId)
            triggerSync()
        }
    }

    fun addProductType(
        name: String,
        category: String,
        priceTzs: Long,
        brand: String = "General",
        unitSize: String = "50 kg Bag"
    ) {
        viewModelScope.launch {
            repository.addProductType(name, category, priceTzs, brand, unitSize)
            triggerSync()
        }
    }

    fun deleteProductType(productTypeId: String) {
        viewModelScope.launch {
            repository.deleteProductType(productTypeId)
            triggerSync()
        }
    }

    fun addAllowedUser(email: String, role: String) {
        viewModelScope.launch {
            val performer = currentUserEmail.value ?: "ADMIN"
            authRepository.addAllowedUser(email, role, performer)
            triggerSync()
        }
    }

    fun removeAllowedUser(email: String) {
        viewModelScope.launch {
            authRepository.removeAllowedUser(email)
            triggerSync()
        }
    }
}
