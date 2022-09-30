package com.mrawluk.taskmaster.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrawluk.taskmaster.R
import com.mrawluk.taskmaster.adapters.MemberItemAdapter
import com.mrawluk.taskmaster.databinding.ActivityMembersBinding
import com.mrawluk.taskmaster.firebase.FireStoreBase
import com.mrawluk.taskmaster.models.Board
import com.mrawluk.taskmaster.models.User
import com.mrawluk.taskmaster.utils.Constants

class MembersActivity : BaseActivity() {

    private var binding: ActivityMembersBinding? = null
    private lateinit var boardDetails: Board
    private lateinit var assignedMembers: ArrayList<User>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            boardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        setUpActionBar()
        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreBase().getAssignedUsers(this, boardDetails.assignedUsers)
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding?.toolbarMembers)
        val actionBar = supportActionBar

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_button)
            actionBar.title = "Members"
        }

        binding?.toolbarMembers?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUpMembersList(list: ArrayList<User>) {
        assignedMembers = list
        hideProgressDialog()

        binding?.rvMembersList?.layoutManager = LinearLayoutManager(this)
        binding?.rvMembersList?.setHasFixedSize(true)
        val adapter = MemberItemAdapter(this, list)

        binding?.rvMembersList?.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionAddMember -> {
                dialogSearch()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearch() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.findViewById<TextView>(R.id.tvAdd).setOnClickListener{
            val email = dialog.findViewById<EditText>(R.id.etEmailSearchMember).text.toString()
            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FireStoreBase().getMemberDetails(this, email)
            }else {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show()
            }
        }
        dialog.findViewById<TextView>(R.id.tvCancel).setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
    }

    fun memberDetails(user: User) {
        boardDetails.assignedUsers.add(user.id)
        FireStoreBase().assignMemberToBoard(this, boardDetails, user)
    }

    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        assignedMembers.add(user)
        setUpMembersList(assignedMembers)
    }
}