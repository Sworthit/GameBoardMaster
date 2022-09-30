package com.mrawluk.taskmaster.utils

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
    const val CREATED_BY: String = "createdBy"
    const val ASSIGNED_USERS: String = "assignedUsers"

    const val TASK_LIST: String = "taskList"

    const val BOARD_DETAIL: String = "board_detail"
    const val USER_ID: String = "id"

    const val TASK_LIST_ITEM_POS: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POS: String = "card_list_item_position"

    fun showImageGallery(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(uri: Uri?, activity: Activity): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}