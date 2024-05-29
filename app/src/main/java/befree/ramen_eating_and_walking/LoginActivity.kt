package befree.ramen_eating_and_walking

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.preference.PreferenceManager
import befree.ramen_eating_and_walking.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var loginListener: OnCompleteListener<AuthResult>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // タイトルの設定
        title = getString (R.string.login_title)

        // データベースへのリファレンスを取得
        databaseReference = FirebaseDatabase.getInstance().reference

        // FirebaseAuthのオブジェクトを取得する
        auth = FirebaseAuth.getInstance()

        /**
         * ログイン処理のリスナー
         */
        loginListener = OnCompleteListener { task ->
            if (task.isSuccessful) { // 成功した場合
                // Preferenceにユーザー名を保存する
                val user = auth.currentUser
                val userRef = databaseReference.child(UsersPATH).child(user!!.uid)
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.value as Map<*, *>?
                        saveName(data!!["userName"] as String)
                    }

                    override fun onCancelled(firebaseError: DatabaseError) {}
                })
                // プログレスバーを非表示にする
                binding.progressBar.visibility = View.GONE
                // Activityを閉じる
                finish()
                // ダイアログを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, getString(R.string.success_login), Snackbar.LENGTH_LONG)
                    .show()
            } else { // 失敗した場合
                // エラーダイアログを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, getString(R.string.email_or_password_error_message), Snackbar.LENGTH_LONG)
                    .show()
                // プログレスバーを非表示にする
                binding.progressBar.visibility = View.GONE
            }
        }

        // アカウント作成ボタンを押した時の設定
        binding.createAccountButton.setOnClickListener {
            // アカウント作成ページへ移動
            val intent = Intent(applicationContext, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        // ログインボタンを押した時の設定
        binding.loginButton.setOnClickListener {
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()

            // ログイン情報の入力判定
            judgeLoginInput(email, password, it)
        }
    }

    /**
     * ログイン情報の入力判定
     */
    private fun judgeLoginInput(email: String, password: String, view: View) {
        // メールアドレスに入力あり、且つパスワードが6文字以上ならログイン処理を行う
        if (email.isNotEmpty() && password.length >= 6) {
            // ログイン処理
            login(email, password)
        }

        // 条件に当てはまらないならログイン処理を行う前にエラーを表示する
        else if(email.isEmpty() ) { // メールアドレスが入力されていない
            Snackbar.make(view, getString(R.string.email_error_message), Snackbar.LENGTH_LONG)
                .show()
        }
        else if(password.length < 6) { // パスワードが6文字未満
            Snackbar.make(view, getString(R.string.password_error_message), Snackbar.LENGTH_LONG)
                .show()
        }
        else { // その他例外
            Snackbar.make(view, getString(R.string.login_error_message), Snackbar.LENGTH_LONG)
                .show()
        }
    }

    /**
     * ログイン処理
     */
    private fun login (email: String, password: String) {
        // プログレスバーを表示する
        binding.progressBar.visibility = View.VISIBLE

        // ログインする
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(loginListener)
    }

    /**
     * Preferenceにユーザー名を保存する
     */
    private fun saveName(name: String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.apply()
    }
}