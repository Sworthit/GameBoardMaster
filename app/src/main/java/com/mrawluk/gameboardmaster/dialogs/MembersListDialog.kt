package com.mrawluk.gameboardmaster.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.adapters.MembersItemsAdapter
import com.mrawluk.gameboardmaster.models.User

abstract class MembersListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
) : Dialog(context) {

    private var adapter: MembersItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_list, null)
        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        view.findViewById<TextView>(R.id.tvTitle).text = title
        val rvList = view.findViewById<RecyclerView>(R.id.rvList)
        if (list.size > 0) {
            rvList.layoutManager = LinearLayoutManager(context)
            adapter = MembersItemsAdapter(context, list)
            rvList.adapter = adapter

            adapter!!.setOnClickListener(object:
            MembersItemsAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user,action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User, action: String)
}