package befree.ramen_eating_and_walking

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import befree.ramen_eating_and_walking.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity () {

    private lateinit var binding: ActivityMainBinding

    // Bottom Navigationが選択された時の設定
    private val onItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                title = getString(R.string.menu_home)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, HomeFragment())
                    .commit()
                return@OnItemSelectedListener true
            }
            R.id.navigation_map -> {
                title = getString(R.string.menu_search)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, MapFragment())
                    .commit()
                return@OnItemSelectedListener true
            }
            R.id.navigation_myPage -> {
                title = getString(R.string.menu_myPage)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, MyPageFragment())
                    .commit()
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
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, HomeFragment())
            .commit()

        // タイトルの設定
        title = getString (R.string.menu_home)

    }

    override fun onResume() {
        super.onResume()

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
}