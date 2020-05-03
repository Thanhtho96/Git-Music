package com.tt.gitmusic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tt.gitmusic.databinding.ItemGenericBinding
import com.tt.gitmusic.model.BranchItem

class BranchAdapter(context: Context,
                    private var listBranch: List<BranchItem>) : RecyclerView.Adapter<BranchAdapter.ViewHolder>() {

    private lateinit var itemGenericBinding: ItemGenericBinding
    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var mListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        itemGenericBinding = ItemGenericBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(itemGenericBinding, mListener)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun getItemCount(): Int {
        return listBranch.size
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        val branchItem = listBranch[position]
        holder.branchName.text = branchItem.name

        holder.itemView.setOnClickListener {
            val position: Int = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                holder.listener.onItemClick(position)
            }
        }
    }

    class ViewHolder(binding: ItemGenericBinding,
                     val listener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        val branchName = binding.genericItemName
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}