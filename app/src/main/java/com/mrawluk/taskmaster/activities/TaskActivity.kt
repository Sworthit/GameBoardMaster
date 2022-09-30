package com.mrawluk.taskmaster.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrawluk.taskmaster.R
import com.mrawluk.taskmaster.adapters.TaskListItemsAdapter
import com.mrawluk.taskmaster.databinding.ActivityTaskBinding
import com.mrawluk.taskmaster.firebase.FireStoreBase
import com.mrawluk.taskmaster.models.Board
import com.mrawluk.taskmaster.models.Card
import com.mrawluk.taskmaster.models.Task
import com.mrawluk.taskmaster.utils.Constants

class TaskActivity : BaseActivity() {
    private var binding: ActivityTaskBinding? = null

    private lateinit var boardDetails: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var firebaseDocId = ""
        if (intent.hasExtra(Constants.NAME)) {
            firebaseDocId = intent.getStringExtra(Constants.NAME)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreBase().getBoardDetails(this, firebaseDocId)
    }

    fun boardDetails(board: Board) {
        boardDetails = board
        hideProgressDialog()
        setUpActionBar()

        val addTaskList = Task(resources.getString(R.string.add_list))
        board.taskList.add(addTaskList)

        binding?.rvTask?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvTask?.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, board.taskList)
        binding?.rvTask?.adapter = adapter
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding?.toolbarTaskActivity)
        val actionBar = supportActionBar

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_button)
            actionBar.title = boardDetails.name
        }

        binding?.toolbarTaskActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreBase().getBoardDetails(this, boardDetails.firestoreDocumentId)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, FireStoreBase().getCurrentUserId())
        boardDetails.taskList.add(0, task)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreBase().addUpdateTaskList(this, boardDetails)
    }

    fun updateTaskList(pos: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)

        boardDetails.taskList[pos] = task
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreBase().addUpdateTaskList(this, boardDetails)
    }

    fun deleteTaskList(pos: Int) {
        boardDetails.taskList.removeAt(pos)

        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreBase().addUpdateTaskList(this, boardDetails)
    }

    fun addCard(pos: Int, cardName: String) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)
        val currentUser = FireStoreBase().getCurrentUserId()
        var cardAssignedUsers: ArrayList<String> = ArrayList()
        cardAssignedUsers.add(currentUser)

        val card = Card(cardName, currentUser, cardAssignedUsers)

        val cardsList = boardDetails.taskList[pos].cards

        cardsList.add(card)

        val task = Task(boardDetails.taskList[pos].title,
                    boardDetails.taskList[pos].createdBy,
                    cardsList
        )

        boardDetails.taskList[pos] = task

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreBase().addUpdateTaskList(this, boardDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menuMembers -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun cardDetails(taskListPos: Int, cardPos: Int) {
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POS, taskListPos)
        intent.putExtra(Constants.CARD_LIST_ITEM_POS, cardPos)
        startActivity(intent)
    }

}