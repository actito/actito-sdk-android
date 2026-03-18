package com.actito.sample.ui.inbox.components

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.actito.inbox.models.ActitoInboxItem
import java.util.Calendar

@Composable
fun InboxItem(
    item: ActitoInboxItem,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
) {
    val title = item.notification.title
    val message = item.notification.message
    val opened = item.opened
    val attachment = item.notification.attachments.firstOrNull()?.uri
    val time = DateUtils.getRelativeTimeSpanString(
        item.time.time,
        Calendar.getInstance().timeInMillis,
        DateUtils.MINUTE_IN_MILLIS,
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onItemClick() },
                onLongClick = { onItemLongClick() },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (attachment.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                )
            } else {
                AsyncImage(
                    model = attachment,
                    contentDescription = null,
                    modifier = Modifier
                        .width(96.dp)
                        .height(64.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge +
                            TextStyle(fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = time.toString(),
                    style = MaterialTheme.typography.bodySmall +
                        TextStyle(fontWeight = FontWeight.Light),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (!opened) {
                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(8.dp)
                            .clip(ButtonDefaults.shape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
    }
}
