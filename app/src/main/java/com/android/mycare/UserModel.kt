package com.android.mycare

import com.google.firebase.database.IgnoreExtraProperties

// [START blog_user_class]
@IgnoreExtraProperties
data class UserModel(
    var username: String? = "",
    var uid: String? = "",
    var profileimageuri: String? = "",
    var role: Long? = 0
)