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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LaoPrimary
import com.example.ui.theme.LaoSecondary
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsState()

    var isLoginTab by remember { mutableStateOf(true) }
    var loginInput by remember { mutableStateOf("somsak") }
    var passwordInput by remember { mutableStateOf("password123") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Register fields
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regFullName by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }

    var forgotPasswordMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(LaoPrimary, LaoSecondary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("auth_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo & Header
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(LaoPrimary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Logo",
                        tint = LaoPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ລາຍຮັບ-ລາຍຈ່າຍ",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Personal Income & Expense Management",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Tabs: Login vs Register
                TabRow(
                    selectedTabIndex = if (isLoginTab) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = isLoginTab,
                        onClick = { isLoginTab = true; authViewModel.clearError() },
                        text = { Text("ເຂົ້າສູ່ລະບົບ (Login)", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_login")
                    )
                    Tab(
                        selected = !isLoginTab,
                        onClick = { isLoginTab = false; authViewModel.clearError() },
                        text = { Text("ລົງທະບຽນ (Register)", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("tab_register")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoginTab) {
                    // Login Form
                    OutlinedTextField(
                        value = loginInput,
                        onValueChange = { loginInput = it; authViewModel.clearError() },
                        label = { Text("ຊື່ຜູ້ໃຊ້ ຫຼື ອີເມວ (Username / Email)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it; authViewModel.clearError() },
                        label = { Text("ລະຫັດຜ່ານ (Password)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = LaoPrimary)
                            )
                            Text(
                                text = "ຈື່ຂ້ອຍໄວ້ (Remember)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        TextButton(
                            onClick = {
                                forgotPasswordMessage = "ລະຫັດທົດລອງເລີ່ມຕົ້ນ: somsak / password123\nທ່ານສາມາດເຂົ້າສູ່ລະບົບໄດ້ທັນທີ."
                            }
                        ) {
                            Text(
                                text = "ລືມລະຫັດ? (Forgot)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = LaoPrimary
                            )
                        }
                    }

                    if (forgotPasswordMessage != null) {
                        Text(
                            text = forgotPasswordMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = LaoPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { authViewModel.login(loginInput, passwordInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LaoPrimary)
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "ເຂົ້າສູ່ລະບົບ (Login)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // Register Form
                    OutlinedTextField(
                        value = regFullName,
                        onValueChange = { regFullName = it; authViewModel.clearError() },
                        label = { Text("ຊື່ ແລະ ນາມສະກຸນ (Full Name)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_fullname_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = regUsername,
                        onValueChange = { regUsername = it; authViewModel.clearError() },
                        label = { Text("ຊື່ຜູ້ໃຊ້ (Username)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_username_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it; authViewModel.clearError() },
                        label = { Text("ອີເມວ (Email)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it; authViewModel.clearError() },
                        label = { Text("ລະຫັດຜ່ານ (Password)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            authViewModel.register(
                                username = regUsername,
                                email = regEmail,
                                password = regPassword,
                                fullName = regFullName
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("register_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LaoPrimary)
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "ລົງທະບຽນໃໝ່ (Create Account)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                if (uiState is AuthUiState.Error) {
                    val err = uiState as AuthUiState.Error
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${err.messageLao} (${err.messageEng})",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
