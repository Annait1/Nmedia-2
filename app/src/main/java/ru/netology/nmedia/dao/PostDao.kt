package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query(
        """
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
        """
    )
    suspend fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("SELECT * FROM PostEntity WHERE shown = 1 ORDER BY id DESC")
    fun getShown(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) FROM PostEntity WHERE shown = 0")
    fun countHidden(): Flow<Int>

    @Query("UPDATE PostEntity SET shown = 1 WHERE shown = 0")
    suspend fun showAll()

    @Query("SELECT COALESCE(MAX(id), 0) FROM PostEntity")
    suspend fun getMaxIdOnce(): Long

    @Query("SELECT MIN(id) FROM PostEntity")
    suspend fun getMinIdOnce(): Long?

   /* @Query("DELETE FROM PostEntity")
    suspend fun removeAll()*/
}
