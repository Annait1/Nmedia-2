package ru.netology.nmedia.repository



import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post

import retrofit2.Callback
import retrofit2.Response
import retrofit2.Call
import ru.netology.nmedia.api.requireBody
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError


class PostRepositoryImpl : PostRepository {


    override fun getAll(): List<Post> = PostApi.service.getAll()
        .execute()
        .requireBody()


    override fun likeById(id: Long, likedByMe: Boolean): Post {
        val call = if (likedByMe) {
            PostApi.service.dislikeById(id)
        } else {
            PostApi.service.likeById(id)
        }
        return call.execute().requireBody()
    }

    override fun save(post: Post) {
        PostApi.service.save(post)
            .execute()
            .requireBody()

    }

    override fun removeById(id: Long) {
        val response = PostApi.service.removeById(id).execute()
        if (!response.isSuccessful) {
            val msg = response.errorBody()?.string().orEmpty().ifBlank { response.message() }
            throw ApiError(response.code(), msg)
        }
    }


    override fun likeByIdAsync(
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
    }
}


