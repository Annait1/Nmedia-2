package ru.netology.nmedia.dto



data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val authorAvatar: String? = "",
    val attachment: Attachment? = null,
    val authorId: Long,
    val ownedByMe: Boolean = false,
)
data class Attachment(
    val url: String,
    val type: AttachmentType,
)

enum class AttachmentType {
    IMAGE
}


