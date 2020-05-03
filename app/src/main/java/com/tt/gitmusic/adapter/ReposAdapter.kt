package com.tt.gitmusic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tt.gitmusic.databinding.ItemGenericBinding
import com.tt.gitmusic.model.UserRepo

class ReposAdapter(context: Context,
                   private var listRepo: List<UserRepo>) : RecyclerView.Adapter<ReposAdapter.ViewHolder>() {

    private lateinit var itemRepoBinding: ItemGenericBinding
    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var mListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        itemRepoBinding = ItemGenericBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(itemRepoBinding, mListener)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun getItemCount(): Int {
        return listRepo.size
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        val userRepo: UserRepo = listRepo[position]
        holder.repoName.text = userRepo.name

        holder.itemView.setOnClickListener {
            val position: Int = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                holder.listener.onItemClick(position)
            }
        }
    }

    class ViewHolder(binding: ItemGenericBinding,
                     val listener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        var repoName = binding.genericItemName
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}