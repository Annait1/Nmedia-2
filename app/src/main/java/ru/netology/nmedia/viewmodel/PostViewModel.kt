package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File


private val empty = Post(
    id = 0L,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = 0L,
    attachment = null,
    authorAvatar = "",
    authorId = 0,

    )

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl(
        AppDb.getInstance(context = application).postDao()
    )


    val data: LiveData<FeedModel> = AppAuth.getInstance()
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.data
                .map { posts ->
                    FeedModel(
                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
                        posts.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)


    val newerCount: LiveData<Int> =
        repository.getNewerCount(0L)
            .catch { _state.postValue(FeedModelState(error = true)) }
            .asLiveData(Dispatchers.Default)

    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state


    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo


    init {
        loadPosts()
    }


    fun loadPosts() {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            _state.value = try {
                repository.getAll()
                FeedModelState()
            } catch (e: Exception) {
                FeedModelState(error = true, errorMessage = errorMessage(e))
            }

        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            _state.value = FeedModelState(refreshing = true)
            _state.value = try {
                repository.getAll()
                FeedModelState()
            } catch (e: Exception) {
                FeedModelState(error = true, errorMessage = errorMessage(e))
            }

        }
    }

    fun save() {
        viewModelScope.launch {
            _state.value = try {
                edited.value?.let {
                    repository.save(it, _photo.value?.file)
                    _postCreated.value = Unit
                }
                FeedModelState()
            } catch (e: Exception) {
                FeedModelState(error = true, errorMessage = errorMessage(e))
            }
            edited.value = empty
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
        viewModelScope.launch {
            _state.value = try {
                repository.likeById(id, likedByMe)
                FeedModelState()
            } catch (e: Exception) {
                FeedModelState(error = true, errorMessage = errorMessage(e))
            }
        }
    }


    fun removeById(id: Long) {
        viewModelScope.launch {
            _state.value = try {
                repository.removeById(id)
                FeedModelState()
            } catch (e: Exception) {
                FeedModelState(error = true, errorMessage = errorMessage(e))
            }

        }

    }

    fun showNewPosts() = viewModelScope.launch {
        repository.showNewPosts()
    }

    private fun errorMessage(e: Throwable): Int = when (e) {
        is NetworkError -> R.string.error_network
        is ApiError -> R.string.error_server
        is UnknownError -> R.string.error_unknown
        else -> R.string.error_unknown

    }

    fun changePhoto(uri: Uri, file: File) {
        _photo.value = PhotoModel(uri, file)
    }

    fun removePhoto() {
        _photo.value = null
    }


}


