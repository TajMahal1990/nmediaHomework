package ru.netology.nmedia.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignUpWithPhotoBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.view.load
import ru.netology.nmedia.viewmodel.SignUpViewModel

@AndroidEntryPoint
class SignUpWithPhotoFragment : Fragment() {
    lateinit var binding: FragmentSignUpWithPhotoBinding

    private val viewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpWithPhotoBinding.inflate(inflater, container, false)

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Activity.RESULT_OK -> {
                        val uri: Uri? = it.data?.data
                        viewModel.changePhoto(uri, uri?.toFile())
                    }
                }
            }

        binding.photoForSignUp.load("http://192.168.1.10:9999/media/${viewModel.photoAvatarUrl}")

        binding.pickPhotoForSignUp.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhotoForSignUp.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.CAMERA)
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhotoForSignUp.setOnClickListener {
            viewModel.changePhoto(null, null)
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoForSignUp.setImageResource(R.drawable.empty_screen)
                return@observe
            }
            binding.photoForSignUp.setImageURI(it.uri)
        }

        binding.buttonSignUp.setOnClickListener {
            if (isFieldNotNull()) {

                viewModel.save(
                    binding.login.text.toString(),
                    binding.password.text.toString(),
                    binding.name.text.toString(),
                )

                findNavController().navigateUp()
            }
        }

        return binding.root
    }

    fun isFieldNotNull(): Boolean {
        var flag = true

        binding.apply {
            if (login.text.isNullOrEmpty()) {
                login.error = "Поле должно быть заполнено"
                flag = false
            }
            if (password.text.isNullOrEmpty()) {
                password.error = "Поле должно быть заполнено"
                flag = false
            }
            if (name.text.isNullOrEmpty()) {
                name.error = "Поле должно быть заполнено"
                flag = false
            }
            if (confirmPassword.text.isNullOrEmpty()) {
                confirmPassword.error = "Поле должно быть заполнено"
                flag = false
            }

            if (password.text.toString() != confirmPassword.text.toString()) {
                password.error = "Пароль не совпадает"
                confirmPassword.error = "Пароль не совпадает"
                flag = false
            }
        }
        return flag
    }
}