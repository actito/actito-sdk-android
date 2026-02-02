package com.actito.sample.ui.home.device

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowNavigation

@Composable
fun DeviceCard(
    modifier: Modifier = Modifier,
    onNavigateToDevice: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        SampleRowNavigation(
            icon = painterResource(R.drawable.ic_baseline_phone_android_24),
            text = "Device",
            onNavigate = onNavigateToDevice,
        )
    }
}
