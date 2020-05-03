package com.tt.gitmusic.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.tt.gitmusic.R
import com.tt.gitmusic.adapter.ReposAdapter
import com.tt.gitmusic.databinding.ActivityMainBinding
import com.tt.gitmusic.model.UserRepo
import com.tt.gitmusic.viewmodel.GithubViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val githubViewModel: GithubViewModel by viewModel()
    private val sharePref: SharedPreferences by inject()

    private var listRepo: MutableList<UserRepo> = ArrayList()
    private lateinit var reposAdapter: ReposAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharePref.getString(getString(R.string.token), "")?.let { githubViewModel.getUserRepos(it) }
        githubViewModel.listRepos.observe(this, Observer {
            Glide.with(this)
                    .load(it[0].owner.avatarUrl)
                    .circleCrop()
                    .into(binding.avatar)
            binding.name.text = it[0].owner.login

            listRepo.clear()
            listRepo.addAll(it)
            reposAdapter.notifyDataSetChanged()
        })

        val recyclerView = binding.genericRecyclerView.genericRecyclerView
        reposAdapter = ReposAdapter(this, listRepo)
        reposAdapter.setOnItemClickListener(object : ReposAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val userRepo: UserRepo = listRepo[position]

                val intent = Intent(this@MainActivity, ViewBranch::class.java)
                intent.putExtra("userName", userRepo.owner.login)
                intent.putExtra("repoName", userRepo.name)
                startActivity(intent)
            }
        })
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        recyclerView.adapter = reposAdapter

    }
}