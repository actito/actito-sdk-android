package com.actito.sample.ui.home.notifications.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.actito.sample.R
import com.actito.sample.ui.theme.Typography

@Composable
fun InboxRowNavigation(
    icon: Painter,
    text: String,
    badge: Int,
    onNavigate: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onNavigate)
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BadgedBox(
            badge = {
                Badge {
                    Text(
                        text = "$badge",
                    )
                }
            },
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = Typography.bodyLarge,
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.ic_baseline_chevron_right_24),
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun InboxRowNavigationPreview() {
    InboxRowNavigation(
        icon = painterResource(R.drawable.ic_baseline_inbox_24),
        text = "Inbox",
        badge = 99,
        onNavigate = {},
    )
}
