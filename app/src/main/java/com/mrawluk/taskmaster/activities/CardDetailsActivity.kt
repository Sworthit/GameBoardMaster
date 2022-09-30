package com.mrawluk.taskmaster.activities

import android.app.Activity
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.mrawluk.taskmaster.R
import com.mrawluk.taskmaster.databinding.ActivityCardDetailsBinding
import com.mrawluk.taskmaster.firebase.FireStoreBase
import com.mrawluk.taskmaster.models.Board
import com.mrawluk.taskmaster.models.Card
import com.mrawluk.taskmaster.models.Task
import com.mrawluk.taskmaster.utils.Constants

class CardDetailsActivity : BaseActivity() {
    private var binding: ActivityCardDetailsBinding? = null
    private lateinit var boardDetails: Board
    private var taskListPos = -1
    private var cardPos = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        getIntentData()
        setUpActionBar()

        binding?.etNameCardDetails?.setText(boardDetails.taskList[taskListPos].cards[cardPos].name)
        binding?.etNameCardDetails?.setSelection(binding?.etNameCardDetails?.text.toString().length)

        binding?.btnUpdateCardDetails?.setOnClickListener {
            if (binding?.etNameCardDetails?.text.toString().isNotEmpty()) {
                updateCardDetails()
            } else {
                showError("ENTER CARD NAME")
            }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding?.toolbarCardDetails)
        val actionBar = supportActionBar

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_button)
            actionBar.title = boardDetails.taskList[taskListPos].cards[cardPos].name
        }

        binding?.toolbarCardDetails?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            boardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POS)) {
            taskListPos = intent.getIntExtra(Constants.TASK_LIST_ITEM_POS, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POS)) {
            cardPos = intent.getIntExtra(Constants.CARD_LIST_ITEM_POS, -1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(boardDetails.taskList[taskListPos].cards[cardPos].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails() {
        val card = Card(
            binding?.etNameCardDetails?.text.toString(),
            boardDetails.taskList[taskListPos].cards[cardPos].createdBy,
            boardDetails.taskList[taskListPos].cards[cardPos].assignedTo
        )

        boardDetails.taskList[taskListPos].cards[cardPos] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreBase().addUpdateTaskList(this@CardDetailsActivity, boardDetails)
    }

    private fun deleteCard() {
        val cardsList: ArrayList<Card> = boardDetails.taskList[taskListPos].cards

        cardsList.removeAt(cardPos)

        val taskList: ArrayList<Task> = boardDetails.taskList
        taskList.removeAt(taskList.size -1 )

        taskList[taskListPos].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreBase().addUpdateTaskList(this@CardDetailsActivity, boardDetails)
    }

    private fun alertDialogForDeleteCard(title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss()
            deleteCard()
        }

        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

}