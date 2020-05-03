package com.tt.gitmusic.model


import com.google.gson.annotations.SerializedName

data class Stats(
    @SerializedName("additions")
    val additions: Int,
    @SerializedName("deletions")
    val deletions: Int,
    @SerializedName("total")
    val total: Int
)