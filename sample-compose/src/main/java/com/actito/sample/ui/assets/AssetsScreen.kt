package com.actito.sample.ui.assets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.actito.sample.ui.assets.components.AssetOverview
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleScaffold

@Composable
fun AssetsScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNavigateToAssetDetails: (asset: ActitoAsset) -> Unit,
    viewModel: AssetsViewModel = viewModel(),
) {
    val assetsGroup = rememberTextFieldState()
    val assets by viewModel.assets.collectAsState()

    SampleScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.assets_title),
    ) { innerPadding ->
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
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
                            placeholder = { Text(stringResource(R.string.assets_asset_group)) },
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
                                Text(stringResource(R.string.button_search))
                            }
                        }
                    }
                }
            }

            items(assets) { asset ->
                AssetOverview(
                    asset = asset,
                    onNavigateToAssetDetails = onNavigateToAssetDetails,
                )
            }
        }
    }
}
