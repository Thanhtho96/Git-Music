package com.tt.gitmusic.model


import com.google.gson.annotations.SerializedName

data class CommitDetail(
    @SerializedName("author")
    val author: Author,
    @SerializedName("comments_url")
    val commentsUrl: String,
    @SerializedName("commit")
    val commit: CommitX,
    @SerializedName("committer")
    val committer: CommitterX,
    @SerializedName("files")
    val files: List<File>,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("node_id")
    val nodeId: String,
    @SerializedName("parents")
    val parents: List<Parent>,
    @SerializedName("sha")
    val sha: String,
    @SerializedName("stats")
    val stats: Stats,
    @SerializedName("url")
    val url: String
)