package com.actito.sample.ui.home.info

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowNavigation

@Composable
fun ApplicationInfoCard(
    onNavigateToApplicationInfo: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        SampleRowNavigation(
            icon = painterResource(R.drawable.ic_baseline_info_24),
            text = stringResource(R.string.application_title),
            onNavigate = onNavigateToApplicationInfo,
        )
    }
}

@Preview
@Composable
private fun ApplicationInfoCardPreview() {
    ApplicationInfoCard {}
}
