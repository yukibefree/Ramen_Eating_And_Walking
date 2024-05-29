package befree.ramen_eating_and_walking

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import befree.ramen_eating_and_walking.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MyPageFragment: Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private var snapshotListener: ListenerRegistration? = null
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

    override fun onResume() {
        super.onResume()

        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) { // ログインしていればログインボタン、アカウント作成ボタンを非表示
            binding.loginButton.visibility = View.INVISIBLE
            binding.createAccountButton.visibility = View.INVISIBLE
            // ログインしていればアカウント情報を反映
            val uid = user.uid
            onLoginAccount(uid)
        }
        else { // ログインしていなければログインボタン、アカウント作成ボタンを表示
            binding.loginButton.visibility = View.VISIBLE
            binding.createAccountButton.visibility = View.VISIBLE
            // ログインしていなければ初期状態で表示
            onLogout()
        }
    }

    /**
     * ログインした時の処理
     * アカウント情報を画面に反映
     */
    @SuppressLint("SetTextI18n")
    private fun onLoginAccount (uid: String) {
        // 一つ前のリスナーを消す
        snapshotListener?.remove()

        // ログイン情報に変更があった際にクエリを取得
        snapshotListener = FirebaseFirestore.getInstance()
            .collection(UsersPATH)
            .document(uid)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    // 取得エラー
                    Log.d("Firebase Error", "Firebaseからデータの取得に失敗しました")
                    return@addSnapshotListener
                }
                // データを変数に格納
                val userProfile: UserProfile = if (querySnapshot != null) {
                    querySnapshot.toObject(UserProfile::class.java)!!
                } else {
                    UserProfile()
                }
                // アイコン画像を取得
                val bytes = if (userProfile.image.isNotEmpty()) {
                    Base64.decode(userProfile.image, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }
                // アカウント情報を画面に反映
                binding.userName.text = userProfile.userName
                binding.email.text = userProfile.email
                binding.origin.text = if(userProfile.origin.isNotEmpty()) {
                    "${getString(R.string.myPage_origin)}${userProfile.origin}"
                } else {
                    getString(R.string.account_origin_no_data)
                }
                binding.activityArea.text = if(userProfile.activityArea.isNotEmpty()) {
                    "${getString(R.string.myPage_activity_area)}${ userProfile.activityArea }"
                } else {
                    getString(R.string.account_activity_area_no_data)
                }
                binding.likes.text = if(userProfile.likes.isNotEmpty()) {
                    "${getString(R.string.myPage_likes)}${ userProfile.likes }"
                } else {
                    getString(R.string.account_likes_no_data)
                }
                binding.introduction.text = if(userProfile.introduction.isNotEmpty()) {
                    "${getString(R.string.myPage_introduction)}${ userProfile.introduction }"
                } else {
                    getString(R.string.account_introduction_no_data)
                }
                // アイコン画像を画面に反映
                if (bytes.isNotEmpty()) {
                    val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        .copy(Bitmap.Config.ARGB_8888, true)
                    val resizedImage = CreateAccountActivity().getCroppedBitmap(image)
                    binding.imageIcon.setImageBitmap(resizedImage)
                }
            }
    }

    private fun onLogout () {
        // アカウント情報(未登録)を画面に反映
        binding.userName.text = getString(R.string.account_userName_no_data)
        binding.email.text = getString(R.string.account_email_no_data)
        binding.origin.text = getString(R.string.account_origin_no_data)
        binding.activityArea.text = getString(R.string.account_activity_area_no_data)
        binding.likes.text = getString(R.string.account_likes_no_data)
        binding.introduction.text = getString(R.string.account_introduction_no_data)
    }
}