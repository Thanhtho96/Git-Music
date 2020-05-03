package com.tt.gitmusic.model


import com.google.gson.annotations.SerializedName

data class Parent(
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("sha")
    val sha: String,
    @SerializedName("url")
    val url: String
)