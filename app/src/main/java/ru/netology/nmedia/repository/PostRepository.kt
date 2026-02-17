package ru.netology.nmedia.repository


import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long, likedByMe: Boolean): Post
    fun save(post: Post)
    fun removeById(id: Long)


    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: LikeCallback)
    fun saveAsync(post: Post, callback: SaveCallback)
    fun removeByIdAsync(id: Long, callback: RemoveCallback)
    fun getAllAsync(callback: GetAllCallback)


    interface GetAllCallback {
        fun onSuccess(posts: List<Post>)
        fun onError(e: Throwable)
    }

    interface LikeCallback {
        fun onSuccess(post: Post)
        fun onError(e: Throwable)
    }

    interface SaveCallback {
        fun onSuccess()
        fun onError(e: Throwable)
    }

    interface RemoveCallback {
        fun onSuccess()
        fun onError(e: Throwable)
    }


}
