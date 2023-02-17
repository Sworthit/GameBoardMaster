package com.mrawluk.gameboardmaster.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.adapters.GameListItemsAdapter
import com.mrawluk.gameboardmaster.databinding.ActivityGameBinding
import com.mrawluk.gameboardmaster.firebase.FirestoreBase
import com.mrawluk.gameboardmaster.models.Board
import com.mrawluk.gameboardmaster.models.Game
import com.mrawluk.gameboardmaster.models.GameEvent
import com.mrawluk.gameboardmaster.models.User
import com.mrawluk.gameboardmaster.utils.Constants

class GameActivity : BaseActivity() {
    private var binding: ActivityGameBinding? = null

    private lateinit var boardDetails: Board
    lateinit var assignedMembersDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var firebaseDocId = ""
        if (intent.hasExtra(Constants.NAME)) {
            firebaseDocId = intent.getStringExtra(Constants.NAME)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreBase().getBoardDetails(this, firebaseDocId)
    }

    fun boardDetails(board: Board) {
        boardDetails = board
        hideProgressDialog()
        setUpActionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreBase().getAssignedUsers(this, boardDetails.assignedUsers)
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding?.toolbarGameActivity)
        val actionBar = supportActionBar

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_button)
            actionBar.title = boardDetails.name
        }

        binding?.toolbarGameActivity?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun addUpdateGameListSuccess() {
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreBase().getBoardDetails(this, boardDetails.firestoreDocumentId)
    }

    fun createGameList(gameListName: String) {
        val game = Game(gameListName, FirestoreBase().getCurrentUserId())
        boardDetails.gameList.add(0, game)
        boardDetails.gameList.removeAt(boardDetails.gameList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreBase().addUpdateGameList(this, boardDetails)
    }

    fun updateGameList(pos: Int, listName: String, model: Game) {
        val game = Game(listName, model.createdBy)

        boardDetails.gameList[pos].title = game.title
        boardDetails.gameList.removeAt(boardDetails.gameList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreBase().addUpdateGameList(this, boardDetails)
    }

    fun deleteGameList(pos: Int) {
        boardDetails.gameList.removeAt(pos)

        boardDetails.gameList.removeAt(boardDetails.gameList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreBase().addUpdateGameList(this, boardDetails)
    }

    fun addGameEvent(pos: Int, gameName: String) {
        boardDetails.gameList.removeAt(boardDetails.gameList.size - 1)
        val currentUser = FirestoreBase().getCurrentUserId()
        var gameEventAssignedUsers: ArrayList<String> = ArrayList()
        gameEventAssignedUsers.add(currentUser)

        val gameEvent = GameEvent(gameName, currentUser, gameEventAssignedUsers)

        val gameEvents = boardDetails.gameList[pos].gameEvents

        gameEvents.add(gameEvent)

        val game = Game(boardDetails.gameList[pos].title,
                    boardDetails.gameList[pos].createdBy,
                    gameEvents
        )

        boardDetails.gameList[pos] = game

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreBase().addUpdateGameList(this, boardDetails)
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

    fun boardMembersDetailsList(list: ArrayList<User>){
        assignedMembersDetailList = list

        hideProgressDialog()

        val addGameList = Game(resources.getString(R.string.add_list))
        boardDetails.gameList.add(addGameList)

        binding?.rvGame?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvGame?.setHasFixedSize(true)

        val adapter = GameListItemsAdapter(this, boardDetails.gameList)
        binding?.rvGame?.adapter = adapter
    }

    fun moveGameEventsInGameList(gameListPos: Int, gameEvents: ArrayList<GameEvent>) {
        boardDetails.gameList.removeAt(boardDetails.gameList.size - 1)

        boardDetails.gameList[gameListPos].gameEvents = gameEvents

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreBase().addUpdateGameList(this, boardDetails)
    }

    fun gameEventDetails(gameListPos: Int, gameEventPos: Int) {
        val intent = Intent(this, GameEventDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
        intent.putExtra(Constants.GAME_LIST_ITEM_POS, gameListPos)
        intent.putExtra(Constants.GAME_EVENT_LIST_ITEM_POS, gameEventPos)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, assignedMembersDetailList)
        startActivity(intent)
        finish()
    }

}