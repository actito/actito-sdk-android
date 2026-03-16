package com.actito.sample.ui.home.location.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.sample.R
import com.actito.sample.ui.components.SampleInfoChip

@Composable
fun BeaconsRowNavigation(
    rangedBeacons: Int,
    onNavigate: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigate)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_baseline_bluetooth_searching_24),
            contentDescription = null,
        )

        Spacer(Modifier.width(12.dp))

        Text(stringResource(R.string.location_beacons_title))

        Spacer(Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SampleInfoChip(
                label = stringResource(R.string.location_beacons_ranged),
                value = rangedBeacons.toString(),
            )

            Icon(
                painter = painterResource(R.drawable.ic_baseline_chevron_right_24),
                contentDescription = null,
            )
        }
    }
}
