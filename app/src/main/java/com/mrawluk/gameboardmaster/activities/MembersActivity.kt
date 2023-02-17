package com.mrawluk.gameboardmaster.activities

import android.app.Dialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.adapters.MembersItemsAdapter
import com.mrawluk.gameboardmaster.databinding.ActivityMembersBinding
import com.mrawluk.gameboardmaster.firebase.FirestoreBase
import com.mrawluk.gameboardmaster.models.Board
import com.mrawluk.gameboardmaster.models.User
import com.mrawluk.gameboardmaster.utils.Constants
import kotlinx.coroutines.launch

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

        FirestoreBase().getAssignedUsers(this, boardDetails.assignedUsers)
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
        val adapter = MembersItemsAdapter(this, list)

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
                FirestoreBase().getMemberDetails(this, email)
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
        lifecycleScope.launch {
            FirestoreBase().assignMemberToBoard(this@MembersActivity, boardDetails, user)
        }
    }

    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        assignedMembers.add(user)
        setUpMembersList(assignedMembers)
        var title = "Assigned to board ${boardDetails.name}"
        var message = "You have been assigned by ${assignedMembers[0].email}"
        CallAPIServerAsyncTask().startApiCall(title, message, user.token)
    }

}