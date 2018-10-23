package com.mycahkrason.queuez.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mycahkrason.queuez.Model.QueueObject
import com.mycahkrason.queuez.R
import kotlinx.android.synthetic.main.queuez_row.view.*
import java.util.*

class QueuezListAdapter(val context: Context, val queuez: MutableList<QueueObject>, val itemClick: (QueueObject) -> Unit): RecyclerView.Adapter<QueuezListAdapter.Holder>() {

    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.bindQueue(queuez[position], context)

    }

    override fun getItemCount(): Int {
        return queuez.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.queuez_row, parent, false)
        return Holder(view, itemClick)
    }



    inner class Holder(itemView: View?, val itemClick: (QueueObject) -> Unit) : RecyclerView.ViewHolder(itemView) {

        val title = itemView?.findViewById<TextView>(R.id.queueTitleRow)
        val subtitle = itemView?.findViewById<TextView>(R.id.queueSubtitleRow)

        fun bindQueue(queue: QueueObject, context: Context){
            title?.text = queue.title
            subtitle?.text = queue.subtitle

            itemView.setOnClickListener {
                itemClick(queue)
            }
        }

    }
}