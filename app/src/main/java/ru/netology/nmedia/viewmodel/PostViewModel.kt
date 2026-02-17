package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated


    init {
        loadPosts()
    }

    /* fun loadPosts() {
         thread {
             // Начинаем загрузку
             _data.postValue(FeedModel(loading = true))
             try {
                 // Данные успешно получены
                 val posts = repository.getAll()
                 FeedModel(posts = posts, empty = posts.isEmpty())
             } catch (e: IOException) {
                 // Получена ошибка
                 FeedModel(error = true)
             }.also(_data::postValue)
         }
     }*/

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun save() {
        edited.value?.let { post ->
            repository.saveAsync(post, object : PostRepository.SaveCallback {
                override fun onSuccess() {
                    _postCreated.postValue(Unit)
                }

                override fun onError(e: Exception) {
                    _data.postValue((_data.value ?: FeedModel()).copy(error = true))
                }
            })
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long, likedByMe: Boolean) {
        repository.likeByIdAsync(id, likedByMe, object : PostRepository.LikeCallback {
            override fun onSuccess(updatedPost: Post) {
                val current = _data.value ?: FeedModel()
                val updatedList = current.posts.map { post ->
                    if (post.id == updatedPost.id) updatedPost else post
                }
                _data.postValue(current.copy(posts = updatedList))
            }

            override fun onError(e: Exception) {
                _data.postValue((_data.value ?: FeedModel()).copy(error = true))
            }
        })
    }

    fun removeById(id: Long) {

            // Оптимистичная модель
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(
                    posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }
                )
            )
            repository.removeByIdAsync(id, object : PostRepository.RemoveCallback {
                override fun onSuccess() {
                    // ничего
                }

                override fun onError(e: Exception) {
                    _data.postValue(_data.value?.copy(posts = old))
                }
            })
        }

    }


