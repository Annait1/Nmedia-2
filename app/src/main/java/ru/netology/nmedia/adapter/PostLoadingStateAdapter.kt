package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.ItemLoadingBinding

class PagingLoadStateAdapter(
    private val onRetry: () -> Unit,
) : LoadStateAdapter<PagingLoadStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LoadStateViewHolder(
            ItemLoadingBinding.inflate(layoutInflater, parent, false),
            onRetry
        )
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadStateViewHolder(
        private val itemLoadingBinding: ItemLoadingBinding,
        private val onRetry: () -> Unit,
    ) : RecyclerView.ViewHolder(itemLoadingBinding.root) {

        fun bind(loadState: LoadState) {
            itemLoadingBinding.apply {
                progress.isVisible = loadState is LoadState.Loading
                retry.isVisible = loadState is LoadState.Error

                retry.setOnClickListener {
                    onRetry()
                }
            }
        }
    }
}