package ru.netology.nmedia.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.LoadStateBinding

interface OnInteractionListenerLoadState {
    fun onRetry() {}
}

class PagingLoadStateAdapter(

    private val onInteractionListenerLoadState: OnInteractionListenerLoadState,
) : LoadStateAdapter<PagingLoadStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LoadStateViewHolder(
            LoadStateBinding.inflate(layoutInflater, parent, false),
            onInteractionListenerLoadState,

        )
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadStateViewHolder(
        private val binding: LoadStateBinding,
        private val onInteractionListenerLoadState: OnInteractionListenerLoadState,

    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            Log.d("MyLog", "loadState=$loadState")
            binding.apply {
                progress.isVisible = loadState is LoadState.Loading

                retry.isVisible = loadState is LoadState.Error
             //   retry.isVisible = true
                retry.setOnClickListener {
                    onInteractionListenerLoadState.onRetry()
                }
            }
        }
    }
}