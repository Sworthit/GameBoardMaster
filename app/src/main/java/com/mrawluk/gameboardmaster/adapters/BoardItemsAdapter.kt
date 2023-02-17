package com.mrawluk.gameboardmaster.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.models.Board
import de.hdodenhof.circleimageview.CircleImageView

open class BoardItemsAdapter(private val context: Context,
                          private var list: ArrayList<Board>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BoardViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_board, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is BoardViewHolder) {
            var image = holder.itemView.findViewById<CircleImageView>(R.id.civBoardImage)
            if (image != null) {
                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(image)
            }


            holder.itemView.findViewById<TextView>(R.id.tvName).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tvCreatedBy).text = "Created By : ${model.createdBy}"

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnCliCkListener(onClickListener: OnClickListener) {
         this.onClickListener = onClickListener
    }

    class BoardViewHolder(view: View): RecyclerView.ViewHolder(view)

    interface OnClickListener{
        fun onClick(position: Int, model: Board)
    }
}