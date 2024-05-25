package befree.ramen_eating_and_walking

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.preference.PreferenceManager
import befree.ramen_eating_and_walking.databinding.ActivityCreateAccountBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding:ActivityCreateAccountBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var createAccountListener: OnCompleteListener<AuthResult>
    private lateinit var loginEventListener: OnCompleteListener<AuthResult>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // タイトルの設定
        title = getString(R.string.create_account_title)

        // データベースへのリファレンスを取得
        databaseReference = FirebaseDatabase.getInstance().reference

        // FirebaseAuthのオブジェクトを取得する
        auth = FirebaseAuth.getInstance()


        // アカウント作成処理のリスナー
        createAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) { // 成功した場合
                // 入力された情報を一時保存
                val email = binding.emailText.text.toString()
                val password = binding.passwordText.text.toString()
                // ログインを行う
                login(email, password)

            } else { // 失敗した場合
                // エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(
                    view,
                    getString(R.string.create_account_failure_message),
                    Snackbar.LENGTH_LONG
                ).show()

                // プログレスバーを非表示にする
                binding.progressBar.visibility = View.GONE
            }
        }

        // ログイン処理のリスナー
        loginEventListener = OnCompleteListener {task ->
            if(task.isSuccessful) {
                // 入力された情報を一時保存
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val userName = binding.userNameText.text.toString()
                val email = binding.emailText.text.toString()
                val origin = binding.origin.text.toString()
                val activityArea = binding.activityArea.text.toString()
                val likes = binding.likes.text.toString()
                val introduction = binding.introductionText.text.toString()

                // ユーザー名をPreferenceに保存する
                saveName(userName)
                // UserProfileのインスタンスを作成
                val userProfile = UserProfile()

                userProfile.uid = uid
                userProfile.userName = userName
                userProfile.email = email
                userProfile.origin = origin
                userProfile.activityArea = activityArea
                userProfile.likes = likes
                userProfile.introduction = introduction

                // アカウント情報をFirebaseに保存する
                FirebaseFirestore.getInstance()
                    .collection(UsersPATH)
                    .document(userProfile.id)
                    .set(userProfile)
                    .addOnSuccessListener {
                        binding.progressBar.visibility = View.GONE
                        finish()
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                        binding.progressBar.visibility = View.GONE
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            getString(R.string.failed_to_save_to_firebase),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                binding.progressBar.visibility = View.VISIBLE
            }
            else { // 失敗した場合
                // エラーダイアログを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, getString(R.string.login_failure_message), Snackbar.LENGTH_LONG)
                    .show()
                // プログレスバーを非表示にする
                binding.progressBar.visibility = View.GONE
            }
        }

        // アカウント作成ボタンの設定
        binding.createAccountButton.setOnClickListener {
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            // 入力された情報を一時保存
            val userName = binding.userNameText.text.toString()
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()
            val verification = binding.passwordVerificationText.text.toString()

            // アカウント情報の必須項目の入力判定
            judgeAccountInput(userName, email, password, verification, it)
        }
    }

    // アカウント情報の必須項目の入力判定
    private fun judgeAccountInput (
        userName: String, email: String,
        password: String, verification: String,
        view: View) {
        // 入力に問題がない場合、アカウント作成処理を行う
        if (userName.isNotEmpty() && email.isNotEmpty() &&
            password.length >= 6 && password == verification
        ) { // アカウント作成処理
            Log.d("success", "アカウント作成：入力問題なし")
            createAccount(email, password)
        }

        // 条件に当てはまらないならログイン処理を行う前にエラーを表示する
        else if(userName.isEmpty() ) { // ユーザー名が入力されていない
            Snackbar.make(view, getString(R.string.user_name_error_message), Snackbar.LENGTH_LONG)
                .show()
        }
        else if(email.isEmpty() ) { // メールアドレスが入力されていない
            Snackbar.make(view, getString(R.string.email_error_message), Snackbar.LENGTH_LONG)
                .show()
        }
        else if(password.length < 6) { // パスワードが6文字未満
            Snackbar.make(view, getString(R.string.password_error_message), Snackbar.LENGTH_LONG)
                .show()
        }
        else if(password != verification) { // パスワードと確認用パスワードが一致しない
            Snackbar.make(view, getString(R.string.password_no_match_error_message), Snackbar.LENGTH_LONG)
                .show()
        }
        else { // その他例外
            Snackbar.make(view, getString(R.string.login_error_message), Snackbar.LENGTH_LONG)
                .show()
        }
    }

    // アカウント作成処理
    private fun createAccount(email: String, password: String) {
        // プログレスバーを表示する
        binding.progressBar.visibility = View.VISIBLE

        // アカウントを作成する
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(createAccountListener)
    }

    // ログイン処理
    private fun login (email: String, password: String) {
        // プログレスバーを表示する
        binding.progressBar.visibility = View.VISIBLE

        // ログインする
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(loginEventListener)
    }

    // Preferenceにユーザー名を保存する
    private fun saveName(name: String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.apply()
    }

    //Spinnerに接続するアダプターの設定
    private fun setAdapter (spinnerItems: MutableList<String>) : ArrayAdapter<String> {

        // Adapterの生成
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)

        // 選択肢の各項目のレイアウト
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

}