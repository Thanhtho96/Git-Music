package com.tt.gitmusic.model


import com.google.gson.annotations.SerializedName

data class FileInBranch(
    @SerializedName("sha")
    val sha: String,
    @SerializedName("tree")
    val tree: List<TreeX>,
    @SerializedName("truncated")
    val truncated: Boolean,
    @SerializedName("url")
    val url: String
)