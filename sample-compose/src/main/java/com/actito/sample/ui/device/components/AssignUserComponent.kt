package com.actito.sample.ui.device.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
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

private const val SAMPLE_USER_ID = "sample.user@actito.com"
private const val SAMPLE_USER_NAME = "Sample User"

@Composable
fun AssignUserComponent(
    onAssignDeviceToAnonymous: () -> Unit,
    onAssignDeviceToUser: (id: String, name: String) -> Unit,
) {
    val userId = rememberTextFieldState()
    val userName = rememberTextFieldState()

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
                text = stringResource(R.string.device_assign_user),
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                state = userId,
                lineLimits = TextFieldLineLimits.SingleLine,
                placeholder = { Text(stringResource(R.string.device_user_id)) },
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                state = userName,
                lineLimits = TextFieldLineLimits.SingleLine,
                placeholder = { Text(stringResource(R.string.device_user_name)) },
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
                        enabled = !userId.text.isEmpty() && !userName.text.isEmpty(),
                        onClick = {
                            onAssignDeviceToUser(userId.text.toString(), userName.text.toString())

                            userId.clearText()
                            userName.clearText()
                        },
                    ) {
                        Text(stringResource(R.string.button_register))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onAssignDeviceToAnonymous,
                    ) {
                        Text(stringResource(R.string.device_button_anonymous))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { onAssignDeviceToUser(SAMPLE_USER_ID, SAMPLE_USER_NAME) },
                    ) {
                        Text(stringResource(R.string.device_button_sample_user))
                    }
                }
            }
        }
    }
}
