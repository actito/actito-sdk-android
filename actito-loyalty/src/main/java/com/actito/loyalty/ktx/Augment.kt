package com.actito.loyalty.ktx

import com.actito.Actito
import com.actito.loyalty.ActitoLoyalty

@Suppress("unused")
public fun Actito.loyalty(): ActitoLoyalty = ActitoLoyalty

// region Intent actions

public val Actito.INTENT_ACTION_PASSBOOK_OPENED: String
    get() = Actito.loyalty().INTENT_ACTION_PASSBOOK_OPENED

// endregion

// region Intent extras

public val Actito.INTENT_EXTRA_PASSBOOK: String
    get() = Actito.loyalty().INTENT_EXTRA_PASSBOOK

// endregion
