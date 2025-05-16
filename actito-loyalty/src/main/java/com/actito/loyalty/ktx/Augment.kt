package com.actito.loyalty.ktx

import com.actito.Actito
import com.actito.loyalty.ActitoLoyalty
import com.actito.loyalty.internal.ActitoLoyaltyImpl

@Suppress("unused")
public fun Actito.loyalty(): ActitoLoyalty {
    return ActitoLoyaltyImpl
}

// region Intent actions

public val Actito.INTENT_ACTION_PASSBOOK_OPENED: String
    get() = "com.actito.intent.action.PassbookOpened"

// endregion

// region Intent extras

public val Actito.INTENT_EXTRA_PASSBOOK: String
    get() = "com.actito.intent.extra.Passbook"

// endregion
