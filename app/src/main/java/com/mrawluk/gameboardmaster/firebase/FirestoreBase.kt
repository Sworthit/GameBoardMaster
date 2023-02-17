package com.mrawluk.gameboardmaster.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mrawluk.gameboardmaster.activities.*
import com.mrawluk.gameboardmaster.models.Board
import com.mrawluk.gameboardmaster.models.User
import com.mrawluk.gameboardmaster.utils.Constants

class FirestoreBase {
    private val fireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, user: User) {
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).set(user, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
    }

    fun registerBoard(activity: CreateBoardActivity, board: Board) {
        fireStore.collection(Constants.BOARDS)
            .document().set(board, SetOptions.merge())
            .addOnSuccessListener {
                activity.boardRegisteredSuccess()
            }
            .addOnFailureListener {
                exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                "Error whilst creating board record",
                exception)
            }
    }

    fun getCurrentUserId(): String {
        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun getUser(activity: Activity, getBoards: Boolean = false) {
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)

                when(activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser!!)
                    }
                    is MainActivity -> {
                        activity.updateUserDetails(loggedInUser!!, getBoards)
                    }
                    is ProfileActivity -> {
                        activity.setUser(loggedInUser!!)
                    }
                }
            }.addOnFailureListener {
                e ->
                when(activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
            }
    }

    fun updateUser(activity: Activity,
                   userHashMap: HashMap<String, Any>) {
        fireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "User updated", Toast.LENGTH_LONG).show()
                when(activity) {
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is ProfileActivity -> {
                        activity.profileUpdateSuccess()

                    }
                }
            }.addOnFailureListener{
                exception ->
                when(activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()                    }
                    is ProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Toast.makeText(activity, exception.toString(), Toast.LENGTH_LONG).show()
            }
    }

    fun getBoards(activity: MainActivity) {
        val userId = getCurrentUserId()
        fireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_USERS, userId)
            .get()
            .addOnSuccessListener {
                document ->
                val boards: ArrayList<Board> = ArrayList()
                for(b in document.documents) {
                    var board = b.toObject(Board::class.java)!!
                    board.firestoreDocumentId = b.id
                    boards.add(board)
                }

                activity.populateBoards(boards)
            }
            .addOnFailureListener {
                _ ->
                activity.hideProgressDialog()
            }
    }

    fun getBoardDetails(gameActivity: GameActivity, firebaseDocId: String) {
        fireStore.collection(Constants.BOARDS)
            .document(firebaseDocId)
            .get()
            .addOnSuccessListener {
                    document ->
                var board = document.toObject(Board::class.java)!!
                board.firestoreDocumentId = document.id
                gameActivity.boardDetails(board)
            }
            .addOnFailureListener {
                    _ ->
                    gameActivity.hideProgressDialog()
            }
    }

    fun addUpdateGameList(activity: Activity, board: Board) {
        val gameListHashMap = HashMap<String, Any>()
        gameListHashMap[Constants.GAME_LIST] = board.gameList

        fireStore.collection(Constants.BOARDS)
            .document(board.firestoreDocumentId)
            .update(gameListHashMap)
            .addOnSuccessListener {
                if (activity is GameActivity) {
                    activity.addUpdateGameListSuccess()
                } else if (activity is GameEventDetailsActivity) {
                    activity.addUpdateGameListSuccess()
                }
            }.addOnFailureListener{
                if (activity is GameActivity) {
                    activity.hideProgressDialog()
                }
            }

    }

    fun getAssignedUsers(activity: Activity, assignedTo: ArrayList<String>) {
        fireStore.collection(Constants.USERS)
            .whereIn(Constants.USER_ID, assignedTo)
            .get()
            .addOnSuccessListener {
                document ->
                    val users : ArrayList<User> = ArrayList()

                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    users.add(user)
                }
                if (activity is MembersActivity) {
                    activity.setUpMembersList(users)
                }
                else if (activity is GameActivity) {
                    activity.boardMembersDetailsList(users)
                }
            }.addOnFailureListener {
                if (activity is MembersActivity)
                    activity.hideProgressDialog()
                else if (activity is GameActivity)
                    activity.hideProgressDialog()
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        fireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                document ->
                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showError("NO MEMBERS FOUND")
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_USERS] = board.assignedUsers

        fireStore.collection(Constants.BOARDS)
            .document(board.firestoreDocumentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener{
                activity.hideProgressDialog()
            }

    }

}