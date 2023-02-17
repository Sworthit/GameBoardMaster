package com.mrawluk.gameboardmaster.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.activities.GameActivity
import com.mrawluk.gameboardmaster.models.GameEvent
import com.mrawluk.gameboardmaster.models.SelectedParticipant

open class GameEventsItemsAdapter(
    private val context: Context,
    private var list: ArrayList<GameEvent>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_game_event,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[holder.adapterPosition]

        if (holder is MyViewHolder) {

            holder.itemView.findViewById<TextView>(R.id.tvGameEventName).text = model.name

            if ((context as GameActivity).assignedMembersDetailList.size > 0) {
                val selectedParticipants: ArrayList<SelectedParticipant> = ArrayList()

                for (i in context.assignedMembersDetailList.indices) {
                    for (j in model.assignedTo) {
                        if (context.assignedMembersDetailList[i].id == j) {
                            val selectedParticipant = SelectedParticipant(
                                context.assignedMembersDetailList[i].id,
                                context.assignedMembersDetailList[i].image
                            )
                            selectedParticipants.add(selectedParticipant)
                        }
                    }
                }

                if (selectedParticipants.size > 0) {
                    if (selectedParticipants.size == 1 && selectedParticipants[0].id == model.createdBy) {
                        holder.itemView.findViewById<RecyclerView>(R.id.rvGameEventSelectedMembers).visibility = View.GONE
                    }else {
                        holder.itemView.findViewById<RecyclerView>(R.id.rvGameEventSelectedMembers).visibility = View.VISIBLE

                        holder.itemView.findViewById<RecyclerView>(R.id.rvGameEventSelectedMembers).layoutManager =
                            GridLayoutManager(context, 4)
                        val adapter = GameEventMemberListItemsAdapter(context, selectedParticipants, false)
                        holder.itemView.findViewById<RecyclerView>(R.id.rvGameEventSelectedMembers).adapter = adapter

                        adapter.setOnClickListener(object: GameEventMemberListItemsAdapter.OnClickListener{
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(holder.adapterPosition)
                                }
                            }
                        })
                    }
                }else {
                    holder.itemView.findViewById<RecyclerView>(R.id.rvGameEventSelectedMembers).visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}