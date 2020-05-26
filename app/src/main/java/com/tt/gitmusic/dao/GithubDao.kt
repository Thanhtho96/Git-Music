package com.tt.gitmusic.dao

import com.tt.gitmusic.model.BranchItem
import com.tt.gitmusic.model.CommitDetail
import com.tt.gitmusic.model.FileInBranch
import com.tt.gitmusic.model.UserRepo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Url

interface GithubDao {

    @GET("user/repos")
    fun getUserRepos(@Header("Authorization") token: String): Call<List<UserRepo>>

    @GET("repos/{user_name}/{repo_name}/branches")
    fun getBranches(@Header("Authorization") token: String,
                    @Path("user_name") userName: String,
                    @Path("repo_name") repoName: String): Call<List<BranchItem>>

    @GET("repos/{user_name}/{repo_name}/commits/{sha}")
    fun getLastCommit(@Header("Authorization") token: String,
                      @Path("user_name") userName: String,
                      @Path("repo_name") repoName: String,
                      @Path("sha") sha: String): Call<CommitDetail>

    @GET
    fun getFileInBranch(@Header("Authorization") token: String,
                        @Url url: String): Call<FileInBranch>
}
