package com.actito.sample.ui.application.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.actito.models.ActitoApplication
import com.actito.sample.R
import com.actito.sample.ui.components.SampleRowHeader
import com.actito.sample.ui.components.SampleRowStatus

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
            SampleRowHeader(
                icon = painterResource(R.drawable.ic_baseline_info_24),
                text = stringResource(R.string.application_action_categories),
            )

            actionCategories.forEachIndexed { index, actionCategory ->
                Box {
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

                    Text(
                        text = stringResource(R.string.application_action_category_actions),
                        fontWeight = FontWeight.Bold,
                    )

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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Text(
                                text = stringResource(R.string.action_icon),
                                fontWeight = FontWeight.Bold,
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Column {
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
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}
