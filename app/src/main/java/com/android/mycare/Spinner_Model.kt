package com.android.mycare

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Spinner_Model(
    var name: String? = "",
    var id: String? = ""
)