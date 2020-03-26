package hieusenpaj.com.callface.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import hieusenpaj.com.callface.R
import hieusenpaj.com.callface.fragment.HomeFragment
import hieusenpaj.com.callface.fragment.NotificationFragment
import hieusenpaj.com.callface.fragment.SettingFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val fragment1: Fragment = HomeFragment()
    val fragment2: Fragment = SettingFragment()
    val fragment3: Fragment = NotificationFragment()
    val fm: FragmentManager = supportFragmentManager
    var active: Fragment = fragment1
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handlePermission()
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        fm.beginTransaction().add(R.id.main_container, fragment3, "3").hide(fragment3).commit()
        fm.beginTransaction().add(R.id.main_container, fragment2, "2").hide(fragment2).commit()
        fm.beginTransaction().add(R.id.main_container, fragment1, "1").commit()

        iv_find_people.setOnClickListener {
            val intent = Intent(this,
                FindPeopleActivity::class.java)
            startActivity(intent)
        }
    }
    private val mOnNavigationItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener =
        object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(@NonNull item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.navigation_home -> {
                        fm.beginTransaction().hide(active).show(fragment1).commit()
                        active = fragment1
                        return true
                    }
                    R.id.navigation_setting -> {
                        fm.beginTransaction().hide(active).show(fragment2).commit()
                        active = fragment2
                        return true
                    }
                    R.id.navigation_notifications -> {
                        fm.beginTransaction().hide(active).show(fragment3).commit()
                        active = fragment3
                        return true
                    }
                    R.id.navigation_logout ->{
                        FirebaseAuth.getInstance().signOut()
                        onBackPressed()
                    }
                }
                return false
            }
        }
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun handlePermission() {
        val perms = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(perms, 3)
        } else {


        }

    }
}
