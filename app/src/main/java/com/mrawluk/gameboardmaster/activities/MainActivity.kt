package com.mrawluk.gameboardmaster.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.adapters.BoardItemsAdapter
import com.mrawluk.gameboardmaster.databinding.ActivityMainBinding
import com.mrawluk.gameboardmaster.firebase.FirestoreBase
import com.mrawluk.gameboardmaster.models.Board
import com.mrawluk.gameboardmaster.models.User
import com.mrawluk.gameboardmaster.utils.Constants
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityMainBinding? = null

    private lateinit var userEmail: String
    private lateinit var sharedPreferences: SharedPreferences

    private var startCreateBoardResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            FirestoreBase().getBoards(this)
        }
    }

    private var startCurrentUserProfileResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            FirestoreBase().getUser(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpActionBar()

        binding?.navView?.setNavigationItemSelectedListener(this)

        sharedPreferences = this.getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = sharedPreferences.getBoolean(Constants.TOKEN_UPDATED, false)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {
            if (!it.isSuccessful) {
                Log.e("Eror", "Fetching token failed", it.exception)
                return@OnCompleteListener
            }
        })

        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreBase().getUser(this, true)
        }else {
            FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity){
                    updateToken(it)
            }
        }

        FirestoreBase().getUser(this@MainActivity, true)

        var fabBtn = findViewById<FloatingActionButton>(R.id.fabCreateBoard)
        fabBtn.setOnClickListener{
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, userEmail);
            startCreateBoardResultLauncher.launch(intent)
        }
    }

    private fun setUpActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbarMain)
        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_navigation_menu)

        toolbar.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (binding?.drawer?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawer?.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawer?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding?.drawer?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawer?.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.navProfile -> {
                startCurrentUserProfileResultLauncher.launch(Intent(this,
                    ProfileActivity::class.java))
            }
            R.id.navSignOut -> {
                FirebaseAuth.getInstance().signOut()
                sharedPreferences.edit().clear().apply()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawer?.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateUserDetails(user: User, getBoards: Boolean) {
        hideProgressDialog()
        var civUserImage = findViewById<CircleImageView>(R.id.civUserImage)
        var tvUserName = findViewById<TextView>(R.id.tvUserName)
        userEmail = user.email
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_placeholder)
            .into(civUserImage)

        tvUserName.text = user.name

        if(getBoards) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreBase().getBoards(this)
        }
    }

    fun populateBoards(boards: ArrayList<Board>) {
        hideProgressDialog()
        var rvBoards = findViewById<RecyclerView>(R.id.rvBoards)
        var tvNoBoards = findViewById<TextView>(R.id.tvNoBoards)
        if(boards.size > 0) {
            rvBoards.visibility = View.VISIBLE
            tvNoBoards.visibility = View.GONE

            rvBoards.layoutManager = LinearLayoutManager(this)
            rvBoards.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boards)

            rvBoards.adapter = adapter

            adapter.setOnCliCkListener(object: BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, GameActivity::class.java)
                    intent.putExtra(Constants.NAME, model.firestoreDocumentId)
                    startActivity(intent)
                }
            })
        } else {
            rvBoards.visibility = View.GONE
            tvNoBoards.visibility = View.VISIBLE
        }
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(Constants.TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreBase().getUser(this, true)
    }

    private fun updateToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreBase().getUser(this, true)
        FirestoreBase().updateUser(this, userHashMap)
    }

}