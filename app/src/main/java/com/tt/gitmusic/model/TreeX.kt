package com.tt.gitmusic.model


import com.google.gson.annotations.SerializedName

data class TreeX(
    @SerializedName("mode")
    val mode: String,
    @SerializedName("path")
    val path: String,
    @SerializedName("sha")
    val sha: String,
    @SerializedName("size")
    val size: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("url")
    val url: String
)