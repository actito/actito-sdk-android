package com.actito.sample.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun SampleRowNavigation(
    icon: Painter,
    text: String,
    onNavigate: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onNavigate)
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
        )

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
private fun SampleRowNavigationPreview() {
    SampleRowNavigation(
        icon = painterResource(R.drawable.ic_baseline_inbox_24),
        text = "Inbox",
        onNavigate = {},
    )
}
