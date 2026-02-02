package com.actito.sample.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
fun SampleRowHeader(
    icon: Painter,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
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
    }
}

@Preview
@Composable
fun SampleRowHeaderPreview() {
    SampleRowHeader(
        icon = painterResource(R.drawable.ic_baseline_notifications_active_24),
        text = "Launch Flow",
    )
}
