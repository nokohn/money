package com.example.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserEntity
import com.example.ui.components.SweetAlertType
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.LaoPrimary
import com.example.ui.theme.LaoSecondary
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun SettingsScreen(
    user: UserEntity?,
    viewModel: FinanceViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency by viewModel.currency.collectAsState()
    val language by viewModel.language.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var oldPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var changePasswordError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (language == "LAO") "ການຕັ້ງຄ່າ ແລະ ໂປຣໄຟລ໌" else "Settings & Profile",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Profile Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(LaoPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = LaoPrimary, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = user?.fullName ?: "ສົມສັກ ໄຊຍະວົງ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = user?.email ?: "somsak@gmail.com",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "@${user?.username ?: "somsak"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LaoSecondary
                        )
                    }
                }
            }
        }

        // Language Section
        item {
            SettingsSectionCard(title = if (language == "LAO") "ພາສາ (Language)" else "Language", icon = Icons.Default.Language) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("LAO" to "🇱🇦 ພາສາລາວ", "ENG" to "🇺🇸 English").forEach { (code, label) ->
                        val isSel = language == code
                        Surface(
                            onClick = { viewModel.setLanguage(code) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) LaoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 10.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Currency Section
        item {
            SettingsSectionCard(title = if (language == "LAO") "ສະກຸນເງິນເລີ່ມຕົ້ນ (Currency)" else "Default Currency", icon = Icons.Default.Paid) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("LAK" to "ກີບ (₭)", "THB" to "ບາດ (฿)", "USD" to "ໂດລາ ($)").forEach { (code, label) ->
                        val isSel = currency == code
                        Surface(
                            onClick = { viewModel.setCurrency(code) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) LaoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 10.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Dark Mode Toggle
        item {
            SettingsSectionCard(title = if (language == "LAO") "ໂໝດມືດ (Dark Mode)" else "Dark Mode", icon = Icons.Default.DarkMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDarkMode) "ເປີດໃຊ້ງານໂໝດມືດແລ້ວ" else "ໃຊ້ງານໂໝດສະຫວ່າງ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = LaoPrimary)
                    )
                }
            }
        }

        // Security & Password
        item {
            SettingsSectionCard(title = if (language == "LAO") "ຄວາມປອດໄພ (Security)" else "Security", icon = Icons.Default.Security) {
                Button(
                    onClick = { showChangePasswordDialog = true },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == "LAO") "ປ່ຽນລະຫັດຜ່ານ (Change Password)" else "Change Password", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Logout Button
        item {
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("settings_logout_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CriticalRed.copy(alpha = 0.12f), contentColor = CriticalRed)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == "LAO") "ອອກຈາກລະບົບ (Logout)" else "Logout",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showChangePasswordDialog) {
        Dialog(onDismissRequest = { showChangePasswordDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == "LAO") "ປ່ຽນລະຫັດຜ່ານ" else "Change Password",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { showChangePasswordDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = oldPasswordInput,
                        onValueChange = { oldPasswordInput = it; changePasswordError = null },
                        label = { Text("ລະຫັດຜ່ານປັດຈຸບັນ (Old Password)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it; changePasswordError = null },
                        label = { Text("ລະຫັດຜ່ານໃໝ່ (New Password)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it; changePasswordError = null },
                        label = { Text("ຢືນຢັນລະຫັດຜ່ານໃໝ່ (Confirm)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (changePasswordError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = changePasswordError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (newPasswordInput.isBlank() || oldPasswordInput.isBlank()) {
                                changePasswordError = "ກະລຸນາປ້ອນຂໍ້ມູນໃຫ້ຄົບຖ້ວນ"
                                return@Button
                            }
                            if (newPasswordInput != confirmPasswordInput) {
                                changePasswordError = "ລະຫັດຜ່ານໃໝ່ບໍ່ກົງກັນ"
                                return@Button
                            }

                            authViewModel.changePassword(oldPasswordInput, newPasswordInput) { success ->
                                if (success) {
                                    showChangePasswordDialog = false
                                    viewModel.showSweetAlert(
                                        type = SweetAlertType.SUCCESS,
                                        title = if (language == "LAO") "ປ່ຽນລະຫັດສຳເລັດ" else "Password Changed",
                                        message = if (language == "LAO") "ລະຫັດຜ່ານຖືກປັບປຸງຮຽບຮ້ອຍແລ້ວ" else "Your password has been updated."
                                    )
                                } else {
                                    changePasswordError = "ລະຫັດຜ່ານເດີມບໍ່ຖືກຕ້ອງ"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LaoPrimary)
                    ) {
                        Text(if (language == "LAO") "ບັນທຶກລະຫັດໃໝ່" else "Update Password", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = LaoPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
