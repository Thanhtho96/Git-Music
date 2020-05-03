package com.tt.gitmusic.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.tt.gitmusic.R
import com.tt.gitmusic.adapter.BranchAdapter
import com.tt.gitmusic.databinding.ActivityBranchBinding
import com.tt.gitmusic.model.BranchItem
import com.tt.gitmusic.viewmodel.GithubViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ViewBranch : AppCompatActivity() {

    private lateinit var binding: ActivityBranchBinding

    private val githubViewModel: GithubViewModel by viewModel()
    private val sharePref: SharedPreferences by inject()

    private var listBranch: MutableList<BranchItem> = ArrayList()
    private lateinit var branchAdapter: BranchAdapter
    private lateinit var userName: String
    private lateinit var repoName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBranchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName")
        repoName = intent.getStringExtra("repoName")

        binding.repoName.text = getString(R.string.repo_name, repoName)

        sharePref.getString(getString(R.string.token), "")?.let {
            githubViewModel.getBranches(it, userName, repoName)
        }
        githubViewModel.listBranches.observe(this, Observer {
            listBranch.clear()
            listBranch.addAll(it)
            branchAdapter.notifyDataSetChanged()
        })

        val recyclerView = binding.genericRecyclerView.genericRecyclerView
        branchAdapter = BranchAdapter(this, listBranch)
        branchAdapter.setOnItemClickListener(object : BranchAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val branchItem = listBranch[position]

                val intent = Intent(this@ViewBranch, ViewFile::class.java)
                intent.putExtra("userName", userName)
                intent.putExtra("repoName", repoName)
                intent.putExtra("sha", branchItem.commit.sha)
                intent.putExtra("branchName", branchItem.name)
                startActivity(intent)
            }

        })
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        recyclerView.adapter = branchAdapter
    }
}
