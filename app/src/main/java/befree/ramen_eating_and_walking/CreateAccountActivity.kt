package befree.ramen_eating_and_walking

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import befree.ramen_eating_and_walking.databinding.ActivityCreateAccountBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding:ActivityCreateAccountBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var createAccountListener: OnCompleteListener<AuthResult>
    private lateinit var loginEventListener: OnCompleteListener<AuthResult>
    private var pictureUri: Uri? = null

    // スピナー設定前の初期設定
    private var origin = ""
    private var activityArea = ""
    private var likes = ""

    // 許可状態の設定
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
    }
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

        /**
         * アカウント作成処理のリスナー
         */
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

        /**
         * ログイン処理のリスナー
         */
        loginEventListener = OnCompleteListener {task ->
            if(task.isSuccessful) {
                // 入力された情報を一時保存
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val userName = binding.userNameText.text.toString()
                val email = binding.emailText.text.toString()
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
                userProfile.image = getAttachedImage()

                // アカウント情報をFirebaseに保存する
                FirebaseFirestore.getInstance()
                    .collection(UsersPATH)
                    .document(uid)
                    .set(userProfile)
                    .addOnSuccessListener {
                        binding.progressBar.visibility = View.GONE
                        finish()
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        // ダイアログを表示する
                        val view = findViewById<View>(android.R.id.content)
                        Snackbar.make(view, getString(R.string.success_create_account), Snackbar.LENGTH_LONG)
                            .show()
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

        // 画像のクリックリスナーの設定
        binding.imageIcon.setOnClickListener(setImageClickListener)

        /**
         * スピナーの設定
         */
        // 出身の選択されたアイテムの変更を検知する
        binding.originSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                origin = binding.originSpinner.selectedItem.toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("onNothingSelected", "parent: $parent")
            }
        }
        // 活動地域の選択されたアイテムの変更を検知する
        binding.activityAreaSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                activityArea = binding.activityAreaSpinner.selectedItem.toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("onNothingSelected", "parent: $parent")
            }
        }
        // 好みの選択されたアイテムの変更を検知する
        binding.likesSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                likes = binding.likesSpinner.selectedItem.toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.e("onNothingSelected", "parent: $parent")
            }
        }
    }

    /**
     * アカウント情報の必須項目の入力判定
    */
    private fun judgeAccountInput (
        userName: String, email: String,
        password: String, verification: String,
        view: View) {
        // 入力に問題がない場合、アカウント作成処理を行う
        if (userName.isNotEmpty() && email.isNotEmpty() &&
            password.length >= 6 && password == verification
        ) { // アカウント作成処理
            Log.d("Success", "Create Account：No problems with input.")
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

    /**
     * アカウント作成処理
     */
    private fun createAccount(email: String, password: String) {
        // プログレスバーを表示する
        binding.progressBar.visibility = View.VISIBLE

        // アカウントを作成する
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(createAccountListener)
    }

    /**
     * ログイン処理
     */
    private fun login (email: String, password: String) {
        // プログレスバーを表示する
        binding.progressBar.visibility = View.VISIBLE

        // ログインする
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(loginEventListener)
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

    /**
     * imageIconが押された時の設定
     * パーミッションを確認して画像のセットに移行
     */
    private val setImageClickListener = View.OnClickListener {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 許可されている
            showChooser()
        } else {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                showChooser()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
                return@OnClickListener
            }
        }
    }

    /**
     * 画像を設定
     */
    private fun showChooser() {
        // ギャラリーから選択するIntent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // カメラで撮影するIntent
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        pictureUri = contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        val chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.get_image))

        // EXTRA_INITIAL_INTENTSにカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        launcher.launch(chooserIntent)
    }

    /**
     * このActivityに戻ってきた時の処理
     */
    private var launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultCode: Int = result.resultCode
        val data: Intent? = result.data

        if (resultCode != Activity.RESULT_OK) {
            if (pictureUri != null) {
                contentResolver.delete(pictureUri!!, null, null)
                pictureUri = null
            }
            return@registerForActivityResult
        }
        // 画像を取得
        val uri = if (data == null || data.data == null) pictureUri else data.data

        // URIからBitmapを取得する
        val image: Bitmap
        try {
            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(uri!!)
            image = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
        } catch (e: Exception) {
            return@registerForActivityResult
        }

        // Bitmapから正円のBitmapを作成
        val resizedImage = getCroppedBitmap(image)

        // BitmapをimageIconに設定する
        binding.imageIcon.setImageBitmap(resizedImage)

        pictureUri = null
    }

    /**
     * 添付画像を取得
     */
    private fun getAttachedImage (): String{
        // 添付画像を取得する
        val drawable = binding.imageIcon.drawable as? BitmapDrawable

        // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
        return if (drawable != null) {
            val bitmap = drawable.bitmap
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)

            Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
        } else {
            "" //データなしを示す
        }
    }

    /**
     * Bitmapから正円のBitmapを作成
     */
    fun getCroppedBitmap(bitmap: Bitmap): Bitmap {
        // 取得したBitmapの短辺の大きさにリサイズする
        val size = bitmap.width.coerceAtMost(bitmap.height)
        val resizedImage= Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        // 画像を円形に切り抜く
        val canvas = Canvas(resizedImage)
        val paint = Paint()

        // ジャギーが目立たなくなるようにアンチエイリアスを設定
        paint.isAntiAlias = true
        // 透明で塗りつぶす
        canvas.drawARGB(0, 0, 0, 0)
        // sizeの半分の大きさの半径で中央に円を描く DST
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val max = bitmap.width.coerceAtLeast(bitmap.height)
        val start = (size - max) / 2
        val left = if (bitmap.width < bitmap.height) 0 else start
        val top = if (bitmap.height < bitmap.width) 0 else start
        val rect = Rect(left, top, bitmap.width + left, bitmap.height + top)
        // 切り抜くrectを決めて描画処理 SRC
        canvas.drawBitmap(bitmap, Rect(0, 0, bitmap.width, bitmap.height), rect, paint)

        return resizedImage
    }

}