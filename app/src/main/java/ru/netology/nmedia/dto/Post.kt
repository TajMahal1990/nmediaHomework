package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

sealed class FeedItem{
    abstract val id:Long
}

data class Ad(
    override val id: Long,
    val url: String,
    val image: String,
) : FeedItem()

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar:String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val shares: Int = 0,
    val watches: Int = 0,
    val videoUrl:String? = null,
    var unSaved:Boolean = true,
    var hidden:Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
):FeedItem()

data class SeparatorPublished(
    override val id: Long,
    val text:String
):FeedItem()

//data class LoadStateItem(
//    override val id:Long
//):FeedItem()
data class Attachment(
    val url: String,
    val type: AttachmentType,
)

//data class Attachment(
//    val url:String,
//    val description:String,
//    val type:String
//)

