package com.actito.sample.ui.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.inbox.models.ActitoInboxItem
import com.actito.sample.R
import com.actito.sample.ui.components.SampleScaffold
import com.actito.sample.ui.inbox.components.InboxItem
import com.actito.sample.utils.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: InboxViewModel = viewModel(),
) {
    val actitivy = LocalContext.current.findActivity
    val items by viewModel.items.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var longClickedItem by remember { mutableStateOf<ActitoInboxItem?>(null) }

    SampleScaffold(
        title = stringResource(R.string.inbox_title),
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_sync_24),
                    contentDescription = null,
                )
            }

            IconButton(onClick = { viewModel.markAllAsRead() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_mark_email_read_24),
                    contentDescription = null,
                )
            }

            IconButton(onClick = { viewModel.clearInbox() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_delete_sweep_24),
                    contentDescription = null,
                )
            }
        },
    ) {
        if (items.isEmpty()) {
            Column(modifier = Modifier.fillMaxHeight()) {
                Spacer(Modifier.weight(1f))

                Text(
                    text = stringResource(R.string.inbox_empty_message),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                )

                Spacer(Modifier.weight(1f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items) { item ->
                    InboxItem(
                        item = item,
                        onItemClick = {
                            viewModel.open(actitivy, item)
                        },
                        onItemLongClick = {
                            longClickedItem = item
                        },
                    )
                }
            }
        }

        longClickedItem?.let { item ->
            ModalBottomSheet(
                onDismissRequest = {
                    longClickedItem = null
                },
                sheetState = sheetState,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.open(actitivy, item)
                            longClickedItem = null
                        },
                    ) {
                        Text(stringResource(R.string.inbox_options_open))
                    }

                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.remove(item)
                            longClickedItem = null
                        },
                    ) {
                        Text(stringResource(R.string.inbox_options_remove))
                    }

                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.markAsRead(item)
                            longClickedItem = null
                        },
                    ) {
                        Text(stringResource(R.string.inbox_options_mark_as_read))
                    }
                }
            }
        }
    }
}
