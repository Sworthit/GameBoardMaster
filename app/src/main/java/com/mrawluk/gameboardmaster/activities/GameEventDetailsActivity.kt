package com.mrawluk.gameboardmaster.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.adapters.GameEventMemberListItemsAdapter
import com.mrawluk.gameboardmaster.databinding.ActivityGameEventDetailsBinding
import com.mrawluk.gameboardmaster.dialogs.MembersListDialog
import com.mrawluk.gameboardmaster.firebase.FirestoreBase
import com.mrawluk.gameboardmaster.models.*
import com.mrawluk.gameboardmaster.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class GameEventDetailsActivity : BaseActivity() {
    private var binding: ActivityGameEventDetailsBinding? = null
    private lateinit var boardDetails: Board
    private var gameListPos = -1
    private var gameEventPos = -1
    private lateinit var membersDetailsList: ArrayList<User>
    private var selectedDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameEventDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        getIntentData()
        setUpActionBar()

        binding?.etNameCardDetails?.setText(boardDetails.gameList[gameListPos].gameEvents[gameEventPos].name)
        binding?.etNameCardDetails?.setSelection(binding?.etNameCardDetails?.text.toString().length)

        binding?.btnUpdateCardDetails?.setOnClickListener {
            if (binding?.etNameCardDetails?.text.toString().isNotEmpty()) {
                updateGameEventDetails()
            } else {
                showError("ENTER CARD NAME")
            }
        }

        binding?.tvSelectMembers?.setOnClickListener {
            membersListDialog()
        }

        selectedDate = boardDetails.gameList[gameListPos].gameEvents[gameEventPos].eventDate

        if (selectedDate > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

            val selectedDateString = simpleDateFormat.format(Date(selectedDate))

            binding?.tvSelectDueDate?.text = selectedDateString
        }

        binding?.tvSelectDueDate?.setOnClickListener {
            showDatePicker()
        }

        setUpSelectedParticipantsList()
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding?.toolbarCardDetails)
        val actionBar = supportActionBar

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_button)
            actionBar.title = boardDetails.gameList[gameListPos].gameEvents[gameEventPos].name
        }

        binding?.toolbarCardDetails?.setNavigationOnClickListener {
            returnToBoard()
        }
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            boardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.GAME_LIST_ITEM_POS)) {
            gameListPos = intent.getIntExtra(Constants.GAME_LIST_ITEM_POS, -1)
        }
        if (intent.hasExtra(Constants.GAME_EVENT_LIST_ITEM_POS)) {
            gameEventPos = intent.getIntExtra(Constants.GAME_EVENT_LIST_ITEM_POS, -1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            membersDetailsList = intent.getParcelableArrayListExtra(
                Constants.BOARD_MEMBERS_LIST
            )!!
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteGameEvent(boardDetails.gameList[gameListPos].gameEvents[gameEventPos].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addUpdateGameListSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        returnToBoard()
    }

    private fun returnToBoard() {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(Constants.NAME, boardDetails.firestoreDocumentId)
        startActivity(intent)
        finish()
    }

    private fun membersListDialog() {
        var gameEventAssignedMembersList = boardDetails.gameList[gameListPos]
            .gameEvents[gameEventPos].assignedTo

        if (gameEventAssignedMembersList.size > 0) {
            for (i in membersDetailsList.indices) {
                for (j in gameEventAssignedMembersList) {
                    if (membersDetailsList[i].id == j) {
                        membersDetailsList[i].selected = true
                    }
                }
            }
        }else {
            for (i in membersDetailsList.indices) {
                membersDetailsList[i].selected = false
            }
        }

        val listDialog = object: MembersListDialog(this, membersDetailsList, resources.getString(R.string.select_host)) {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if(!boardDetails
                            .gameList[gameListPos]
                            .gameEvents[gameEventPos].assignedTo.contains(user.id)) {
                        boardDetails
                            .gameList[gameListPos].gameEvents[gameEventPos].assignedTo.add(user.id)
                    }
                }else {
                    boardDetails
                        .gameList[gameListPos].gameEvents[gameEventPos].assignedTo.remove(user.id)

                    for (i in membersDetailsList.indices) {
                        if (membersDetailsList[i].id == user.id) {
                            membersDetailsList[i].selected = false
                        }
                    }
                }
                setUpSelectedParticipantsList()
            }
        }.show()
    }

    private fun updateGameEventDetails() {
        val gameEvent = GameEvent(
            binding?.etNameCardDetails?.text.toString(),
            boardDetails.gameList[gameListPos].gameEvents[gameEventPos].createdBy,
            boardDetails.gameList[gameListPos].gameEvents[gameEventPos].assignedTo,
            selectedDate
        )

        boardDetails.gameList[gameListPos].gameEvents[gameEventPos] = gameEvent

        val gameList: ArrayList<Game> = boardDetails.gameList
        gameList.removeAt(gameList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        var assignedToTmp = boardDetails.gameList[gameListPos].gameEvents[gameEventPos].assignedTo
        if (assignedToTmp.size > 1) {
            var title = "New game of ${boardDetails.gameList[gameListPos].title} was scheduled in ${boardDetails.name}!"
            var message = "You've been invited to play by ${membersDetailsList[0].email}"
            var userToken = ""
            for (user in assignedToTmp) {
                if (user != getCurrentUserId()) {
                    userToken = membersDetailsList.filter { filteredUser -> filteredUser.id == user }.single().token
                    CallAPIServerAsyncTask().startApiCall(title, message, userToken)
                }
            }
        }
        FirestoreBase().addUpdateGameList(this@GameEventDetailsActivity, boardDetails)
    }

    private fun deleteGameEvent() {
        val gameEventsList: ArrayList<GameEvent> = boardDetails.gameList[gameListPos].gameEvents

        gameEventsList.removeAt(gameEventPos)

        val gameList: ArrayList<Game> = boardDetails.gameList
        gameList.removeAt(gameList.size -1 )

        gameList[gameListPos].gameEvents = gameEventsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreBase().addUpdateGameList(this@GameEventDetailsActivity, boardDetails)
    }

    private fun alertDialogForDeleteGameEvent(title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss()
            deleteGameEvent()
        }

        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun setUpSelectedParticipantsList() {
        val gameEventAssignedParticipantsList =
            boardDetails.gameList[gameListPos].gameEvents[gameEventPos].assignedTo

        val selectedParticipantsList: ArrayList<SelectedParticipant> = ArrayList()

        for (i in membersDetailsList.indices) {
            for (j in gameEventAssignedParticipantsList) {
                if (membersDetailsList[i].id == j) {
                    val selectedParticipant = SelectedParticipant(
                        membersDetailsList[i].id,
                        membersDetailsList[i].image
                    )
                    selectedParticipantsList.add(selectedParticipant)
                }
            }
        }

        if (selectedParticipantsList.size > 0) {
            selectedParticipantsList.add(SelectedParticipant("",""))
            binding?.tvSelectMembers?.visibility = View.GONE
            binding?.rvSelectedMembers?.visibility = View.VISIBLE
            binding?.rvSelectedMembers?.layoutManager = GridLayoutManager(
                this, 6
            )
            val adapter = GameEventMemberListItemsAdapter(this, selectedParticipantsList, true)
            binding?.rvSelectedMembers?.adapter = adapter
            adapter.setOnClickListener(
                object: GameEventMemberListItemsAdapter.OnClickListener{
                    override fun onClick() {
                        membersListDialog()
                    }
                }
            )
        }else {
            binding?.tvSelectMembers?.visibility = View.VISIBLE
            binding?.rvSelectedMembers?.visibility = View.GONE
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            {
                view, year, month, day ->
                val dayOfMonth = if (day < 10) "0$day" else "$day"
                val monthOfYear = if ((month + 1) < 10) "0${month + 1}" else "$month + 1"
                val selectedDateString = "$dayOfMonth/$monthOfYear/$year"

                binding?.tvSelectDueDate?.text = selectedDateString

                val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                val date = simpleDateFormat.parse(selectedDateString)

                selectedDate = date!!.time
            },
            year,
            month,
            day
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

}