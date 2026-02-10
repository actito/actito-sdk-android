package com.actito.sample.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.actito.sample.R
import com.actito.sample.core.SampleSnackbarType
import com.actito.sample.core.SampleSnackbarVisuals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleScaffold(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: (() -> Unit)? = null,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val visuals = data.visuals as? SampleSnackbarVisuals
                val type = visuals?.type
                val backgroundColor = when (type) {
                    SampleSnackbarType.INFO -> MaterialTheme.colorScheme.primary
                    SampleSnackbarType.ERROR -> MaterialTheme.colorScheme.error
                    null -> MaterialTheme.colorScheme.secondary
                }

                Snackbar(
                    snackbarData = data,
                    containerColor = backgroundColor,
                )
            }
        },

        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondary,
                ),
                navigationIcon = if (onNavigateBack != null) {
                    {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_back_24px),
                                contentDescription = null,
                            )
                        }
                    }
                } else {
                    {}
                },
                actions = actions,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding),
            content = content,
        )
    }
}
