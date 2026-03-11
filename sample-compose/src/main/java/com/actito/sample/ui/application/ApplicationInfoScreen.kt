package com.actito.sample.ui.application

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.actito.Actito
import com.actito.sample.R
import com.actito.sample.ui.application.components.ApplicationActionCategories
import com.actito.sample.ui.application.components.ApplicationBasicInfo
import com.actito.sample.ui.application.components.ApplicationInboxConfig
import com.actito.sample.ui.application.components.ApplicationRegionConfig
import com.actito.sample.ui.application.components.ApplicationServices
import com.actito.sample.ui.application.components.ApplicationUserDataFields
import com.actito.sample.ui.components.SampleScaffold

@Composable
fun ApplicationInfoScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
) {
    val application = requireNotNull(Actito.application)

    SampleScaffold(
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        title = stringResource(R.string.application_title),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            ApplicationBasicInfo(
                id = application.id,
                name = application.name,
                category = application.category,
                enforceSizeLimit = application.enforceSizeLimit,
                enforceTagRestrictions = application.enforceTagRestrictions,
                enforceEventNameRestrictions = application.enforceEventNameRestrictions,
            )

            ApplicationInboxConfig(
                inboxConfig = application.inboxConfig,
            )

            ApplicationRegionConfig(
                regionConfig = application.regionConfig,
            )

            ApplicationServices(
                services = application.services,
            )

            ApplicationUserDataFields(
                userDataFields = application.userDataFields,
            )

            ApplicationActionCategories(
                actionCategories = application.actionCategories,
            )
        }
    }
}
