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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tabaapps.ve_agrohub_app.data.model.AllowedUser
import com.tabaapps.ve_agrohub_app.ui.theme.StockAlertRed

/**
 * Admin User Whitelist & Role-Based Access Control (RBAC) Management Screen.
 *
 * Java Educational Note:
 * Analogous to a Java Admin Security Console for user access management.
 * Controls permissions (`ADMIN` vs `STAFF`) stored in database tables and synced to Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    allowedUsers: List<AllowedUser>,
    onAddAllowedUser: (email: String, role: String) -> Unit,
    onRemoveAllowedUser: (email: String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Authorize New Email")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Authorized Whitelist & RBAC Roles",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Store Managers (ADMIN) can grant or revoke system access and assign roles.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allowedUsers, key = { it.email }) { user ->
                    val isUserAdmin = user.role.equals("ADMIN", ignoreCase = true)
                    val roleBgColor = if (isUserAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    val roleTextColor = if (isUserAdmin) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    val roleIcon = if (isUserAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Badge

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (user.isSeedAdmin) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Seed Admin",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.height(16.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Added by: ${user.addedBy}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(roleBgColor)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            roleIcon,
                                            contentDescription = null,
                                            tint = roleTextColor,
                                            modifier = Modifier.height(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = user.role.uppercase(),
                                            color = roleTextColor,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                if (!user.isSeedAdmin) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { onRemoveAllowedUser(user.email) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Revoke Access", tint = StockAlertRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAllowedUserDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { email, role ->
                onAddAllowedUser(email, role)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddAllowedUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (email: String, role: String) -> Unit
) {
    var emailText by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("STAFF") } // "ADMIN" or "STAFF"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Authorize New Email Access", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = emailText,
                    onValueChange = { emailText = it },
                    label = { Text("User Google Email") },
                    placeholder = { Text("clerk@veagrohub.co.tz") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Assign RBAC Security Role:", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedRole == "STAFF",
                        onClick = { selectedRole = "STAFF" },
                        label = { Text("STAFF (Stock Taking)") }
                    )
                    FilterChip(
                        selected = selectedRole == "ADMIN",
                        onClick = { selectedRole = "ADMIN" },
                        label = { Text("ADMIN (Full Access)") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (emailText.isNotBlank()) {
                        onConfirm(emailText, selectedRole)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Grant Authorization")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
