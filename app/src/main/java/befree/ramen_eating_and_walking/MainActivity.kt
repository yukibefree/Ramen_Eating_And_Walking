package befree.ramen_eating_and_walking

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import befree.ramen_eating_and_walking.databinding.ActivityMainBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import befree.ramen_eating_and_walking.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import befree.ramen_eating_and_walking.PermissionUtils.isPermissionGranted
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity (),
    ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityMainBinding

    // 位置情報を取得するためのプロパティ宣言
    private lateinit var mMap: GoogleMap
    private var permissionDenied = false

    // Bottom Navigationが選択された時の設定
    private val onItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                title = getString(R.string.menu_home)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, HomeFragment())
                    .commit()
                binding.fab.visibility = View.VISIBLE
                return@OnItemSelectedListener true
            }
            R.id.navigation_map -> {
                title = getString(R.string.menu_search)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, MapFragment())
                    .commit()
                binding.fab.visibility = View.INVISIBLE
                return@OnItemSelectedListener true
            }
            R.id.navigation_myPage -> {
                title = getString(R.string.menu_myPage)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, MyPageFragment())
                    .commit()
                binding.fab.visibility = View.VISIBLE
                return@OnItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // メニューバー(オプションバー)の設定
        setSupportActionBar(binding.toolbar)

        // Bottom NavigationとnavViewと紐付け
        binding.navView.setOnItemSelectedListener(onItemSelectedListener)

        // 初期表示
        val homeFragment = supportFragmentManager.beginTransaction()
        homeFragment.replace(R.id.frameLayout, HomeFragment())
        homeFragment.commit()
        title = getString (R.string.menu_home)

        // 位置情報を取得する許可をとる
        enableMyLocation()

    }

    override fun onResume() {
        super.onResume()

        // ログイン済みのユーザーを取得する
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) { // ログイン済みの場合の処理
            binding.toolbar.visibility = View.VISIBLE
        }
        else { // 未ログインの場合の処理
            binding.toolbar.visibility = View.INVISIBLE
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * ACCESS_FINE_LOCATIONが許可されている場合
     * より正確な位置情報を利用可能にする
     */
    private fun enableMyLocation() {

        // 権限が付与されているか確認し、付与されている場合はマイロケーションレイヤーを有効にする。
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // 許可根拠ダイアログを表示するかどうか
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            PermissionUtils.RationaleDialog.newInstance(
                LOCATION_PERMISSION_REQUEST_CODE, true
            ).show(supportFragmentManager, "dialog")
            return
        }

        // そうでない場合は、許可を求める。
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // 許可されている場合は、マイロケーションレイヤーを有効にする
            enableMyLocation()
        } else {
            // エラーメッセージを表示する
            // フラグメントの再開時にパーミッション不足エラーダイアログを表示する。
            permissionDenied = true
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }


    /**
     * 位置情報の取得権限がないことをダイアログで表示する。
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        const val LOCATION_PERMISSION_REQUEST_CODE = 300
    }

}