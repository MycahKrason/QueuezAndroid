package com.mycahkrason.queuez.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mycahkrason.queuez.Model.MemberObject
import com.mycahkrason.queuez.R
import java.lang.reflect.Member

class MembersListAdapter(val context: Context, val members: MutableList<MemberObject>, val itemClick: (MemberObject) -> Unit ): RecyclerView.Adapter<MembersListAdapter.Holder>() {

    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.bindMember(members[position], position, context)

    }

    override fun getItemCount(): Int {
        return members.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.members_row, parent, false)

        return Holder(view, itemClick)

    }

    inner class Holder(itemView: View?, val itemClick: (MemberObject) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val membersPlace = itemView?.findViewById<TextView>(R.id.memberNumberRow)
        val membersName = itemView?.findViewById<TextView>(R.id.memberNameRow)

        fun bindMember(member: MemberObject, placement: Int, context: Context){
            membersName?.text = member.memberName

            //TODO: get the placement of the item and affix that to the members place - this will be recieved from "onBindViewHolder"
            membersPlace?.text = "${placement + 1}"

            itemView?.setOnClickListener { itemClick(member) }

        }
    }
}