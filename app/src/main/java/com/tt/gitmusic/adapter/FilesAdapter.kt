package com.tt.gitmusic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tt.gitmusic.databinding.ItemGenericBinding
import com.tt.gitmusic.model.TreeX

class FilesAdapter(context: Context,
                   private var files: List<TreeX>) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

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
        return files.size
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        val fileInBranch = files[position]
        holder.name.text = fileInBranch.path

        holder.itemView.setOnClickListener {
            val position: Int = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                holder.listener.onItemClick(position)
            }
        }
    }

    class ViewHolder(binding: ItemGenericBinding,
                     val listener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        val name = binding.genericItemName
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}