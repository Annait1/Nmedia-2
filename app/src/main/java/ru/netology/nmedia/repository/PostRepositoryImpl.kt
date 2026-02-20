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
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.AppError


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


    override suspend fun save(post: Post): Post {
        try {
            val response = PostApi.service.save(post)

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


/*override fun likeByIdAsync(
    id: Long,
    likedByMe: Boolean,
    callback: PostRepository.LikeCallback
) {
    val call = if (likedByMe) PostApi.service.dislikeById(id) else PostApi.service.likeById(id)

    call.enqueue(
        object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                try {
                    callback.onSuccess(response.requireBody())
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(
                    if (t is java.io.IOException)
                        NetworkError()
                    else
                        UnknownError(t)
                )
            }
        })
}


override fun saveAsync(
    post: Post,
    callback: PostRepository.SaveCallback
) {
    PostApi.service.save(post).enqueue(object : Callback<Post> {
        override fun onResponse(
            call: Call<Post>,
            response: Response<Post>
        ) {
            if (response.isSuccessful) callback.onSuccess()
            else {
                val msg = response.errorBody()?.string().orEmpty()
                    .ifBlank { response.message() }
                callback.onError(ApiError(response.code(), msg))
            }
        }

        override fun onFailure(call: Call<Post>, t: Throwable) {
            callback.onError(
                if (t is java.io.IOException)
                    NetworkError()
                else
                    UnknownError(t)
            )
        }
    })
}

override fun removeByIdAsync(
    id: Long,
    callback: PostRepository.RemoveCallback
) {
    PostApi.service.removeById(id).enqueue(object : Callback<Unit> {
        override fun onResponse(
            call: Call<Unit>,
            response: Response<Unit>
        ) {
            if (response.isSuccessful) callback.onSuccess()
            else {
                val msg = response.errorBody()?.string().orEmpty()
                    .ifBlank { response.message() }
                callback.onError(ApiError(response.code(), msg))
            }
        }

        override fun onFailure(call: Call<Unit>, t: Throwable) {
            callback.onError(
                if (t is java.io.IOException)
                    NetworkError()
                else
                    UnknownError(t)
            )
        }
    })
}


override fun getAllAsync(callback: PostRepository.GetAllCallback) {
    PostApi.service.getAll()
        .enqueue(object : Callback<List<Post>> {
            override fun onResponse(
                call: Call<List<Post>>,
                response: Response<List<Post>>
            ) {
                try {
                    callback.onSuccess(response.requireBody())
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                callback.onError(
                    if (t is java.io.IOException)
                        NetworkError()
                    else
                        UnknownError(t)
                )
            }
        })
}*/



