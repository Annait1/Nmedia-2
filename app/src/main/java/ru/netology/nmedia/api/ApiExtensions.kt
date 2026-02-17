package ru.netology.nmedia.api

import retrofit2.Response
import ru.netology.nmedia.error.ApiError


fun <T> Response<T>.requireBody(): T {
    if (!isSuccessful) {
        val msg = errorBody()?.string().orEmpty().ifBlank { message() }
        throw ApiError(code(), msg)
    }

    return body() ?: throw ApiError(code(), "Body is null")
}