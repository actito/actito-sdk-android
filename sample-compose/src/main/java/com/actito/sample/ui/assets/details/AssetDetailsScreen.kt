package com.actito.sample.ui.assets.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.actito.assets.models.ActitoAsset
import com.actito.sample.R
import com.actito.sample.ui.assets.components.AssetImage
import com.actito.sample.ui.components.SampleColumnStatus
import com.actito.sample.ui.components.SampleHorizontalDivider
import com.actito.sample.ui.components.SampleScaffold

@Composable
fun AssetDetailsScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    asset: ActitoAsset,
) {
    SampleScaffold(
        title = stringResource(R.string.assets_asset_details),
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(
                    text = asset.title,
                    fontWeight = FontWeight.Bold,
                )

                AssetImage(
                    modifier = Modifier.size(128.dp),
                    asset = asset,
                )
            }

            SampleColumnStatus(
                label = stringResource(R.string.assets_asset_description),
                status = asset.description.toString(),
            )

            SampleColumnStatus(
                label = stringResource(R.string.assets_asset_key),
                status = asset.key.toString(),
            )

            SampleColumnStatus(
                label = stringResource(R.string.assets_asset_url),
                status = asset.url.toString(),
            )

            SampleHorizontalDivider()

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.assets_asset_button),
                    fontWeight = FontWeight.Bold,
                )

                SampleColumnStatus(
                    label = stringResource(R.string.assets_asset_button_label),
                    status = asset.button?.label.toString(),
                )

                SampleColumnStatus(
                    label = stringResource(R.string.assets_asset_button_action),
                    status = asset.button?.action.toString(),
                )
            }

            SampleHorizontalDivider()

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.assets_asset_meta_data),
                    fontWeight = FontWeight.Bold,
                )

                SampleColumnStatus(
                    label = stringResource(R.string.assets_asset_original_file_name),
                    status = asset.metaData?.originalFileName.toString(),
                )

                SampleColumnStatus(
                    label = stringResource(R.string.assets_asset_content_type),
                    status = asset.metaData?.contentType.toString(),
                )

                SampleColumnStatus(
                    label = stringResource(R.string.assets_asset_content_length),
                    status = asset.metaData?.contentLength.toString(),
                )
            }
        }
    }
}
