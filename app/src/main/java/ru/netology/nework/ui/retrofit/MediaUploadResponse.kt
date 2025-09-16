package ru.netology.nework.ui.retrofit

import android.net.Uri
import java.io.File

data class MediaUploadResponse(val id: String)

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO
}

data class PhotoModel(val uri: Uri? = null, val file: File? = null)