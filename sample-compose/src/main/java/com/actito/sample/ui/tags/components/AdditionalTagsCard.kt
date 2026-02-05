package com.actito.sample.ui.tags.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader

@Composable
fun AdditionalTagsCard(
    defaultTags: List<String>,
    selectedTags: List<String>,
    onTagSelected: (tag: String) -> Unit,
    onAddTags: (tags: List<String>) -> Unit,
) {
    val customTag = rememberTextFieldState()

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
                icon = painterResource(R.drawable.ic_baseline_add_24),
                text = stringResource(R.string.tags_quick_fill),
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp),
            ) {
                if (!defaultTags.isEmpty()) {
                    defaultTags.forEach { tag ->
                        AssistChip(
                            onClick = { onTagSelected(tag) },
                            label = { Text(tag) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selectedTags.contains(tag)) {
                                    Color.White
                                } else {
                                    Color.Unspecified
                                },
                            ),
                        )
                    }
                }
            }

            OutlinedTextField(
                state = customTag,
                lineLimits = TextFieldLineLimits.SingleLine,
                placeholder = { Text(stringResource(R.string.tags_manual_input)) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !selectedTags.isEmpty() || !customTag.text.isEmpty(),
                    onClick = {
                        val tags =
                            if (customTag.text.isEmpty()) selectedTags else selectedTags + customTag.text.toString()

                        onAddTags(tags)
                        customTag.clearText()
                    },
                ) {
                    Text(stringResource(R.string.button_add))
                }
            }
        }
    }
}
