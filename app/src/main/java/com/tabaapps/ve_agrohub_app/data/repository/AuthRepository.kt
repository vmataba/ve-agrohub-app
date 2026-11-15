package com.tabaapps.ve_agrohub_app.data.repository

import android.content.Context
import com.tabaapps.ve_agrohub_app.data.local.dao.AllowedUserDao
import com.tabaapps.ve_agrohub_app.data.local.entity.AllowedUserEntity
import com.tabaapps.ve_agrohub_app.data.model.AuthStatus
import com.tabaapps.ve_agrohub_app.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 3-Tier Security & Whitelist Authentication Repository.
 *
 * Tier 1: Hardcoded / Configured Seed Admin Whitelist (`admin_whitelist.json` in assets) -> Role: ADMIN
 * Tier 2: Dynamic Whitelist in Room Database / Firestore (`AllowedUserDao`) -> Role: ADMIN or STAFF
 * Tier 3: Unauthorized Email -> `AuthStatus.ACCESS_DENIED` ("Access Pending")
 *
 * Java Educational Note:
 * In Java Spring Security, this corresponds to a custom `AuthenticationProvider` and `UserDetailsService`.
 * `StateFlow<AuthStatus>` maintains the active user's reactive auth state, equivalent to Java's `LiveData<AuthStatus>`
 * or RxJava `BehaviorSubject<AuthStatus>`.
 */
class AuthRepository(
    private val context: Context,
    private val allowedUserDao: AllowedUserDao
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _currentUserEmail = MutableStateFlow<String?>("admin@veagrohub.co.tz")
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUserRole = MutableStateFlow(UserRole.ADMIN)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _authStatus = MutableStateFlow(AuthStatus.AUTHENTICATED)
    val authStatus: StateFlow<AuthStatus> = _authStatus.asStateFlow()

    private var seedAdminList: Set<String> = emptySet()

    init {
        loadSeedAdminWhitelist()
        // Default login as Demo Admin on app launch
        verifyUserAccess("admin@veagrohub.co.tz")
    }

    /**
     * Reads `admin_whitelist.json` from the Android app assets directory.
     *
     * Java Analogy:
     * Reads resources using `ClassLoader.getResourceAsStream("admin_whitelist.json")`
     * and parses JSON array using Jackson or Gson.
     */
    private fun loadSeedAdminWhitelist() {
        runCatching {
            context.assets.open("admin_whitelist.json").use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonText = reader.readText()
                val jsonArray = JSONArray(jsonText)
                val set = mutableSetOf<String>()
                for (i in 0 until jsonArray.length()) {
                    set.add(jsonArray.getString(i).trim().lowercase())
                }
                seedAdminList = set
            }
        }.onFailure {
            // Fallback seed admin list if file read fails
            seedAdminList = setOf("admin@veagrohub.co.tz", "manager@veagrohub.co.tz", "director@veagrohub.co.tz")
        }
    }

    /**
     * Executes 3-tier user email verification.
     *
     * Java Analogy:
     * public CompletableFuture<AuthStatus> verifyUserAccess(String email) { ... }
     */
    fun verifyUserAccess(rawEmail: String) {
        val email = rawEmail.trim().lowercase()
        _currentUserEmail.value = email

        scope.launch {
            // Tier 1: Check Seed Admin Whitelist
            if (seedAdminList.contains(email)) {
                _currentUserRole.value = UserRole.ADMIN
                _authStatus.value = AuthStatus.AUTHENTICATED

                // Ensure seed admin exists in local database
                allowedUserDao.insertAllowedUser(
                    AllowedUserEntity(
                        email = email,
                        role = "ADMIN",
                        addedBy = "SYSTEM",
                        isSeedAdmin = true
                    )
                )
                return@launch
            }

            // Tier 2: Check Dynamic Database Whitelist
            val allowedUser = allowedUserDao.getAllowedUserByEmail(email)
            if (allowedUser != null) {
                val role = if (allowedUser.role.equals("ADMIN", ignoreCase = true)) {
                    UserRole.ADMIN
                } else {
                    UserRole.STAFF
                }
                _currentUserRole.value = role
                _authStatus.value = AuthStatus.AUTHENTICATED
                return@launch
            }

            // Tier 3: Access Denied
            _currentUserRole.value = UserRole.NONE
            _authStatus.value = AuthStatus.ACCESS_DENIED
        }
    }

    /**
     * Toggles guest mode for instant UI demonstration and testing.
     */
    fun setGuestDemoUser(demoType: String) {
        when (demoType) {
            "ADMIN" -> verifyUserAccess("admin@veagrohub.co.tz")
            "STAFF" -> verifyUserAccess("staff@veagrohub.co.tz")
            "UNAUTHORIZED" -> verifyUserAccess("guest.unauthorized@farmer.co.tz")
        }
    }

    /**
     * Sign Out
     */
    fun signOut() {
        _currentUserEmail.value = null
        _currentUserRole.value = UserRole.NONE
        _authStatus.value = AuthStatus.UNAUTHENTICATED
    }

    /**
     * Adds a new authorized user email to the dynamic whitelist (ADMIN only).
     */
    suspend fun addAllowedUser(email: String, role: String, addedBy: String): Boolean {
        if (_currentUserRole.value != UserRole.ADMIN) return false
        val cleanEmail = email.trim().lowercase()
        allowedUserDao.insertAllowedUser(
            AllowedUserEntity(
                email = cleanEmail,
                role = role.uppercase(),
                addedBy = addedBy,
                isSeedAdmin = seedAdminList.contains(cleanEmail)
            )
        )
        return true
    }

    /**
     * Removes an authorized user email from the whitelist.
     */
    suspend fun removeAllowedUser(email: String): Boolean {
        if (_currentUserRole.value != UserRole.ADMIN) return false
        val cleanEmail = email.trim().lowercase()
        // Prevent deleting seed admins
        if (seedAdminList.contains(cleanEmail)) return false

        allowedUserDao.deleteAllowedUserByEmail(cleanEmail)
        return true
    }
}
