package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LaoPrimary

enum class AppScreen(
    val titleLao: String,
    val titleEng: String,
    val icon: ImageVector
) {
    DASHBOARD("ໜ້າຫຼັກ", "Dashboard", Icons.Default.Home),
    INCOME("ລາຍຮັບ", "Income", Icons.Default.TrendingUp),
    EXPENSES("ລາຍຈ່າຍ", "Expenses", Icons.Default.TrendingDown),
    TRANSACTIONS("ປະຫວັດທຸລະກຳ", "Transactions", Icons.Default.ReceiptLong),
    BUDGET("ງົບປະມານ", "Budget", Icons.Default.PieChart),
    CALENDAR("ປະຕິທິນ", "Calendar", Icons.Default.CalendarMonth),
    REPORTS("ລາຍງານ", "Reports", Icons.Default.Assessment),
    CATEGORIES("ໝວດໝູ່", "Categories", Icons.Default.Category),
    ACCOUNTS("ບັນຊີ / ກະເປົາ", "Accounts", Icons.Default.AccountBalance),
    NOTIFICATIONS("ແຈ້ງເຕືອນ", "Notifications", Icons.Default.Notifications),
    SETTINGS("ຕັ້ງຄ່າ", "Settings", Icons.Default.Settings),
    PROFILE("ໂປຣໄຟລ໌", "Profile", Icons.Default.Person)
}

@Composable
fun AppBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onFabClick: () -> Unit,
    unreadNotificationsCount: Int = 0,
    language: String = "LAO",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 1. Dashboard / Home
            BottomNavItem(
                icon = Icons.Default.Home,
                label = if (language == "LAO") "ໜ້າຫຼັກ" else "Home",
                isSelected = currentScreen == AppScreen.DASHBOARD,
                testTag = "nav_home",
                onClick = { onNavigate(AppScreen.DASHBOARD) }
            )

            // 2. Transactions
            BottomNavItem(
                icon = Icons.Default.ReceiptLong,
                label = if (language == "LAO") "ທຸລະກຳ" else "History",
                isSelected = currentScreen == AppScreen.TRANSACTIONS,
                testTag = "nav_transactions",
                onClick = { onNavigate(AppScreen.TRANSACTIONS) }
            )

            // 3. Central Prominent Plus Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .offset(y = (-6).dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = onFabClick,
                    shape = CircleShape,
                    containerColor = LaoPrimary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("nav_fab_plus")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // 4. Budget
            BottomNavItem(
                icon = Icons.Default.PieChart,
                label = if (language == "LAO") "ງົບປະມານ" else "Budget",
                isSelected = currentScreen == AppScreen.BUDGET,
                testTag = "nav_budget",
                onClick = { onNavigate(AppScreen.BUDGET) }
            )

            // 5. Profile / More
            BottomNavItem(
                icon = Icons.Default.Person,
                label = if (language == "LAO") "ໂປຣໄຟລ໌" else "Profile",
                isSelected = currentScreen == AppScreen.PROFILE || currentScreen == AppScreen.SETTINGS,
                testTag = "nav_profile",
                badgeCount = unreadNotificationsCount,
                onClick = { onNavigate(AppScreen.PROFILE) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    val activeColor = LaoPrimary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                        Text(text = if (badgeCount > 9) "9+" else badgeCount.toString())
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) activeColor else inactiveColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}
