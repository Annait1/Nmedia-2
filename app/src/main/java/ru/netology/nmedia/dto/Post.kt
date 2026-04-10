package ru.netology.nmedia.dto

sealed class FeedItem{
    abstract val id: Long
}

data class Post(
    override val id: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String? = "",
    val attachment: Attachment? = null,
    val authorId: Long,
    val ownedByMe: Boolean = false,
): FeedItem()

data class Ad(
    override val id: Long,
 /*   val url: String,*/
    val image: String,
) : FeedItem()
data class Attachment(
    val url: String,
    val type: AttachmentType,
)

enum class AttachmentType {
    IMAGE
}


