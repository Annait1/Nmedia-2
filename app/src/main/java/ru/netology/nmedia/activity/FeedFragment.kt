package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

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
        })


        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)

            binding.emptyText.isVisible = state.empty
            binding.resresh.isRefreshing = false


        }

        viewModel.state.observe(viewLifecycleOwner) {state ->
            binding.progress.isVisible = state.loading
          /*  binding.errorGroup.isVisible = state.error уже не надо*/
            binding.resresh.isRefreshing = state.refreshing

            if (state.error) {
                Snackbar.make(
                    binding.root,
                    state.errorMessage ?: R.string.error_loading,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }

        }

       /* binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }уже не надо. в layout удалили**/

        binding.resresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }
}

/*    private fun showErrorSnackbar(root: View) {
        Snackbar.make(root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) { viewModel.loadPosts() }
            .show()
    }поменяла как в лекции*/

