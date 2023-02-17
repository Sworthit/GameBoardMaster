package com.mrawluk.gameboardmaster.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.activities.GameActivity
import com.mrawluk.gameboardmaster.models.Game
import java.util.*

open class GameListItemsAdapter(
    private val context: Context,
    private var list:ArrayList<Game>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var positionDraggedSource = -1
    private var positionDraggedTarget = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_game, parent, false)

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
            val tvAddGameList =  holder.itemView.findViewById<TextView>(R.id.tvAddGameList)
            val llGameItem = holder.itemView.findViewById<LinearLayout>(R.id.llGameItem)
            val tvGameListTitle = holder.itemView.findViewById<TextView>(R.id.tvGameListTitle)
            val cvAddListName = holder.itemView.findViewById<CardView>(R.id.cvAddGameListName)
            val ibCloseListName = holder.itemView.findViewById<ImageButton>(R.id.ibCloseGameListName)
            val ibDoneListName = holder.itemView.findViewById<ImageButton>(R.id.ibDoneGameListName)
            val ibEditListName = holder.itemView.findViewById<ImageButton>(R.id.ibEditGameListName)
            val etEditGameListName = holder.itemView.findViewById<EditText>(R.id.etEditGameListName)
            val llTitleView = holder.itemView.findViewById<LinearLayout>(R.id.llTitleView)
            val cvEditGameListName = holder.itemView.findViewById<CardView>(R.id.cvEditGameListName)
            val ibCloseEditableView = holder.itemView.findViewById<ImageButton>(R.id.ibCloseEditableView)
            val ibDoneEditListName = holder.itemView.findViewById<ImageButton>(R.id.ibDoneEditGameListName)
            val ibDeleteList = holder.itemView.findViewById<ImageButton>(R.id.ibDeleteGameList)
            val tvAddGameEvent = holder.itemView.findViewById<TextView>(R.id.tvAddGameEvent)
            val cvAddGameEvent = holder.itemView.findViewById<CardView>(R.id.cvAddGameEvent)
            val ibCloseGameEventName = holder.itemView.findViewById<ImageButton>(R.id.ibCloseGameEventName)
            val ibDoneGameEventName = holder.itemView.findViewById<ImageButton>(R.id.ibDoneGameEventName)
            if (position == list.size - 1) {
                tvAddGameList.visibility = View.VISIBLE
                llGameItem.visibility = View.GONE

            } else {
                tvAddGameList.visibility = View.GONE
                llGameItem.visibility = View.VISIBLE
            }

            tvGameListTitle.text = model.title
            tvAddGameList.setOnClickListener{
                tvAddGameList.visibility = View.GONE
                cvAddListName.visibility = View.VISIBLE
            }

            ibCloseListName.setOnClickListener {
                tvAddGameList.visibility = View.VISIBLE
                cvAddListName.visibility = View.GONE
            }

            ibDoneListName.setOnClickListener {
                val listName = holder.itemView.findViewById<EditText>(R.id.etTaskListName).text.toString()

                if(listName.isNotEmpty()) {
                    if (context is GameActivity) {
                        context.createGameList(listName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter Name", Toast.LENGTH_LONG).show()
                }
            }
            ibEditListName.setOnClickListener {
                etEditGameListName.setText(model.title)
                llTitleView.visibility = View.GONE
                cvEditGameListName.visibility = View.VISIBLE
            }

            ibCloseEditableView.setOnClickListener {
                llTitleView.visibility = View.VISIBLE
                cvEditGameListName.visibility = View.GONE
            }

            ibDoneEditListName.setOnClickListener {
                val listName = etEditGameListName.text.toString()
                if(listName.isNotEmpty()) {
                    if (context is GameActivity) {
                        context.updateGameList(position,listName, model)
                    }
                } else {
                    Toast.makeText(context, "Please Enter Name", Toast.LENGTH_LONG).show()
                }
            }

            ibDeleteList.setOnClickListener {
                alertDialogForDeleteList(position, model.title)
            }

            tvAddGameEvent.setOnClickListener {
                tvAddGameList.visibility = View.GONE
                cvAddGameEvent.visibility = View.VISIBLE
            }

            ibCloseGameEventName.setOnClickListener {
                tvAddGameList.visibility = View.VISIBLE
                cvAddGameEvent.visibility = View.GONE
            }

            ibDoneGameEventName.setOnClickListener {
                val gameEventName = holder.itemView.findViewById<EditText>(R.id.etCardName).text.toString()

                if(gameEventName.isNotEmpty()) {
                    if (context is GameActivity) {
                        context.addGameEvent(position, gameEventName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter Game Event Name", Toast.LENGTH_LONG).show()
                }
            }

            val rvGameEventList = holder.itemView.findViewById<RecyclerView>(R.id.rvCardList)
            rvGameEventList.layoutManager = LinearLayoutManager(context)
            rvGameEventList.setHasFixedSize(true)
            var adapter = GameEventsItemsAdapter(context, model.gameEvents)
            rvGameEventList.adapter = adapter

            adapter.setOnClickListener(
                object: GameEventsItemsAdapter.OnClickListener{
                    override fun onClick(gameEventPosition: Int) {
                        if (context is GameActivity) {
                            context.gameEventDetails(holder.adapterPosition, gameEventPosition)
                        }
                    }
                }
            )

            val dividerItemDecoration = DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL)
            holder.itemView.findViewById<RecyclerView>(R.id.rvCardList).addItemDecoration(dividerItemDecoration)

            val itemTouchHelper = ItemTouchHelper(
                object: ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
                ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val draggedPosition = viewHolder.adapterPosition
                        val targetPosition = target.adapterPosition

                        if(positionDraggedSource == -1) {
                            positionDraggedSource = draggedPosition
                        }
                        positionDraggedTarget = targetPosition
                        Collections.swap(list[holder.adapterPosition].gameEvents,draggedPosition, targetPosition)
                        adapter.notifyItemMoved(draggedPosition, targetPosition)
                        return false
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    }

                    override fun clearView(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder
                    ) {
                        super.clearView(recyclerView, viewHolder)

                        if(positionDraggedSource != -1 && positionDraggedTarget != -1
                            && positionDraggedSource != positionDraggedTarget) {
                            (context as GameActivity).moveGameEventsInGameList(
                                    holder.adapterPosition,
                                    list[holder.adapterPosition].gameEvents
                                    )
                        }
                        positionDraggedTarget = -1
                        positionDraggedSource = -1
                    }

                }
            )
            itemTouchHelper.attachToRecyclerView(holder.itemView.findViewById(R.id.rvCardList))
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

            if (context is GameActivity) {
                context.deleteGameList(position)
            }
        }

        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view)

}