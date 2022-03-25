package com.example.third_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_card.view.*

//import kotlinx.android.synthetic.main.item_list.view.*

class ListAdapter(
    private val openCard: (String, Int)->Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = mutableListOf<String>()

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: String, position: Int) {
            itemView.tvItem.text = data
            itemView.setOnClickListener {
                openCard(data, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder) holder.bind(list[position], position)
    }

    override fun getItemCount() = list.size

    fun addList(list: List<String>) {
        this.list.addAll(list)
        notifyDataSetChanged()
    }
}