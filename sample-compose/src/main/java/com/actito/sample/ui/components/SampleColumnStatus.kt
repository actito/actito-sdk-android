package com.actito.sample.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SampleColumnStatus(
    label: String,
    status: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
        )

        Text(status)
    }
}

@Preview
@Composable
private fun SampleColumnStatusPreview() {
    SampleColumnStatus(
        label = "Label",
        status = "Status",
    )
}
