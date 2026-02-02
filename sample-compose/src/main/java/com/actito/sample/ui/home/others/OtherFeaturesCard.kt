package com.actito.sample.ui.home.others

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleRowNavigation

@Composable
fun OtherFeaturesCard(
    onNavigateToAssets: () -> Unit,
    onNavigateToEvents: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            SampleRowHeader(
                icon = painterResource(R.drawable.ic_baseline_widgets_24),
                text = "Other Features",
            )
        }

        HorizontalDivider()

        SampleRowNavigation(
            icon = painterResource(R.drawable.ic_baseline_folder_24),
            text = "Assets",
            onNavigate = onNavigateToAssets,
        )

        HorizontalDivider()

        SampleRowNavigation(
            icon = painterResource(R.drawable.ic_baseline_event_24),
            text = "Events",
            onNavigate = onNavigateToEvents,
        )
    }
}
