package ru.netology.nmedia.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.AppError
import java.io.File


class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {

    override val data = postDao.getShown()
        .map { it.map { it.toDto() } }
    /* .flowOn(Dispatchers.Default)*/


    override suspend fun getAll() {
        try {
            val response = PostApi.service.getAll()

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val posts: List<Post> = response.body()
                ?: throw ApiError(response.code(), "Response body is empty")

            postDao.insert(posts.map { PostEntity.fromDto(it, shown = true) })
        } catch (e: Exception) {
            throw AppError.from(e)
        }
    }

    override suspend fun likeById(
        id: Long,
        likedByMe: Boolean
    ): Post {
        postDao.likeById(id)
        try {
            val response = if (!likedByMe) {
                PostApi.service.likeById(id)
            } else {
                PostApi.service.dislikeById(id)
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body()
                ?: throw ApiError(response.code(), "Response body is empty")
        } catch (e: Exception) {
            throw AppError.from(e)
        }
    }


    override suspend fun save(post: Post, image: File?): Post {
        try {

            val media = image?.let {
                upload(it)
            }

            val postWithAttachment = media?.let {
                post.copy(attachment = Attachment(url = it.id, AttachmentType.IMAGE))
            }?: post


            val response = PostApi.service.save(postWithAttachment)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body()
                ?: throw ApiError(response.code(), "Response body is empty")

            postDao.insert(PostEntity.fromDto(body))

            return body
        } catch (e: Exception) {
            throw AppError.from(e)
        }
    }

    private suspend fun upload(file: File): Media =
        PostApi.service.upload(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody()
            )
        )

    override suspend fun removeById(id: Long) {
        postDao.removeById(id)
        try {
            val response = PostApi.service.removeById(id)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: Exception) {
            throw AppError.from(e)
        }
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)

            val maxId = postDao.getMaxIdOnce()


            val response = PostApi.service.getNewer(maxId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            postDao.insert(body.map { PostEntity.fromDto(it, shown = false) })
            val hiddenCount = postDao.countHidden().first()
            emit(hiddenCount)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun showNewPosts() {
        postDao.showAll()
    }
}




