package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.NewerCount
import ru.netology.nmedia.dto.Post

interface PostRepository {

   // val data: Flow<List<Post>>
    // val data: Flow<PagingData<Post>>
    val data:Flow<PagingData<FeedItem>>
    suspend fun getAll()
  //  fun getNewerCount(id: Long): Flow<Int>

    suspend fun getLatest()

    fun getNewerCount(): Flow<NewerCount>

    fun getNewer():Flow<Int>
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun upload(upload: MediaUpload): Media

    suspend fun removeById(post: Post)
    suspend fun likeById(post: Post)

    suspend fun syncPost(list: List<Post>)
    suspend fun syncOnePost(post: Post)
    suspend fun changeHidden()

    suspend fun getPostById(id: Long): Flow<Post>

 //   suspend fun getCount():Long
  //  suspend fun getHiddenCount():Int

//    fun getAll(): List<Post>
 //   fun likeById(post: Post):Post
     fun shareById(post: Post)
  //  fun removeById(id: Long)
    //fun save(post: Post)

    //fun getAllAsync(callback: RepositoryCallback<List<Post>>)

//    interface GetAllCallback {
//        fun onSuccess(posts: List<Post>) {}
//        fun onError(e: Exception) {}
//    }

    //fun likeByIdAsync(post: Post, callback:RepositoryCallback<Post>)

//    interface LikeCallback {
//        fun onSuccess(post:Post) {}
//        fun onError(e: Exception) {}
//    }

    //fun removeByIdAsync(id: Long, callback: RepositoryCallback<Long>)

//    interface RemoveCallback {
//        fun onSuccess (id: Long) {}
//        fun onError(e: Exception) {}
//    }



//    fun getAllAsync(callback: Callback<List<Post>>)
//    fun saveAsync(post: Post, callback: Callback<Unit>)
//    fun removeByIdAsync(id: Long, callback: Callback<Unit>)
//    fun likeByIdAsync(post: Post, callback: Callback<Post>)
//
//    interface Callback<T> {
//        fun onSuccess(result: T) {}
//        fun onError(e: Exception) {}
//    }

//    interface SaveCallback {
//        fun onSuccess(value:Unit) {}
//        fun onError(e:Exception) {}
//    }


//    interface RepositoryCallback<T> {
//        fun onSuccess(result:T) {}
//        fun onError(e: Exception) {}
//
//    }
}