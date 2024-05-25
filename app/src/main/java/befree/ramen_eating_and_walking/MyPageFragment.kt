package befree.ramen_eating_and_walking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import befree.ramen_eating_and_walking.databinding.FragmentMypageBinding

class MyPageFragment: Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.apply {
            setOnClickListener {
                Log.d("Click", "Click Login Button")
                val intent = Intent(context, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        binding.createAccountButton.apply {
            setOnClickListener {
                Log.d("Click", "Click Create Button")
                val intent = Intent(context, CreateAccountActivity::class.java)
                startActivity(intent)
            }
        }
    }
}