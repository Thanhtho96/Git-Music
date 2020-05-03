package com.tt.gitmusic.model


import com.google.gson.annotations.SerializedName

data class Verification(
    @SerializedName("payload")
    val payload: Any,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("signature")
    val signature: Any,
    @SerializedName("verified")
    val verified: Boolean
)