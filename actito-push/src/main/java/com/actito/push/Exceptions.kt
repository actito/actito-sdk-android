@file:Suppress("detekt:MatchingDeclarationName")

package com.actito.push

@Suppress("detekt:MaxLineLength")
public class ActitoSubscriptionUnavailable : Exception(
    "Actito push subscription unavailable at the moment. It becomes available after calling enableRemoteNotifications().",
)
