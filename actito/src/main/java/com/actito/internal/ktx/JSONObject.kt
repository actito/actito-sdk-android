package com.actito.internal.ktx

import com.actito.ActitoEventData
import com.actito.ActitoEventsComponent
import org.json.JSONObject

/**
 * Creates an [ActitoEventData] instance from this [JSONObject].
 *
 * **INTERNAL USE ONLY**
 *
 * This method is to only be used at framework-level integrations.
 *
 * This method deserializes the JSON string into an [ActitoEventData] object. If the JSON
 * cannot be parsed into a valid instance, an [IllegalArgumentException] will be thrown.
 *
 * @receiver The [JSONObject] containing the serialized [ActitoEventData].
 * @return A non-null [ActitoEventData] parsed from this [JSONObject].
 * @throws IllegalArgumentException if the JSON cannot be deserialized into [ActitoEventData].
 */
public fun JSONObject.toEventData(): ActitoEventData =
    requireNotNull(ActitoEventsComponent.dataAdapter.fromJson(this.toString()))
