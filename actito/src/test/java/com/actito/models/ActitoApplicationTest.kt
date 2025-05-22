package com.actito.models

import com.actito.models.ActitoNotification.Action.Companion.TYPE_APP
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
public class ActitoApplicationTest {
    @Test
    public fun testApplicationSerialization() {
        val application = ActitoApplication(
            id = "testId",
            name = "testName",
            category = "testCategory",
            services = mapOf("testKey" to true),
            inboxConfig = ActitoApplication.InboxConfig(
                useInbox = true,
                useUserInbox = true,
                autoBadge = true,
            ),
            regionConfig = ActitoApplication.RegionConfig(
                proximityUUID = "testProximityUUID",
            ),
            userDataFields = listOf(
                ActitoApplication.UserDataField(
                    type = "testType",
                    key = "testKey",
                    label = "testLabel",
                ),
            ),
            actionCategories = listOf(
                ActitoApplication.ActionCategory(
                    type = "testType",
                    name = "testName",
                    description = "testDescription",
                    actions = listOf(
                        ActitoNotification.Action(
                            type = TYPE_APP,
                            label = "testLabel",
                            target = "",
                            camera = true,
                            keyboard = true,
                            destructive = true,
                            icon = ActitoNotification.Action.Icon(
                                android = "testAndroid",
                                ios = "testIos",
                                web = "testWeb",
                            ),
                        ),
                    ),
                ),
            ),
        )

        val convertedApplication = ActitoApplication.fromJson(application.toJson())

        assertEquals(application, convertedApplication)
    }

    @Test
    public fun testApplicationSerializationWithNullProps() {
        val application = ActitoApplication(
            id = "testId",
            name = "testName",
            category = "testCategory",
            services = mapOf("testValue" to true),
            inboxConfig = null,
            regionConfig = null,
            userDataFields = listOf(
                ActitoApplication.UserDataField(
                    type = "testType",
                    key = "testKey",
                    label = "testLabel",
                ),
            ),
            actionCategories = listOf(
                ActitoApplication.ActionCategory(
                    type = "testType",
                    name = "testName",
                    description = "testDescription",
                    actions = listOf(
                        ActitoNotification.Action(
                            type = TYPE_APP,
                            label = "testLabel",
                            target = "",
                            camera = true,
                            keyboard = true,
                            destructive = true,
                            icon = ActitoNotification.Action.Icon(
                                android = "testAndroid",
                                ios = "testIos",
                                web = "testWeb",
                            ),
                        ),
                    ),
                ),
            ),
        )

        val convertedApplication = ActitoApplication.fromJson(application.toJson())

        assertEquals(application, convertedApplication)
    }

    @Test
    public fun testInboxConfigSerialization() {
        val inboxConfig = ActitoApplication.InboxConfig(
            useInbox = true,
            useUserInbox = true,
            autoBadge = true,
        )

        val convertedConfig = ActitoApplication.InboxConfig.fromJson(inboxConfig.toJson())

        assertEquals(inboxConfig, convertedConfig)
    }

    @Test
    public fun testInboxConfigSerializationWithNoProps() {
        val inboxConfig = ActitoApplication.InboxConfig()

        val convertedConfig = ActitoApplication.InboxConfig.fromJson(inboxConfig.toJson())

        assertEquals(inboxConfig, convertedConfig)
    }

    @Test
    public fun testRegionConfigSerialization() {
        val regionConfig = ActitoApplication.RegionConfig(
            proximityUUID = "testProximityUUID",
        )

        val convertedRegionConfig = ActitoApplication.RegionConfig.fromJson(regionConfig.toJson())

        assertEquals(regionConfig, convertedRegionConfig)
    }

    @Test
    public fun testRegionConfigSerializationWithNullProps() {
        val regionConfig = ActitoApplication.RegionConfig(
            proximityUUID = null,
        )

        val convertedRegionConfig = ActitoApplication.RegionConfig.fromJson(regionConfig.toJson())

        assertEquals(regionConfig, convertedRegionConfig)
    }

    @Test
    public fun testUserDataFieldSerialization() {
        val userDataField = ActitoApplication.UserDataField(
            type = "testType",
            key = "testKey",
            label = "testLabel",
        )

        val convertedUserDataField = ActitoApplication.UserDataField.fromJson(userDataField.toJson())

        assertEquals(userDataField, convertedUserDataField)
    }

    @Test
    public fun testActionCategorySerialization() {
        val actionCategory = ActitoApplication.ActionCategory(
            type = "testType",
            name = "testName",
            description = "testDescription",
            actions = listOf(
                ActitoNotification.Action(
                    type = TYPE_APP,
                    label = "testLabel",
                    target = "",
                    camera = true,
                    keyboard = true,
                    destructive = true,
                    icon = ActitoNotification.Action.Icon(
                        android = "testAndroid",
                        ios = "testIos",
                        web = "testWeb",
                    ),
                ),
            ),
        )

        val convertedActionCategory = ActitoApplication.ActionCategory.fromJson(actionCategory.toJson())

        assertEquals(actionCategory, convertedActionCategory)
    }

    @Test
    public fun testActionCategorySerializationWithNullProps() {
        val actionCategory = ActitoApplication.ActionCategory(
            type = "testType",
            name = "testName",
            description = null,
            actions = listOf(
                ActitoNotification.Action(
                    type = TYPE_APP,
                    label = "testLabel",
                    target = "",
                    camera = true,
                    keyboard = true,
                    destructive = true,
                    icon = ActitoNotification.Action.Icon(
                        android = "testAndroid",
                        ios = "testIos",
                        web = "testWeb",
                    ),
                ),
            ),
        )

        val convertedActionCategory = ActitoApplication.ActionCategory.fromJson(actionCategory.toJson())

        assertEquals(actionCategory, convertedActionCategory)
    }
}
