package ru.netology.nework.ui.retrofit

data class Post(
    val id: Long = 0,               //*
    val authorId: Long = 0,         //*
    val author: String = "",        //*
    val authorJob: String? = null,
    val authorAvatar: String? = null,
    val content: String = "",       //*
    val published: String = "1970-01-01T00:00:00.000Z",     //*
    val mentionIds: List<Long> = emptyList(),   //*
    val mentionedMe: Boolean = false,           //*
    val likeOwnerIds: List<Long> = emptyList(),  //*
    val likedByMe: Boolean = false, //*
    val video: String = "",
    val attachment: Attachment? = null,
    val users: Map<Long, UserPreview> = emptyMap(),   //*
    val ownedByMe: Boolean = false,
)

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

data class UserPreview(
    val name: String = "",
    val avatar: String? = null,
)

val postByDefault = Post()




