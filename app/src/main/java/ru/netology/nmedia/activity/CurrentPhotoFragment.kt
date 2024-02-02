package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.filter
import androidx.paging.flatMap
import androidx.paging.map
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentCurrentPhotoBinding
import ru.netology.nmedia.dto.CounterView
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.view.load
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import java.util.Locale.filter
import javax.inject.Inject

@AndroidEntryPoint
class CurrentPhotoFragment : Fragment() {

    companion object {
        var Bundle.textArgument: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCurrentPhotoBinding.inflate(
            inflater,
            container,
            false
        )

        val currentId = arguments?.textArgument?.toLong()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.getPostById(currentId ?: 0L).collectLatest { post ->

                    if (post.id == currentId) {
                        binding.apply {
                            currentPhoto.load("http://192.168.1.10:9999/media/${post.attachment?.url}")

                            likePhoto.isChecked = post.likedByMe
                            likePhoto.text = CounterView.createCount(post.likes)

                            likePhoto.setOnClickListener {
                                if (authViewModel.authenticated) {
                                    viewModel.likeById(post)
                                } else {
                                    mustSignIn()
                                }
                                //   viewModel.likeById(currentPost)
                            }

                            buttonReturn.setOnClickListener {
                                findNavController().navigateUp()
                            }
                        }
                    }
                }

            }
        }


//        viewModel.data.observe(viewLifecycleOwner) { list ->
//            list.posts.find { it.id == currentId }?.let { currentPost ->
//
//                binding.apply {
//                    currentPhoto.load("http://192.168.1.10:9999/media/${currentPost.attachment?.url}")
//
//                    likePhoto.isChecked = currentPost.likedByMe
//                    likePhoto.text = CounterView.createCount(currentPost.likes)
//
//                    likePhoto.setOnClickListener {
//                        if(authViewModel.authenticated) {
//                            viewModel.likeById(currentPost)
//                        } else {
//                            mustSignIn()
//                        }
//                     //   viewModel.likeById(currentPost)
//                    }
//
//                    buttonReturn.setOnClickListener {
//                        findNavController().navigateUp()
//                    }
//                }
//            }
//        }

//        val currentPost = viewModel.data.value?.posts
//            ?.first { it.id == currentId }
//
//        if (currentPost != null) {
//            binding.apply {
//                currentPhoto.load("http://192.168.1.10:9999/media/${currentPost.attachment?.url}")
//
//                likePhoto.isChecked = currentPost.likedByMe
//                likePhoto.text = CounterView.createCount(currentPost.likes)
//
//                likePhoto.setOnClickListener {
//                    viewModel.likeById(currentPost)
//                }
//
//                buttonReturn.setOnClickListener {
//                    findNavController().navigateUp()
//                }
//            }
//        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

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