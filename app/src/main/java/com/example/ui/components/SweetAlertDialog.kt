package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LaoSecondary
import com.example.ui.theme.WarningOrange

enum class SweetAlertType {
    SUCCESS,
    WARNING,
    CRITICAL,
    CONFIRM,
    ERROR
}

data class SweetAlertState(
    val isVisible: Boolean = false,
    val type: SweetAlertType = SweetAlertType.SUCCESS,
    val title: String = "",
    val message: String = "",
    val confirmText: String = "ຕົກລົງ (OK)",
    val cancelText: String = "ຍົກເລີກ (Cancel)",
    val showCancel: Boolean = false,
    val onConfirm: () -> Unit = {},
    val onCancel: () -> Unit = {}
)

@Composable
fun SweetAlertDialog(
    state: SweetAlertState,
    onDismissRequest: () -> Unit
) {
    if (!state.isVisible) return

    val iconColor = when (state.type) {
        SweetAlertType.SUCCESS -> IncomeGreen
        SweetAlertType.WARNING -> WarningOrange
        SweetAlertType.CRITICAL, SweetAlertType.ERROR -> CriticalRed
        SweetAlertType.CONFIRM -> LaoSecondary
    }

    val icon = when (state.type) {
        SweetAlertType.SUCCESS -> Icons.Default.Check
        SweetAlertType.WARNING -> Icons.Default.Warning
        SweetAlertType.CRITICAL -> Icons.Default.PriorityHigh
        SweetAlertType.CONFIRM -> Icons.Default.HelpOutline
        SweetAlertType.ERROR -> Icons.Default.Close
    }

    Dialog(onDismissRequest = onDismissRequest) {
        AnimatedVisibility(
            visible = state.isVisible,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)),
            exit = scaleOut()
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("sweet_alert_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circular Icon with soft glow
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(iconColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(iconColor.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = state.title,
                                tint = iconColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    if (state.message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.showCancel) {
                            OutlinedButton(
                                onClick = {
                                    state.onCancel()
                                    onDismissRequest()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("sweet_alert_cancel"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(state.cancelText)
                            }
                        }

                        Button(
                            onClick = {
                                state.onConfirm()
                                onDismissRequest()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("sweet_alert_confirm"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.type == SweetAlertType.CRITICAL || state.type == SweetAlertType.ERROR) CriticalRed else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(state.confirmText, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
