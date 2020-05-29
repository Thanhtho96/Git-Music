package com.tt.gitmusic.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tt.gitmusic.model.BranchItem
import com.tt.gitmusic.model.CommitDetail
import com.tt.gitmusic.model.FileInBranch
import com.tt.gitmusic.model.UserRepo
import com.tt.gitmusic.dao.GithubDao
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GithubViewModel(application: Application, private val githubDao: GithubDao) : AndroidViewModel(application) {

    var listRepos: MutableLiveData<List<UserRepo>> = MutableLiveData()
    var listBranches: MutableLiveData<List<BranchItem>> = MutableLiveData()
    var commitDetail: MutableLiveData<CommitDetail> = MutableLiveData()
    var fileInBranch: MutableLiveData<FileInBranch> = MutableLiveData()

    fun getUserRepos(token: String) {
        viewModelScope.launch {
            githubDao.getUserRepos(token).enqueue(object : Callback<List<UserRepo>> {
                override fun onResponse(call: Call<List<UserRepo>>,
                                        response: Response<List<UserRepo>>) {
                    listRepos.value = response.body()
                }

                override fun onFailure(call: Call<List<UserRepo>>,
                                       t: Throwable) {
                    Toast.makeText(getApplication(), "Network is down", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun getBranches(token: String,
                    userName: String,
                    repoName: String) {
        viewModelScope.launch {
            githubDao.getBranches(token, userName, repoName).enqueue(object : Callback<List<BranchItem>> {
                override fun onResponse(call: Call<List<BranchItem>>,
                                        response: Response<List<BranchItem>>) {
                    listBranches.value = response.body()
                }

                override fun onFailure(call: Call<List<BranchItem>>,
                                       t: Throwable) {
                    Toast.makeText(getApplication(), "Network is down", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun getLastCommit(token: String,
                      userName: String,
                      repoName: String,
                      sha: String) {
        viewModelScope.launch {
            githubDao.getLastCommit(token, userName, repoName, sha).enqueue(object : Callback<CommitDetail> {
                override fun onResponse(call: Call<CommitDetail>,
                                        response: Response<CommitDetail>) {
                    commitDetail.value = response.body()
                }

                override fun onFailure(call: Call<CommitDetail>,
                                       t: Throwable) {
                    Toast.makeText(getApplication(), "Network is down", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun getFileInBranch(token: String,
                        url: String) {
        viewModelScope.launch {
            githubDao.getFileInBranch(token, url).enqueue(object : Callback<FileInBranch> {
                override fun onResponse(call: Call<FileInBranch>,
                                        response: Response<FileInBranch>) {
                    fileInBranch.value = response.body()
                }

                override fun onFailure(call: Call<FileInBranch>,
                                       t: Throwable) {
                    Toast.makeText(getApplication(), "Network is down", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}