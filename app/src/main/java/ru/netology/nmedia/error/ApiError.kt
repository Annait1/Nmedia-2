package ru.netology.nmedia.error


class ApiError(
    val code: Int,
    message: String
) : RuntimeException(message)

class NetworkError : RuntimeException("Network error")

class UnknownError(
    cause: Throwable
) : RuntimeException(cause)