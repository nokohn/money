package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.LaoPrimary
import com.example.ui.theme.LaoSecondary

@Composable
fun AppNavigationDrawerContent(
    user: UserEntity?,
    currentScreen: AppScreen,
    unreadNotificationCount: Int,
    language: String = "LAO",
    onNavigate: (AppScreen) -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Profile Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(LaoPrimary, LaoSecondary)
                        )
                    )
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close drawer", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user?.fullName ?: "ຜູ້ໃຊ້ງານ (User)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = user?.email ?: "user@finance.la",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "ລາຍຮັບ-ລາຍຈ່າຍ • Lao Personal Finance",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Items (13 items requested)
            DrawerItemRow(
                icon = Icons.Default.Home,
                title = if (language == "LAO") "1. Dashboard (ໜ້າຫຼັກ)" else "1. Dashboard",
                isSelected = currentScreen == AppScreen.DASHBOARD,
                testTag = "drawer_nav_dashboard",
                onClick = { onNavigate(AppScreen.DASHBOARD); onClose() }
            )

            DrawerItemRow(
                icon = Icons.Default.TrendingUp,
                title = if (language == "LAO") "2. ລາຍຮັບ (Income)" else "2. Income",
                isSelected = currentScreen == AppScreen.INCOME,
                testTag = "drawer_nav_income",
                onClick = { onNavigate(AppScreen.INCOME); onClose() }
            )

            DrawerItemRow(
                icon = Icons.Default.TrendingDown,
                title = if (language == "LAO") "3. ລາຍຈ່າຍ (Expenses)" else "3. Expenses",
                isSelected = currentScreen == AppScreen.EXPENSES,
                testTag = "drawer_nav_expenses",
                onClick = { onNavigate(AppScreen.EXPENSES); onClose() }
            )

            DrawerItemRow(
                icon = Icons.Default.ReceiptLong,
                title = if (language == "LAO") "4. ປະຫວັດທຸລະກຳ (Transactions)" else "4. Transactions",
                isSelected = currentScreen == AppScreen.TRANSACTIONS,
                testTag = "drawer_nav_transactions",
                onClick = { onNavigate(AppScreen.TRANSACTIONS); onClose() }
            )

            DrawerItemRow(
                icon = Icons.Default.PieChart,
                title = if (language == "LAO") "5. ງົບປະມານ (Budget)" else "5. Budget",
                isSelected = currentScreen == AppScreen.BUDGET,
                testTag = "drawer_nav_budget",
                onClick = { onNavigate(AppScreen.BUDGET); onClose() }
            )

            DrawerItemRow(
                icon = Icons.Default.CalendarMonth,
                title = if (language == "LAO") "6. ປະຕິທິນ (Calendar)" else "6. Calendar",
                isSelected = currentScreen == AppScreen.CALENDAR,
                testTag = "drawer_nav_calendar",
                onClick = { onNavigate(AppScreen.CALENDAR); onClose() }
            )

            DrawerItemRow(
                icon = Icons.Default.Assessment,
                title = if (language == "LAO") "7. ລາຍງານ (Reports)" else "7. Reports",
                isSelected = currentScreen == AppScreen.REPORTS,
                testTag = "drawer_nav_reports",
                onClick = { onNavigate(AppScreen.REPORTS); onClose() }
            )

            DrawerItemRow(
                icon = Icons.Default.Category,
                title = if (language == "LAO") "8. ໝວດໝູ່ (Categories)" else "8. Categories",
                isSelected = currentScreen == AppScreen.CATEGORIES,
                testTag = "drawer_nav_categories",
                onClick = { onNavigate(AppScreen.CATEGORIES); onClose() }
            )

            DrawerItemRow(
                icon = Icons.Default.AccountBalance,
                title = if (language == "LAO") "9. ບັນຊີ / ກະເປົາ (Accounts)" else "9. Accounts / Wallets",
                isSelected = currentScreen == AppScreen.ACCOUNTS,
                testTag = "drawer_nav_accounts",
                onClick = { onNavigate(AppScreen.ACCOUNTS); onClose() }
            )

            DrawerItemRow(
                icon = Icons.Default.Notifications,
                title = if (language == "LAO") "10. ແຈ້ງເຕືອນ (Notifications)" else "10. Notifications",
                isSelected = currentScreen == AppScreen.NOTIFICATIONS,
                badge = if (unreadNotificationCount > 0) unreadNotificationCount.toString() else null,
                testTag = "drawer_nav_notifications",
                onClick = { onNavigate(AppScreen.NOTIFICATIONS); onClose() }
            )

            DrawerItemRow(
                icon = Icons.Default.Settings,
                title = if (language == "LAO") "11. ຕັ້ງຄ່າ (Settings)" else "11. Settings",
                isSelected = currentScreen == AppScreen.SETTINGS,
                testTag = "drawer_nav_settings",
                onClick = { onNavigate(AppScreen.SETTINGS); onClose() }
            )

            DrawerItemRow(
                icon = Icons.Default.Person,
                title = if (language == "LAO") "12. ໂປຣໄຟລ໌ (Profile)" else "12. Profile",
                isSelected = currentScreen == AppScreen.PROFILE,
                testTag = "drawer_nav_profile",
                onClick = { onNavigate(AppScreen.PROFILE); onClose() }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            DrawerItemRow(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = if (language == "LAO") "13. ອອກຈາກລະບົບ (Logout)" else "13. Logout",
                isSelected = false,
                textColor = CriticalRed,
                iconColor = CriticalRed,
                testTag = "drawer_nav_logout",
                onClick = { onClose(); onLogout() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerItemRow(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    testTag: String,
    badge: String? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = if (isSelected) LaoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp)) },
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) LaoPrimary else textColor
                )
                if (badge != null) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier
            .padding(NavigationDrawerItemDefaults.ItemPadding)
            .testTag(testTag),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = LaoPrimary.copy(alpha = 0.12f),
            unselectedContainerColor = Color.Transparent
        )
    )
}
