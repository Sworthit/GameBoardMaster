package com.mrawluk.taskmaster.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrawluk.taskmaster.R
import com.mrawluk.taskmaster.activities.TaskActivity
import com.mrawluk.taskmaster.models.Task
import com.mrawluk.taskmaster.adapters.CardsItemsAdapter
open class TaskListItemsAdapter(
    private val context: Context,
    private var list:ArrayList<Task>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_task, parent, false)

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.75).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        view.layoutParams = layoutParams

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {
            val tvAddTaskList =  holder.itemView.findViewById<TextView>(R.id.tvAddTaskList)
            val llTaskItem = holder.itemView.findViewById<LinearLayout>(R.id.llTaskItem)
            val tvTaskListTitle = holder.itemView.findViewById<TextView>(R.id.tvTaskListTitle)
            val cvAddListName = holder.itemView.findViewById<CardView>(R.id.cvAddListName)
            val ibCloseListName = holder.itemView.findViewById<ImageButton>(R.id.ibCloseListName)
            val ibDoneListName = holder.itemView.findViewById<ImageButton>(R.id.ibDoneListName)
            val ibEditListName = holder.itemView.findViewById<ImageButton>(R.id.ibEditListName)
            val etEditTaskListName = holder.itemView.findViewById<EditText>(R.id.etEditTaskListName)
            val llTitleView = holder.itemView.findViewById<LinearLayout>(R.id.llTitleView)
            val cvEditTaskListName = holder.itemView.findViewById<CardView>(R.id.cvEditTaskListName)
            val ibCloseEditableView = holder.itemView.findViewById<ImageButton>(R.id.ibCloseEditableView)
            val ibDoneEditListName = holder.itemView.findViewById<ImageButton>(R.id.ibDoneEditListName)
            val ibDeleteList = holder.itemView.findViewById<ImageButton>(R.id.ibDeleteList)
            val tvAddCard = holder.itemView.findViewById<TextView>(R.id.tvAddCard)
            val cvAddCard = holder.itemView.findViewById<CardView>(R.id.cvAddCard)
            val ibCloseCardName = holder.itemView.findViewById<ImageButton>(R.id.ibCloseCardName)
            val ibDoneCardName = holder.itemView.findViewById<ImageButton>(R.id.ibDoneCardName)
            if (position == list.size - 1) {
                tvAddTaskList.visibility = View.VISIBLE
                llTaskItem.visibility = View.GONE

            } else {
                tvAddTaskList.visibility = View.GONE
                llTaskItem.visibility = View.VISIBLE
            }

            tvTaskListTitle.text = model.title
            tvAddTaskList.setOnClickListener{
                tvAddTaskList.visibility = View.GONE
                cvAddListName.visibility = View.VISIBLE
            }

            ibCloseListName.setOnClickListener {
                tvAddTaskList.visibility = View.VISIBLE
                cvAddListName.visibility = View.GONE
            }

            ibDoneListName.setOnClickListener {
                val listName = holder.itemView.findViewById<EditText>(R.id.etTaskListName).text.toString()

                if(listName.isNotEmpty()) {
                    if (context is TaskActivity) {
                        context.createTaskList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter Name", Toast.LENGTH_LONG).show()
                }
            }
            ibEditListName.setOnClickListener {
                etEditTaskListName.setText(model.title)
                llTitleView.visibility = View.GONE
                cvEditTaskListName.visibility = View.VISIBLE
            }

            ibCloseEditableView.setOnClickListener {
                llTitleView.visibility = View.VISIBLE
                cvEditTaskListName.visibility = View.GONE
            }

            ibDoneEditListName.setOnClickListener {
                val listName = etEditTaskListName.text.toString()
                if(listName.isNotEmpty()) {
                    if (context is TaskActivity) {
                        context.updateTaskList(position,listName, model)
                    }
                } else {
                    Toast.makeText(context, "Please Enter Name", Toast.LENGTH_LONG).show()
                }
            }

            ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }

            tvAddCard.setOnClickListener {
                tvAddTaskList.visibility = View.GONE
                cvAddCard.visibility = View.VISIBLE
            }

            ibCloseCardName.setOnClickListener {
                tvAddTaskList.visibility = View.VISIBLE
                cvAddCard.visibility = View.GONE
            }

            ibDoneCardName.setOnClickListener {
                val cardName = holder.itemView.findViewById<EditText>(R.id.etCardName).text.toString()

                if(cardName.isNotEmpty()) {
                    if (context is TaskActivity) {
                        context.addCard(position, cardName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter Card Name", Toast.LENGTH_LONG).show()
                }
            }

            val rvCardList = holder.itemView.findViewById<RecyclerView>(R.id.rvCardList)
            rvCardList.layoutManager = LinearLayoutManager(context)
            rvCardList.setHasFixedSize(true)
            var adapter = CardsItemsAdapter(context, model.cards)
            rvCardList.adapter = adapter

            adapter.setOnClickListener(
                object: CardsItemsAdapter.OnClickListener{
                    override fun onClick(cardPosition: Int) {
                        if (context is TaskActivity) {
                            context.cardDetails(position, cardPosition)
                        }
                    }
                }
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDp(): Int= (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int= (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss()

            if (context is TaskActivity) {
                context.deleteTaskList(position)
            }
        }

        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}