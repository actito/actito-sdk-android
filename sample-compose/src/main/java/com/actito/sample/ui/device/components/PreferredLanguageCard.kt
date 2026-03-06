package com.actito.sample.ui.device.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader

private const val SAMPLE_PREFERRED_LANGUAGE = "nl-NL"

@Composable
fun PreferredLanguageCard(
    onClearPreferredLanguage: () -> Unit,
    onUpdatePreferredLanguage: (language: String) -> Unit,
) {
    val language = rememberTextFieldState()

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
                icon = painterResource(R.drawable.ic_baseline_text_fields_24),
                text = stringResource(R.string.device_preferred_language),
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                state = language,
                lineLimits = TextFieldLineLimits.SingleLine,
                placeholder = { Text(stringResource(R.string.device_language)) },
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = !language.text.isEmpty(),
                        onClick = { onUpdatePreferredLanguage(language.text.toString()) },
                    ) {
                        Text(stringResource(R.string.device_button_update))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onClearPreferredLanguage,
                    ) {
                        Text(stringResource(R.string.device_button_clear))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdatePreferredLanguage(SAMPLE_PREFERRED_LANGUAGE) },
                    ) {
                        Text(stringResource(R.string.device_button_sample_language))
                    }
                }
            }
        }
    }
}
