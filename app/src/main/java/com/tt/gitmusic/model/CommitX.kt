package com.tt.gitmusic.model


import com.google.gson.annotations.SerializedName

data class CommitX(
    @SerializedName("author")
    val author: AuthorX,
    @SerializedName("comment_count")
    val commentCount: Int,
    @SerializedName("committer")
    val committer: Committer,
    @SerializedName("message")
    val message: String,
    @SerializedName("tree")
    val tree: Tree,
    @SerializedName("url")
    val url: String,
    @SerializedName("verification")
    val verification: Verification
)