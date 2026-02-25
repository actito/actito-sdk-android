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

@Composable
fun UserDataComponent(
    userData: Map<String, String>?,
    onUpdateUserData: (userData: Map<String, String?>) -> Unit,
) {
    val sampleUserData = mapOf("firstName" to "Sample Name", "lastName" to "Sample Last Name")
    val firstName = rememberTextFieldState()
    val lastName = rememberTextFieldState()

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
                icon = painterResource(R.drawable.user_attributes_24px),
                text = stringResource(R.string.device_user_data),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("firstName")

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = firstName,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    placeholder = { Text("Enter first name") },
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("lastName")

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    state = lastName,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    placeholder = { Text("Enter last name") },
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = !firstName.text.isEmpty() || !lastName.text.isEmpty(),
                        onClick = {
                            val data = mapOf(
                                "firstName" to firstName.text.toString(),
                                "lastName" to lastName.text.toString(),
                            ).filterValues { it.isNotBlank() }

                            onUpdateUserData(data)

                            firstName.clearText()
                            lastName.clearText()
                        },
                    ) {
                        Text(stringResource(R.string.device_button_update))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = userData?.get("lastName") != null,
                        onClick = {
                            val data = mapOf(
                                "firstName" to firstName.text.toString(),
                                "lastName" to null,
                            ).filterValues { it?.isNotBlank() != false }

                            onUpdateUserData(data)
                        },
                    ) {
                        Text(stringResource(R.string.device_button_remove_one))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { onUpdateUserData(sampleUserData) },
                    ) {
                        Text(stringResource(R.string.device_button_quick_update))
                    }
                }
            }
        }
    }
}
