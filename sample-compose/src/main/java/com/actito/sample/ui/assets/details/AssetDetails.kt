package com.actito.sample.ui.assets.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.actito.assets.models.ActitoAsset
import com.actito.sample.ui.assets.components.AssetImageView
import com.actito.sample.ui.components.SampleColumnStatus
import com.actito.sample.ui.components.SampleScaffold

@Composable
fun AssetDetailsScreen(
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    asset: ActitoAsset,
) {
    SampleScaffold(
        title = "Asset Details",
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(
                    text = asset.title,
                    fontWeight = FontWeight.Bold,
                )

                AssetImageView(
                    modifier = Modifier.size(128.dp),
                    asset = asset,
                )
            }

            SampleColumnStatus(
                label = "Description",
                status = asset.description.toString(),
            )

            SampleColumnStatus(
                label = "Key",
                status = asset.key.toString(),
            )

            SampleColumnStatus(
                label = "URL",
                status = asset.url.toString(),
            )

            HorizontalDivider()

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Button",
                    fontWeight = FontWeight.Bold,
                )

                SampleColumnStatus(
                    label = "Label",
                    status = asset.button?.label.toString(),
                )

                SampleColumnStatus(
                    label = "Action",
                    status = asset.button?.action.toString(),
                )
            }

            HorizontalDivider()

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Meta Data",
                    fontWeight = FontWeight.Bold,
                )

                SampleColumnStatus(
                    label = "Original File Name",
                    status = asset.metaData?.originalFileName.toString(),
                )

                SampleColumnStatus(
                    label = "Content Type",
                    status = asset.metaData?.contentType.toString(),
                )

                SampleColumnStatus(
                    label = "Content Type",
                    status = asset.metaData?.contentLength.toString(),
                )
            }
        }
    }
}
