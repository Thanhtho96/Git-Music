package com.tt.gitmusic.model

import com.google.gson.annotations.SerializedName

data class BranchItem(
        @SerializedName("commit")
        val commit: Commit,
        @SerializedName("name")
        val name: String,
        @SerializedName("protected")
        val `protected`: Boolean
)