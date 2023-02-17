package com.mrawluk.gameboardmaster.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val USERS: String = "Users"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val EMAIL: String = "email"

    const val BOARDS: String = "boards"
    const val ASSIGNED_USERS: String = "assignedUsers"
    const val BOARD_MEMBERS_LIST: String = "board_members_list"

    const val GAME_LIST: String = "gameList"

    const val BOARD_DETAIL: String = "board_detail"
    const val USER_ID: String = "id"

    const val GAME_LIST_ITEM_POS: String = "game_list_item_position"
    const val GAME_EVENT_LIST_ITEM_POS: String = "game_event_list_item_position"
    const val SELECT: String = "Select"
    const val UNSELECT: String = "Unselect"

    const val APP_PREFERENCES = "Gamemaster_Preferences"
    const val TOKEN_UPDATED = "tokenUpdated"
    const val TOKEN = "token"

    const val FCM_BASE_URL:String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_SERVER_KEY:String = "AAAAtqRNEx0:APA91bG2j3F9qFGbwtywcsMdjDDGU3ERyXHZYhYmXq7V5vDuyIBgxNNbZErqX4nZa5ki1BzyksJ3LljbYryXj3STdmMAKrdMWNVcQyvgnGOTvk7QewoNPIC9fBu3w1iA0dV4Cn6ksTze"
    const val FCM_KEY_TITLE:String = "title"
    const val FCM_KEY_MESSAGE:String = "body"

    fun showImageGallery(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(uri: Uri?, activity: Activity): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}