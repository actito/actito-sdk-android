package com.actito.sample.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
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
fun SampleSwitchRow(
    icon: Painter? = null,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = text,
            style = Typography.bodyLarge,
        )

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Preview
@Composable
fun SampleSwitchRowPreview() {
    SampleSwitchRow(
        icon = painterResource(R.drawable.ic_baseline_notifications_active_24),
        text = "Notifications",
        checked = true,
        onCheckedChange = {},
    )
}
