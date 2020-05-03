package com.tt.gitmusic.model

import com.google.gson.annotations.SerializedName

data class Commit(
        @SerializedName("sha")
        val sha: String,
        @SerializedName("url")
        val url: String
)