package ru.netology.nmedia.activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.viewmodel.SignInViewModel

@AndroidEntryPoint
class FragmentSignIn : Fragment(R.layout.fragment_sign_in) {

    private val viewModel: SignInViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val login = view.findViewById<EditText>(R.id.login)
        val pass = view.findViewById<EditText>(R.id.pass)
        val button = view.findViewById<Button>(R.id.signIn)

        button.setOnClickListener {
            viewModel.signIn(
                login.text.toString().trim(),
                pass.text.toString()
            )
        }

        viewModel.success.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        viewModel.state.observe(viewLifecycleOwner) { st ->
            button.isEnabled = !st.loading
            if (st.error) {
                Toast.makeText(requireContext(), R.string.auth_error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}