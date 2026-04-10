package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope

import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PagingLoadStateAdapter
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel


@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    /*private var errorShown = false*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id, post.likedByMe)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onImage(post: Post) {
                val fileName = post.attachment?.url ?: return
                val fullUrl = "http://10.0.2.2:9999/media/$fileName"

                findNavController().navigate(
                    R.id.action_feedFragment_to_imageFragment,
                    bundleOf("url" to fullUrl)
                )
            }
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter { adapter.retry() },
            footer = PagingLoadStateAdapter { adapter.retry() },
        )


        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }
        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.refresh.isRefreshing = it.refresh is LoadState.Loading

            }
        }

        binding.refresh.setOnRefreshListener {
            adapter.refresh()
        }


        binding.newPostsCard.setOnClickListener {
            binding.newPostsCard.isVisible = false
            adapter.refresh()
            binding.list.smoothScrollToPosition(0)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->


            if (state.error) {
                Snackbar.make(
                    binding.root,
                    state.errorMessage ?: R.string.error_loading,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.retry_loading) { adapter.refresh() }
                    .show()
            }

        }

        viewModel.newerCount.observe(viewLifecycleOwner) { count ->
            binding.newPostsCard.isVisible = count > 0
            if (count > 0) {
                binding.newPostsText.text =
                    resources.getQuantityString(
                        R.plurals.new_posts, count, count
                    )
            }
        }


        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }
}


