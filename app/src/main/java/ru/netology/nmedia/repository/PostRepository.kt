package ru.netology.nmedia.repository


import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun likeById(id: Long, likedByMe: Boolean): Post
    suspend fun save(post: Post): Post
    suspend fun removeById(id: Long)

    fun getNewerCount(id: Long): Flow<Int>
    suspend fun showNewPosts()

}

//колбэк методы, удалю
/*    fun likeByIdAsync(id: Long, likedByMe: Boolean, callback: LikeCallback)
    fun saveAsync(post: Post, callback: SaveCallback)
    fun removeByIdAsync(id: Long, callback: RemoveCallback)
    fun getAllAsync(callback: GetAllCallback)*/


/*    interface GetAllCallback {
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
    }*/



