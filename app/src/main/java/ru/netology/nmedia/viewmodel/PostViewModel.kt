package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.switchMap
import kotlinx.coroutines.flow.transformLatest
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import kotlin.coroutines.EmptyCoroutineContext
import java.util.Locale.filter
import javax.inject.Inject


private val empty = Post(
    id = 0,
    authorId = 0,
    content = "",
    author = "",
    authorAvatar = "",
    published = 0,
    likedByMe = false,
    likes = 0,
    shares = 0,
    watches = 0,
    videoUrl = null,
    unSaved = true,
    hidden = false,
    attachment = null,
    ownedByMe = false
)

val noPhoto = PhotoModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    auth: AppAuth
) : ViewModel() {


    //private val repository: PostRepository = PostRepositoryImpl()
//    private val repository: PostRepository =
//        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

//    val data: LiveData<FeedModel> = repository.data.map(::FeedModel)

//    val data: LiveData<FeedModel> = repository.data
//        .map(::FeedModel)
//        .asLiveData(Dispatchers.Default, 100)


//    val data: LiveData<FeedModel> = auth
//        .authStateFlow
//        .flatMapLatest { (myId, _) ->
//            repository.data
//                .map { posts ->
//                    FeedModel(
//                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
//                        posts.isEmpty()
//                    )
//                }
//        }.asLiveData(Dispatchers.Default, 100)

//    private val cached = repository
//        .data
//        .cachedIn(viewModelScope)

//    val data: Flow<PagingData<Post>> = auth.authStateFlow
//        .flatMapLatest { (myId, _) ->
//            cached.map { pagingData ->
//                pagingData.map { post ->
//                    post.copy(ownedByMe = post.authorId == myId)
//                }
//            }
//        }

    val data: Flow<PagingData<FeedItem>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.data.map { pagingData ->
                pagingData.map { feedItem ->
                    if(feedItem is Post) {
                        feedItem.copy(ownedByMe = feedItem.authorId == myId)
                    } else feedItem
                }
            }
        }

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState


    val newerCountOld = data.flatMapLatest {
        repository.getNewer()
            .catch { e -> e.printStackTrace() }
    }.asLiveData(Dispatchers.Default)

    val newerCount = data.flatMapLatest {
        repository.getNewerCount()
            .map {
                it.count
            }
            .catch { e -> e.printStackTrace() }
    }.asLiveData(Dispatchers.Default)


//    val newerCount: LiveData<Int> = data.switchMap {
//        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
//            .catch { e -> e.printStackTrace() }
//            .asLiveData(Dispatchers.Default)
//    }

//    var count:Int = 0
//
//    fun getHiddenCount() = viewModelScope.launch {
//        try {
//            count = repository.getHiddenCount()
//        } catch (e: Exception) {
//            _dataState.value = FeedModelState(error = true)
//        }
//    }


//    val newerCount: LiveData<Int> = data.switchMap {
//        repository.getNewerCount()
//            .catch { e -> e.printStackTrace() }
//            .asLiveData(Dispatchers.Default)
//    }


    //   suspend fun getC():Long = viewModelScope.launch {
//            repository.getCount()
//        }
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

//    private val _data = MutableLiveData(FeedModel())
//    val data: LiveData<FeedModel>
//        get() = _data
//    val edited = MutableLiveData(empty)
//    private val _postCreated = SingleLiveEvent<Unit>()
//    val postCreated: LiveData<Unit>
//        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
        //    repository.getAll()
            repository.getLatest()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            // Toast.makeText(getApplication(), "Не удалось скачать посты.\nПопробуйте снова", Toast.LENGTH_SHORT).show()
        }
    }

//    fun checkEmpty() = viewModelScope.launch {
//        data.value = data.value?.copy(posts = data.value!!.posts, empty = data.value!!.posts.isEmpty())
//    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            //   repository.changeHidden()
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
            // Toast.makeText(getApplication(), "Не удалось обновить посты.\nПопробуйте снова", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getPostById(id: Long) = repository.getPostById(id)

    //    fun save() {
//        edited.value?.let {
//            _postCreated.value = Unit
//            viewModelScope.launch {
//                try {
//                    repository.save(it)
//                //    repository.syncOnePost(it)
//                    _dataState.value = FeedModelState()
//                } catch (e: Exception) {
//                    _dataState.value = FeedModelState(error = true)
//
//                }
//            }
//        }
//        edited.value = empty
//    }
    fun save() {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> repository.save(it)
                        else -> _photo.value?.file?.let { file ->
                            repository.saveWithAttachment(it, MediaUpload(file))
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

//    fun syncPost() {
//        viewModelScope.launch {
//            try {
//                val listUnSaved = data.value?.posts
//                ?.filter {it.unSaved}
//                .orEmpty()
//                _dataState.value = FeedModelState(loading = true)
//                repository.syncPost(listUnSaved)
//                _dataState.value = FeedModelState()
//            } catch (e:Exception) {
//                _dataState.value = FeedModelState(error = true)
//            }
//        }
//    }

    fun syncOnePost(post: Post) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.syncOnePost(post)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun changeHidden() {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.changeHidden()
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

//    fun loadPosts() {
//        thread {
//            // Начинаем загрузку
//            val value = _data.value
//            if (value != null) {
//                if (!value.refreshing)
//                    _data.postValue(FeedModel(loading = true))
//            }
//            try {
//                // Данные успешно получены
//                val posts = repository.getAll()
//                FeedModel(posts = posts, empty = posts.isEmpty())
//            } catch (e: IOException) {
//                // Получена ошибка
//                FeedModel(error = true)
//            }.also(_data::postValue)
//        }
//    }

//    fun loadPosts() {
//        if (_data.value != null) {
//            _data.value = FeedModel(loading = !_data.value!!.refreshing)
//            //_data.value = FeedModel(loading = true)
//        }
//        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
//            override fun onSuccess(result: List<Post>) {
//                _data.value = FeedModel(posts = result, empty = result.isEmpty())
//            }
//
//            override fun onError(e: Exception) {
//                _data.value = FeedModel(error = true)
//                Toast.makeText(getApplication(), "Не удалось обновить посты.\nПопробуйте снова", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

//    fun save() {
//        edited.value?.let {
//            thread {
//                repository.save(it)
//                _postCreated.postValue(Unit)
//            }
//        }
//        edited.postValue(empty)
//    }

//    fun save() {
//        edited.value?.let {
//            repository.saveAsync(it, object :
//            PostRepository.Callback<Unit>{
//                override fun onSuccess(value: Unit) {
//                    _postCreated.value = Unit
//                }
//
//                override fun onError(e: Exception) {
//                    _data.value = FeedModel(error = true)
//                }
//            })
//        }
//        edited.value = empty
//    }

//    fun saveNew() {
//        thread {
//            edited.value?.let { edited->
//                val post = repository.save(edited)
//                val value = _data.value
//
//                val updatedPosts = value?.posts?.map {
//                    if (it.id==edited.id) {
//                        post
//                    } else {
//                        it
//                    }
//                }.orEmpty()
//
//                val result = if (value?.posts==updatedPosts) {
//                    listOf(post)+updatedPosts
//                } else {
//                    updatedPosts
//                }
//                _data.postValue(value?.copy(posts = result))
//            }
//            edited.postValue(empty)
//            _postCreated.postValue(Unit)
//        }
//    }

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

//    fun likeById(post: Post) {
//        thread {
//            //Log.d("MyLog", "viewModel до ${_data.value?.posts?.filter { it.id == post.id }.toString()}")
//
//            val newPost = repository.likeById(post)
//
//            //Log.d("MyLog", "newPost ${newPost.toString()}")
//
//            _data.postValue(FeedModel(posts = data.value?.posts?.map {
//                if (it.id != newPost.id) it else newPost
//            }.orEmpty()))
//        }
//    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.likeById(post)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)

            }
        }

    }

//    fun likeById(post: Post) {
//
//        //Log.d("MyLog", "viewModel до ${_data.value?.posts?.filter { it.id == post.id }.toString()}")
//
//        repository.likeByIdAsync(post, object : PostRepository.RepositoryCallback<Post> {
//            override fun onSuccess(result: Post) {
//                _data.postValue(FeedModel(posts = data.value?.posts?.map {
//                    if (it.id != result.id) it else result
//                }.orEmpty()))
//            }
//
//            override fun onError(e: Exception) {
//                _data.postValue(FeedModel(posts = data.value?.posts.orEmpty(), error = true))
//            }
//        })
//
//        //Log.d("MyLog", "newPost ${newPost.toString()}")
//    }

//    fun likeById(post: Post) {
//
//        repository.likeByIdAsync(post, object : PostRepository.Callback<Post> {
//            override fun onSuccess(result: Post) {
//                Log.d("MyLog", "$result")
//                _data.value = _data.value?.copy(posts = data.value?.posts
//                    ?.map {
//                    if (it.id != result.id) it else result
//                }.orEmpty()
//                )
//            }
//            override fun onError(e: Exception) {
//                //_data.postValue(FeedModel(posts = data.value?.posts.orEmpty(), error = true))
//               // _data.value = FeedModel(error = true)
//                Toast.makeText(getApplication(), "Не удалось отправить лайк.\nПопробуйте снова", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

    fun share(post: Post) {
        repository.shareById(post)
    }

//    fun removeById(id: Long) {
//        thread {
//            // Оптимистичная модель
//            val old = _data.value?.posts.orEmpty()
//            _data.postValue(
//                _data.value?.copy(posts = _data.value?.posts.orEmpty()
//                    .filter { it.id != id }
//                )
//            )
//            try {
//                repository.removeById(id)
//            } catch (e: IOException) {
//                _data.postValue(_data.value?.copy(posts = old))
//            }
//        }
//    }

    fun removeById(post: Post) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.removeById(post)
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)

            }
        }
    }

//    fun removeById(id: Long) {
//
//        //val old = _data.value?.posts.orEmpty()
//        repository.removeByIdAsync(id, object : PostRepository.Callback<Unit> {
//            override fun onSuccess(result:Unit) {
////                _data.postValue(
////                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
////                        .filter { it.id != id }
////                    ))
//                _data.value = _data.value?.copy(posts = _data.value?.posts
//                    .orEmpty()
//                    .filter { it.id != id })
//            }
//            override fun onError(e: Exception) {
//                //_data.postValue(FeedModel(posts = data.value?.posts.orEmpty(), error = true))
//                _data.value = FeedModel(error = true)
//                Toast.makeText(getApplication(), "Не удалось удалить пост.\nПопробуйте снова", Toast.LENGTH_SHORT).show()
//                // _data.postValue(_data.value?.copy(posts = old))
//            }
//        })
//    }


//    val data = repository.getAll()
//
//    val edited = MutableLiveData(empty)
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun changeContentAndSave(content: String) {
//
//        val dateTime = LocalDateTime.now()
//            .format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a"))
//
//        edited.value?.let {
//            val text = content.trim()
//            if (it.content != text) {
//                repository.save(it.copy(content = text, published = dateTime))
//            }
//        }
//        edited.value = empty
//    }
//
//    fun like(id: Long) = repository.likeById(id)
//    fun share(id: Long) = repository.shareById(id)
//    fun removeById(id: Long) = repository.removeById(id)
//    fun edit(post: Post?) {
//        edited.value = post
//    }
}