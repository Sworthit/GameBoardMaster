package com.mrawluk.gameboardmaster.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.models.User
import com.mrawluk.gameboardmaster.utils.Constants

open class MembersItemsAdapter (
    private val context:Context,
    private var list: ArrayList<User>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_member,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .into(holder.itemView.findViewById(R.id.ivMemberImage))

            holder.itemView.findViewById<TextView>(R.id.tvMemberName).text = model.email

            if (model.selected) {
                holder.itemView.findViewById<ImageView>(R.id.ivSelectedMember).visibility = View.VISIBLE
            } else {
                holder.itemView.findViewById<ImageView>(R.id.ivSelectedMember).visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    if (model.selected) {
                        onClickListener!!.onClick(position, model, Constants.UNSELECT)
                    } else {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    interface OnClickListener{
        fun onClick(position: Int, user: User, action: String)
    }
}
