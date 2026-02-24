package com.actito.sample.ui.application.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.models.ActitoApplication
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleRowStatus

@Composable
fun ApplicationUserDataFields(
    userDataFields: List<ActitoApplication.UserDataField>,
) {
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
                icon = painterResource(R.drawable.ic_baseline_info_24),
                text = stringResource(R.string.application_user_data_fields),
            )

            userDataFields.forEachIndexed { index, field ->
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

                if (index != userDataFields.lastIndex) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}
