package com.actito.sample.ui.assets.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.actito.assets.models.ActitoAsset

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
            AssetImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                asset = asset,
            )

            Text(
                text = asset.title,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
        }
    }
}

@Preview
@Composable
private fun AssetOverviewPreview() {
    AssetOverview(
        asset = ActitoAsset(
            title = "Title",
            description = "Description",
            key = "Key",
            url = null,
            button = null,
            metaData = ActitoAsset.MetaData(
                originalFileName = "FIle Name",
                contentType = "text/css",
                contentLength = 999,
            ),
        ),
    )
}
