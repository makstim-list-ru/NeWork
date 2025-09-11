package ru.netology.nework.ui.retrofit


sealed interface FeedItem {
    val id: Long
}

data class Ad(
    override val id: Long,
    val image: String
) : FeedItem

data class Post(
    override val id: Long = 0,
    val authorId: Long = 0,
    val author: String = "",
    val authorAvatar: String = "",
    val published: String = "",
    val content: String = "",
    val likedByMe: Boolean = false,
    val likes: Long = 999,
    val sharesNum: Long = 99,
    val seenNum: Long = 9_999,
    val video: String = "",
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
) : FeedItem

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

val postEmpty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    published = "",
    content = "",
    likedByMe = false,
    likes = 0,
    sharesNum = 0,
    seenNum = 0,
    video = "",
    attachment = null,
    ownedByMe = false,
)




