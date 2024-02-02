package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.enumeration.AttachmentType

@Dao
interface PostDao {
//        @Query("SELECT * FROM PostEntity ORDER BY id DESC")
//    fun getAll(): Flow<List<PostEntity>>
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>

    @Query("DELETE FROM PostEntity")
    suspend fun clear()

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    fun getPostById(id: Long): Flow<PostEntity>

    @Query("SELECT * FROM PostEntity WHERE hidden=0 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM PostEntity")
    suspend fun count(): Long

    @Query("SELECT COUNT(id) FROM PostEntity WHERE hidden=0")
    suspend fun countHidden(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    suspend fun likeById(id: Long)

    @Query(
        """
           UPDATE PostEntity SET
               shares = shares + 1
           WHERE id = :id
        """
    )
    fun shareById(id: Long)

    @Query(
        """UPDATE PostEntity SET 
                unSaved = :unSaved, 
                authorAvatar = :authorAva              
                WHERE id = :id"""
    )
    suspend fun updateUnSavedById(id: Long, unSaved: Boolean, authorAva:String)

    @Query(
        """UPDATE PostEntity SET 
                            author = :author,
                            authorAvatar = :authorAvatar,
                            content = :content,
                            published = :published,
                unSaved = :unSaved
                WHERE id = :id"""
    )
    suspend fun updateUnSavedByPost(id: Long, author:String, authorAvatar:String, content:String, published:Long, unSaved: Boolean)

    @Query(
        """UPDATE PostEntity 
            SET hidden = 0              
        """
    )
    suspend fun updateHiddenAll()

    @Query("SELECT MAX(id) FROM PostEntity")
    suspend fun getMaxId(): Long?

    @Query("SELECT COUNT(*) FROM PostEntity WHERE hidden = 1")
    suspend fun getHiddenCount(): Int

}

class Converters {
    @TypeConverter
    fun toAttachmentType(value: String) = enumValueOf<AttachmentType>(value)
    @TypeConverter
    fun fromAttachmentType(value: AttachmentType) = value.name
}

//@Dao
//interface PostDao {
//    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
//    fun getAll(): LiveData<List<PostEntity>>
//
//    @Insert
//    fun insert(post: PostEntity)
//
//    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
//    fun updateContentById(id: Long, content: String)
//
//    fun save(post: PostEntity) =
//        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)
//
//    @Query(
//        """
//        UPDATE PostEntity SET
//        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
//        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
//        WHERE id = :id
//        """
//    )
//    fun likeById(id: Long)
//
//    @Query("DELETE FROM PostEntity WHERE id = :id")
//    fun removeById(id: Long)
//
//
//    @Query(
//        """
//           UPDATE PostEntity SET
//               shares = shares + 1
//           WHERE id = :id
//        """
//    )
//    fun shareById(id: Long)

//fun getAll(): List<Post>
//fun save(post: Post): Post
//fun likeById(id: Long)
//fun removeById(id: Long)
//fun shareById(id: Long)
//}