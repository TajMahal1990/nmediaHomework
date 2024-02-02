package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

//class PostRepositoryJsonImpl(
//    context: Context,
//) : PostRepository {
//    private val gson = Gson()
//    private val prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)
//    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
//    private val postKey = "posts"
//    private val nextIdKey = "next_id"
//    private var nextId = 1L
//    private var posts = emptyList<Post>()
//        set(value) {
//            field = value
//            data.value = value
//            sync()
//        }
//
//    private val data = MutableLiveData(posts)
//
//    init {
//        prefs.getString(postKey, null)?.let {
//            posts = gson.fromJson(it, type)
//
//        }
//        nextId = prefs.getLong(nextIdKey, nextId)
//        data.value = posts
//    }
//
////    init {
////        posts = prefs.getString(postKey, null)?.let {
////            gson.fromJson<List<Post>>(it,type)
////        } .orEmpty()
////        nextId = prefs.getLong(nextIdKey, nextId)
////        data.value = posts
////    }
//
//    override fun getAll(): LiveData<List<Post>> = data
//
//    override fun save(post: Post) {
//        if (post.id == 0L) {
//            posts = listOf(
//                post.copy(
//                    id = nextId++,
//                    author = "Me",
//                    likedByMe = false,
//                    published = "now"
//                )
//            ) + posts
//
//        }
//
//        posts = posts.map {
//            if (it.id != post.id) it else it.copy(content = post.content)
//        }
//    }
//
//    override fun likeById(id: Long) {
//        posts = posts.map {
//            if (it.id != id) it else it.copy(
//                likedByMe = !it.likedByMe,
//                likes = if (it.likedByMe) (it.likes - 1) else (it.likes + 1)
//            )
//        }
//    }
//
//    override fun shareById(id: Long) {
//        posts = posts.map {
//            if (it.id != id) it else it.copy(shares = (it.shares + 1))
//        }
//    }
//
//    override fun removeById(id: Long) {
//        posts = posts.filter { it.id != id }
//    }
//
//    private fun sync() {
//        with(prefs.edit()) {
//            putString(postKey, gson.toJson(posts))
//            putLong(nextIdKey, nextId)
//            apply()
//        }
//    }
//}