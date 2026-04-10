package ru.netology.nmedia.repository


import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>

    suspend fun likeById(id: Long, likedByMe: Boolean): Post
    suspend fun save(post: Post, image: File?): Post
    suspend fun removeById(id: Long)

    fun getNewerCount(id: Long): Flow<Int>


}




