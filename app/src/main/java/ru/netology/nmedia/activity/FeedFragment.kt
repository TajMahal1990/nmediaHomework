package ru.netology.nmedia.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingSource
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.internal.addHeaderLenient
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.CurrentPostFragment.Companion.textArgument
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.FeedAdapter
import ru.netology.nmedia.adapter.OnInteractionListenerLoadState
import ru.netology.nmedia.adapter.PagingLoadStateAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    private  val authViewModel:AuthViewModel by activityViewModels()

    lateinit var binding:FragmentFeedBinding

//    private val viewModel: PostViewModel by viewModels(
//        ownerProducer = ::requireParentFragment
//    )

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = FeedAdapter(object : OnInteractionListener {

            override fun like(post: Post) {
                if(authViewModel.authenticated) {
                    viewModel.likeById(post)
                } else {
                    mustSignIn()
             //       post =post.copy(likedByMe = !post.likedByMe)
                }

            }

            override fun share(post: Post) {
                viewModel.share(post)

                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun remove(post: Post) {
               // viewModel.removeById(post.id)
                viewModel.removeById(post)
            }

            override fun edit(post: Post) {
                viewModel.edit(post)

                findNavController()
                    .navigate(R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply {
                            textArg = post.content
                        })
            }

            override fun showVideo(post: Post) {
                val intentVideo = Intent(Intent.ACTION_VIEW, Uri.parse(post.videoUrl))
                startActivity(intentVideo)
            }

            override fun goToPost(post: Post) {

                findNavController()
                    .navigate(R.id.action_feedFragment_to_currentPostFragment,
                        Bundle().apply {
                            textArgument = post.id.toString()
                        })
            }

            override fun syncPost() {

            }

            override fun syncOnePost(post: Post) {
               // viewModel.syncPost()
                viewModel.syncOnePost(post)
            }

            override fun goToPhoto(id: Long) {
                findNavController()
                    .navigate(R.id.action_feedFragment_to_currentPhotoFragment,
                        Bundle().apply {
                            textArgument = id.toString()
                        })
            }

            override fun onAdClick(ad: Ad) {

            }

        })

     //   binding.list.adapter = adapter

        val header = PagingLoadStateAdapter(object :OnInteractionListenerLoadState{
            override fun onRetry() {
                adapter.retry()
            }
        })
//        adapter.addLoadStateListener { loadStates ->
//            header.loadState = loadStates.prepend
//        }
        val footer = PagingLoadStateAdapter(object :OnInteractionListenerLoadState{
            override fun onRetry() {
                adapter.retry()
            }
        })
//        adapter.addLoadStateListener { loadStates ->
//            footer.loadState = loadStates.append
//        }

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = header,
            footer = footer,
            )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        // добавление эффекта свайп
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.START or ItemTouchHelper.END
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {

                println("DO SOMETHING")
            }
        }).attachToRecyclerView(binding.list)

        //добавить линию разграничения между постами
        binding.list.addItemDecoration(
            DividerItemDecoration(
                binding.list.context,
                DividerItemDecoration.VERTICAL
            )
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swipeRW.isRefreshing =
                        state.refresh is LoadState.Loading
                            //    ||  state.prepend is LoadState.Loading ||
                             //   state.append is LoadState.Loading
                }
            }
        }

        binding.swipeRW.setOnRefreshListener(adapter::refresh)

//        viewModel.dataState.observe(viewLifecycleOwner) { state ->
//            binding.progress.isVisible = state.loading
//            binding.swipeRW.isRefreshing = state.refreshing
//            if (state.error) {
//                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
//                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
//                    .show()
//            }
//        }

//        viewModel.data.observe(viewLifecycleOwner) { state ->
//            val newPost = state.posts.size > adapter.currentList.size
//            adapter.submitList(state.posts) {
//                if (newPost) {
//                    binding.list.smoothScrollToPosition(0)
//                }
//            }
//            binding.emptyText.isVisible = state.empty
//        }

        viewModel.newerCount.observe(viewLifecycleOwner) { state ->
            if(state!=0L) {
                  binding.newPost.setIconTintResource(R.color.red)
            //    binding.newPost.setBackgroundColor(Color.parseColor("#FF0000"))
                binding.newPost.isEnabled = true
                binding.buttonNewPosts.visibility = View.VISIBLE
            //    binding.newPost.setTextColor(R.color.red)
                binding.newPost.text = state.toString()
            } else {
                binding.newPost.setIconTintResource(R.color.purple_700)
            //    binding.newPost.setBackgroundColor(resources.getColor(R.color.purple_700, null))
                binding.newPost.isEnabled = false
                binding.buttonNewPosts.visibility = View.GONE
                binding.newPost.text = ""
            }
            println(state)
        }

        binding.newPost.setOnClickListener {
            showNewPost()
        }

        binding.buttonNewPosts.setOnClickListener {
            showNewPost()
        }

//        binding.swipeRW.setOnRefreshListener {
//            viewModel.refreshPosts()
//        }
//        viewModel.data.observe(viewLifecycleOwner) { state ->
//            adapter.submitList(state.posts)
//            binding.progress.isVisible = state.loading
//            binding.errorGroup.isVisible = state.error
//            binding.emptyText.isVisible = state.empty
//        }

//        binding.retryButton.setOnClickListener {
//            viewModel.loadPosts()
//        }

        binding.fab.setOnClickListener {
            if(authViewModel.authenticated) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                mustSignIn()
            }

        }

//        binding.swipeRW.setOnRefreshListener {
//            viewModel.data.value?.refreshing = true
//            viewModel.loadPosts()
//            binding.swipeRW.isRefreshing = false
//        }

//        binding.list.adapter = adapter
//
//        viewModel.data.observe(viewLifecycleOwner) { posts ->
//            val newPost = posts.size > adapter.currentList.size
//            adapter.submitList(posts) {
//                if (newPost) {
//                    binding.list.smoothScrollToPosition(0)
//                }
//            }
//        }
        return binding.root
    }

    fun showNewPost() {
        binding.newPost.setIconTintResource(R.color.purple_700)
        binding.newPost.text = ""
        binding.buttonNewPosts.visibility = View.GONE
        binding.newPost.isEnabled = false
        viewModel.changeHidden()
    }

    fun mustSignIn() {
        val menuDialog = SignInOutDialogFragment("Нужна регистрация","Для этого действия необходимо войти в систему", R.drawable.info_24, "Sign In", "Позже")
        val manager = childFragmentManager
        menuDialog.show(manager, "Sign in")
    }
}

