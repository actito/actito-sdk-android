package com.actito.sample.ui.assets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.assets.models.ActitoAsset
import com.actito.sample.R
import com.actito.sample.ui.assets.components.AssetImageView
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleScaffold

@Composable
fun AssetsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAssetDetails: (asset: ActitoAsset) -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: AssetsViewModel = viewModel(),
) {
    val assetsGroup = rememberTextFieldState()
    val assets by viewModel.assets.collectAsState()

    SampleScaffold(
        title = stringResource(R.string.assets_title),
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
    ) {
        Column {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SampleRowHeader(
                        icon = painterResource(R.drawable.ic_baseline_folder_24),
                        text = stringResource(R.string.assets_fetch_assets_title),
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = assetsGroup,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        placeholder = { Text(text = stringResource(R.string.assets_asset_group)) },
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            enabled = !assetsGroup.text.isEmpty(),
                            onClick = {
                                viewModel.fetchAssets(assetsGroup.text.toString())
                                assetsGroup.clearText()
                            },
                        ) {
                            Text(text = stringResource(R.string.button_search))
                        }
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(assets) { asset ->
                    AssetOverview(
                        asset = asset,
                        modifier = Modifier.clickable { onNavigateToAssetDetails(asset) },
                    )
                }
            }
        }
    }
}

@Composable
fun AssetOverview(
    asset: ActitoAsset,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column {
            AssetImageView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                asset = asset,
            )

            Text(
                text = asset.title,
                modifier = Modifier
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
        }
    }
}
