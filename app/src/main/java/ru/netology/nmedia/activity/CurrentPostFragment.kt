package ru.netology.nmedia.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.filter
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.FragmentCurrentPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel


@AndroidEntryPoint
class CurrentPostFragment : Fragment() {

    companion object {
        var Bundle.textArgument: String? by StringArg
    }

//    private val viewModel: PostViewModel by viewModels(
//        ownerProducer = ::requireParentFragment
//    )

    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCurrentPostBinding.inflate(
            inflater,
            container,
            false
        )

        val currentId = arguments?.textArgument?.toLong()


//        viewModel.data.observe(viewLifecycleOwner) { list ->
//            list.posts.find { it.id == currentId }?.let {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.getPostById(currentId ?: 0L).collectLatest { post ->
                    if (post.id == currentId) {
                        PostViewHolder(
                            binding.singlePost,
                            object : OnInteractionListener {
                                override fun like(post: Post) {
                                    if (authViewModel.authenticated) {
                                        viewModel.likeById(post)
                                    } else {
                                        mustSignIn()
                                    }
                                    //     viewModel.likeById(post)
                                }

                                override fun share(post: Post) {
                                    viewModel.share(post)

                                    val intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, post.content)
                                        type = "text/plain"
                                    }
                                    val shareIntent =
                                        Intent.createChooser(
                                            intent,
                                            getString(R.string.chooser_share_post)
                                        )
                                    startActivity(shareIntent)
                                }

                                override fun remove(post: Post) {
                                    viewModel.removeById(post)
                                    findNavController()
                                        .navigate(
                                            R.id.action_currentPostFragment_to_feedFragment
                                        )
                                }

                                override fun edit(post: Post) {
                                    viewModel.edit(post)

                                    findNavController()
                                        .navigate(R.id.action_currentPostFragment_to_newPostFragment,
                                            Bundle().apply {
                                                textArg = post.content
                                            })
                                }

                                override fun showVideo(post: Post) {
                                    val intentVideo = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(post.videoUrl)
                                    )
                                    startActivity(intentVideo)
                                }

                                override fun goToPost(post: Post) {

                                }

                                override fun syncPost() {

                                }

                                override fun syncOnePost(post: Post) {

                                }

                                override fun goToPhoto(id: Long) {
                                    findNavController()
                                        .navigate(R.id.action_currentPostFragment_to_currentPhotoFragment,
                                            Bundle().apply {
                                                textArg = id.toString()
                                            })
                                }

                                override fun onAdClick(ad: Ad) {

                                }
                            }).bind(post) //вызываем метод bind у PostViewHolder
                    }
                }
            }
        }


//        val currentId = arguments?.textArgument?.toLong()
//
//        val currentPost = viewModel.data.value
//            ?.first { it.id == currentId }
//
//
//
//        if (currentPost != null) {
//            binding.apply {
//                author.text = currentPost.author
//                published.text = currentPost.published
//                content.text = currentPost.content
//                like.isChecked = currentPost.likedByMe
//                like.text = CounterView.createCount(currentPost.likes)
//                share.text = CounterView.createCount(currentPost.shares)
//                if (currentPost.videoUrl.isNotEmpty()) {
//                    binding.groupVideo.visibility = View.VISIBLE
//                } else {
//                    binding.groupVideo.visibility = View.GONE
//                }
//                videoView.setOnClickListener {
//                    val intentVideo = Intent(Intent.ACTION_VIEW, Uri.parse(currentPost.videoUrl))
//                    startActivity(intentVideo)
//                    //onInteractionListener.showVideo(currentPost)
//                }
//                like.setOnClickListener {
//                    viewModel.like(currentPost.id)
//                    //onInteractionListener.like(post)
//                }
//                share.setOnClickListener {
//                    viewModel.share(currentPost.id)
//
//                    val intent = Intent().apply {
//                        action = Intent.ACTION_SEND
//                        putExtra(Intent.EXTRA_TEXT, currentPost.content)
//                        type = "text/plain"
//                    }
//
//                    val shareIntent =
//                        Intent.createChooser(intent, getString(R.string.chooser_share_post))
//                    startActivity(shareIntent)
//                    //onInteractionListener.share(post)
//                }
//
//                menu.setOnClickListener {
//                    PopupMenu(it.context, it).apply {
//                        inflate(R.menu.menu_options)
//                        setOnMenuItemClickListener { item ->
//                            when (item.itemId) {
//                                R.id.remove -> {
//                                    viewModel.removeById(currentPost.id)
//                                    findNavController()
//                                        .navigate(
//                                            R.id.action_currentPostFragment_to_feedFragment
//                                        )
//                                    //onInteractionListener.remove(post)
//                                    true
//                                }
//
//                                R.id.edit -> {
//                                    viewModel.edit(currentPost)
//
//                                    findNavController()
//                                        .navigate(R.id.action_currentPostFragment_to_newPostFragment,
//                                            Bundle().apply {
//                                                textArg = currentPost.content
//                                            })
//                                    //onInteractionListener.edit(post)
//                                    true
//                                }
//
//                                else -> false
//                            }
//                        }
//                    }.show()
//                }
//                watchCount.text = currentPost.watch.toString()
//            }
//        }
        return binding.root
    }

    fun mustSignIn() {
        val menuDialog = SignInOutDialogFragment(
            "Нужна регистрация",
            "Для этого действия необходимо войти в систему",
            R.drawable.info_24,
            "Sign In",
            "Позже"
        )
        val manager = childFragmentManager
        menuDialog.show(manager, "Sign in")
    }
}


