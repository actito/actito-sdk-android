package com.actito.sample.ui.application.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.actito.models.ActitoApplication
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowStatus
import com.actito.sample.ui.theme.Typography

@Composable
fun ApplicationActionCategories(
    actionCategories: List<ActitoApplication.ActionCategory>,
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
            Text(
                text = stringResource(R.string.application_action_categories),
                style = Typography.bodyLarge,
            )

            actionCategories.forEachIndexed { index, actionCategory ->
                Column {
                    SampleRowStatus(
                        label = stringResource(R.string.application_action_category_type),
                        status = actionCategory.type,
                    )

                    SampleRowStatus(
                        label = stringResource(R.string.application_action_category_name),
                        status = actionCategory.name,
                    )

                    SampleRowStatus(
                        label = stringResource(R.string.application_action_category_description),
                        status = actionCategory.description.toString(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(stringResource(R.string.application_action_category_actions))

                    for (action in actionCategory.actions) {
                        SampleRowStatus(
                            label = stringResource(R.string.action_type),
                            status = action.type,
                        )

                        SampleRowStatus(
                            label = stringResource(R.string.action_label),
                            status = action.label,
                        )

                        SampleRowStatus(
                            label = stringResource(R.string.action_target),
                            status = action.target.toString(),
                        )

                        SampleRowStatus(
                            label = stringResource(R.string.action_camera),
                            status = action.camera.toString(),
                        )

                        SampleRowStatus(
                            label = stringResource(R.string.action_keyboard),
                            status = action.keyboard.toString(),
                        )

                        SampleRowStatus(
                            label = stringResource(R.string.action_destructive),
                            status = action.destructive.toString(),
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column {
                                Text(stringResource(R.string.action_icon))

                                Spacer(modifier = Modifier.weight(1f))

                                SampleRowStatus(
                                    label = stringResource(R.string.action_icon_android),
                                    status = action.icon?.android.toString(),
                                )

                                SampleRowStatus(
                                    label = stringResource(R.string.action_icon_ios),
                                    status = action.icon?.ios.toString(),
                                )

                                SampleRowStatus(
                                    label = stringResource(R.string.action_icon_web),
                                    status = action.icon?.web.toString(),
                                )
                            }
                        }
                    }
                }

                if (index != actionCategories.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
