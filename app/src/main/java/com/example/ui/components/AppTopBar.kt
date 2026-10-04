package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LaoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    subtitle: String,
    unreadNotificationCount: Int,
    language: String,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick, modifier = Modifier.testTag("app_bar_menu_button")) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            // Global Search icon
            IconButton(onClick = onSearchClick, modifier = Modifier.testTag("app_bar_search_button")) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Language Toggle Chip
            Surface(
                onClick = onToggleLanguage,
                shape = RoundedCornerShape(12.dp),
                color = LaoPrimary.copy(alpha = 0.12f),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .testTag("app_bar_lang_toggle")
            ) {
                Text(
                    text = if (language == "LAO") "🇱🇦 ລາວ" else "🇺🇸 EN",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = LaoPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Notifications Bell with Badge
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("app_bar_notifications_button")
            ) {
                if (unreadNotificationCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text(if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}
