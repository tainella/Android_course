package com.example.third_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_card.view.*

//import kotlinx.android.synthetic.main.item_list.view.*

class ListAdapter(
    private val openCard: (Int)->Unit,  private val onClick : ()->Unit //ссылка на функцию, Unit - void
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = mutableListOf<String>()

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: String) {
            itemView.tvItem.text = data
            itemView.setOnClickListener {
                openCard(data.toInt())
            }
        }
    }

    inner class ButtonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.setOnClickListener {
                list.add(list.size.toString())
                onClick()
                notifyItemInserted(list.size - 1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) ItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        )
        else ButtonHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_button, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemHolder) holder.bind(list[position])
        if (holder is ButtonHolder) holder.bind()
    }

    override fun getItemCount() = list.size + 1

    override fun getItemViewType(position: Int) = if (position == list.size) 1 else 0

}