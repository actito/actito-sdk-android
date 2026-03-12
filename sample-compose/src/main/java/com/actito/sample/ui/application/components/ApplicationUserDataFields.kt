package com.actito.sample.ui.application.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.models.ActitoApplication
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowStatus
import com.actito.sample.ui.components.SampleSectionHeaderWithCounter

@Composable
fun ApplicationUserDataFields(
    userDataFields: List<ActitoApplication.UserDataField>,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SampleSectionHeaderWithCounter(
            title = stringResource(R.string.application_user_data_fields),
            count = userDataFields.size,
        )

        if (userDataFields.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    userDataFields.forEachIndexed { index, field ->
                        Column {
                            SampleRowStatus(
                                label = stringResource(R.string.application_user_data_field_type),
                                status = field.type,
                            )

                            SampleRowStatus(
                                label = stringResource(R.string.application_user_data_field_key),
                                status = field.key,
                            )

                            SampleRowStatus(
                                label = stringResource(R.string.application_user_data_field_label),
                                status = field.label,
                            )
                        }

                        if (index != userDataFields.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
