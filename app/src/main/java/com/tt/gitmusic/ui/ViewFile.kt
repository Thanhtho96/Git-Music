package com.tt.gitmusic.ui

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.tt.gitmusic.R
import com.tt.gitmusic.adapter.FilesAdapter
import com.tt.gitmusic.databinding.ActivityFileBinding
import com.tt.gitmusic.model.TreeX
import com.tt.gitmusic.viewmodel.GithubViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ViewFile : AppCompatActivity(){

    private lateinit var binding: ActivityFileBinding
    private val githubViewModel by viewModel<GithubViewModel>()
    private val sharedPreferences by inject<SharedPreferences>()

    private var listFile: MutableList<TreeX> = ArrayList()
    private lateinit var filesAdapter: FilesAdapter
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("userName")
        val repoName = intent.getStringExtra("repoName")
        val sha = intent.getStringExtra("sha")
        val branchName = intent.getStringExtra("branchName")

        binding.branchName.text = getString(R.string.branch_name, branchName)

        val token = sharedPreferences.getString("token", "")

        if (token != null) {
            githubViewModel.getLastCommit(token, userName, repoName, sha)
            githubViewModel.commitDetail.observe(this, Observer {
                githubViewModel.getFileInBranch(token, it.commit.tree.url)
            })
        }

        githubViewModel.fileInBranch.observe(this, Observer {
            listFile.clear()
            for (file in it.tree) {
                if (file.path.endsWith(".mp3") || file.path.endsWith(".flac")) {
                    listFile.add(file)
                }
            }
            filesAdapter.notifyDataSetChanged()
            if (listFile.size == 0) {
                Toast.makeText(this, "No music file in this branch", Toast.LENGTH_SHORT).show()
            }
        })

        val map: HashMap<String, String> = HashMap()
        if (token != null) {
            map["Authorization"] = token
            map["Accept"] = "application/vnd.github.v3.raw+json"
        }

        val recyclerView = binding.genericRecyclerView.genericRecyclerView
        filesAdapter = FilesAdapter(this, listFile)
        filesAdapter.setOnItemClickListener(object : FilesAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val url = listFile[position].url
                val intent = Intent(this@ViewFile, PlayMusic::class.java)
                intent.putExtra("token", token)
                intent.putExtra("url", url)
                intent.action = "com.example.action.PLAY"
                startService(intent)
            }
        })

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        recyclerView.adapter = filesAdapter
    }
}
