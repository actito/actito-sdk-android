package com.actito.sample.ui.tags

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.actito.sample.R
import com.actito.sample.ui.components.SampleScaffold
import com.actito.sample.ui.tags.components.AdditionalTagsCard
import com.actito.sample.ui.tags.components.DeviceTagsCard

@Composable
fun TagsScreen(
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: TagsViewModel = viewModel(),
) {
    val tags by viewModel.tags.collectAsState()
    val defaultTags = remember {
        mutableStateListOf(
            "Android",
            "iOS",
            "Flutter",
            "React Native",
            "Capacitor",
            "Cordova",
            ".NET",
        ).filterNot { it in tags }
    }
    val selectedDeviceTags = remember { mutableStateListOf<String>() }
    val selectedAdditionalTags = remember { mutableStateListOf<String>() }

    SampleScaffold(
        title = "Device",
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = { viewModel.clearTags() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_baseline_delete_sweep_24),
                    contentDescription = null,
                )
            }
        },
    ) {
        Column {
            DeviceTagsCard(
                deviceTags = tags,
                selectedTags = selectedDeviceTags.toList(),
                onTagSelected = { tag ->
                    if (selectedDeviceTags.contains(tag)) {
                        selectedDeviceTags.remove(tag)
                    } else {
                        selectedDeviceTags.add(tag)
                    }
                },
                onRemoveTags = { tags ->
                    if (tags.size == 1) {
                        viewModel.removeTag(tags.first())
                    } else {
                        viewModel.removeTags(tags.toList())
                    }

                    selectedDeviceTags.clear()
                },
            )

            AdditionalTagsCard(
                defaultTags = defaultTags,
                selectedTags = selectedAdditionalTags.toList(),
                onTagSelected = { tag ->
                    if (selectedAdditionalTags.contains(tag)) {
                        selectedAdditionalTags.remove(tag)
                    } else {
                        selectedAdditionalTags.add(tag)
                    }
                },
                onAddTags = { tags ->
                    if (tags.size == 1) {
                        viewModel.addTag(tags.first())
                    } else {
                        viewModel.addTags(tags)
                    }

                    selectedAdditionalTags.clear()
                },
            )
        }
    }
}
