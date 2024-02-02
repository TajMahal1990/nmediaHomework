package ru.netology.nmedia.adapter

import android.util.Log
import android.util.LogPrinter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.constraintlayout.widget.Group
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.SeparatorPublishedBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.CounterView.createCount
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.SeparatorPublished
import ru.netology.nmedia.view.load


interface OnInteractionListener {
    fun like(post: Post)
    fun share(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun showVideo(post: Post)
    fun goToPost(post: Post)
    fun syncPost()
    fun syncOnePost(post: Post)
    fun goToPhoto(id: Long)
    fun onAdClick(ad: Ad)
}

class FeedAdapter(
    private val onInteractionListener: OnInteractionListener,

) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(FeedItemDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            is SeparatorPublished -> R.layout.separator_published
            else -> error("unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when(viewType) {
            R.layout.card_post -> {
                val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }
            R.layout.card_ad -> {
                val binding = CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding, onInteractionListener)
            }
            R.layout.separator_published -> {
                val binding = SeparatorPublishedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SeparatorPublishedViewHolder(binding)
            }

            else -> error("unknown viewType: $viewType")
        }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val post = getItem(position)
//        holder.bind(post)
  //      val post = getItem(position) ?: return
  //     getItem(position)?.let {
    //        holder.bind(it)
  //      }
        when(val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is SeparatorPublished -> (holder as? SeparatorPublishedViewHolder)?.bind(item)
            else -> error("unknown item type")
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {


    fun Group.setAllOnClickListener(listener: View.OnClickListener?) {
        referencedIds.forEach { id ->
            rootView.findViewById<View>(id).setOnClickListener(listener)
        }
    }

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published.toString()
            content.text = post.content
            like.isChecked = post.likedByMe
            like.text = createCount(post.likes)
            share.text = createCount(post.shares)

            menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            Glide.with(avatar)
                .load("http://192.168.1.10:9999/avatars/${post.authorAvatar}")
                .placeholder(R.drawable.baseline_emoji_emotions_24)
                .error(R.drawable.remove_red_eye_24)
                .timeout(10_000)
                .circleCrop()
                .into(avatar)

            photoImage.load("http://192.168.1.10:9999/media/${post.attachment?.url}")

            if (post.videoUrl != null) {
                groupVideo.visibility = View.VISIBLE
            } else {
                groupVideo.visibility = View.GONE
            }

            if (post.unSaved) {
                unSavedPost.visibility = View.VISIBLE
            } else {
                unSavedPost.visibility = View.GONE
            }

            unSavedPost.setOnClickListener {
                onInteractionListener.syncOnePost(post)
            }

//            if(post.attachment!=null) {
//                Glide.with (attachmentImage)
//                    .load("http://192.168.1.10:9999/images/${post.attachment.url}")
//                    .placeholder(R.drawable.baseline_autorenew_24)
//                    .error(R.drawable.ic_cancel_48)
//                    .timeout(10_000)
//                    .into(attachmentImage)
//                attachmentImage.visibility = View.VISIBLE
//            } else {
//                attachmentImage.visibility = View.GONE
//            }

            videoView.setOnClickListener {
                onInteractionListener.showVideo(post)
            }
            like.setOnClickListener {
                like.isChecked = !like.isChecked
                if (!post.unSaved) {
                    onInteractionListener.like(post)
                }
            }
            share.setOnClickListener {
                if (!post.unSaved) {
                    onInteractionListener.share(post)
                }
            }
            groupPost.setAllOnClickListener {
                Log.d("MyLog", "groupPost ${post.id}")
                onInteractionListener.goToPost(post)
            }

            if (post.attachment != null) {
                photoImage.visibility = View.VISIBLE
            } else {
                photoImage.visibility = View.GONE
            }

            photoImage.setOnClickListener {
                onInteractionListener.goToPhoto(post.id)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.menu_options)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.remove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.edit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
            watchCount.text = post.watches.toString()
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        binding.apply {
            image.load("${BuildConfig.BASE_URL}/media/${ad.image}")
            image.setOnClickListener {
                onInteractionListener.onAdClick(ad)
            }
        }
    }
}

class SeparatorPublishedViewHolder(
    private val binding: SeparatorPublishedBinding
):RecyclerView.ViewHolder(binding.root) {
    fun bind(separatorPublished: SeparatorPublished) {
        binding.textSeparator.text = separatorPublished.text
    }
}

class FeedItemDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
