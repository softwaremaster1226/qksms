package com.android.QKSMS.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuickMessage(
        @PrimaryKey var id: Long = 0,
        var messageQ: String = ""
) : RealmObject()