package com.axiel7.moelist.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.animelist.AnimeListFragment
import com.axiel7.moelist.ui.home.HomeFragment
import com.axiel7.moelist.ui.mangalist.MangaListFragment
import com.axiel7.moelist.ui.profile.ProfileFragment
import com.axiel7.moelist.utils.CreateOkHttpClient
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialFadeThrough
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {

    private val fragmentManager = supportFragmentManager
    private val homeFragment = HomeFragment()
    private val animeListFragment = AnimeListFragment()
    private val mangaListFragment = MangaListFragment()
    private val profileFragment = ProfileFragment()
    private lateinit var bottomSheetDialog: BottomSheetDialog

    companion object {
        var httpClient: OkHttpClient? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //shared preferences
        SharedPrefsHelpers.init(this)
        val sharedPref = SharedPrefsHelpers.instance
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val isUserLogged = sharedPref?.getBoolean("isUserLogged", false)
        val accessToken = sharedPref?.getString("accessToken", "null")
        val isTokenNull = accessToken.equals("null")
        val defaultSection = sharedPreferences.getString("default_section", "home")!!

        when(sharedPreferences.getString("theme", "follow_system")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "follow_system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        //toolbar
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_round_menu_24)

        //edge to edge
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }

        // bottom sheet
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_main, null)
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(dialogView)
        toolbar.setNavigationOnClickListener { bottomSheetDialog.show() }

        //launch login
        if (!isUserLogged!! || isTokenNull) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            httpClient = CreateOkHttpClient.createOkHttpClient(applicationContext, true)
            //bottom nav and fragments
            val navView: BottomNavigationView = findViewById(R.id.nav_view)
            setupTransitions()
            setupBottomBar(navView, defaultSection)
        }
        /*FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FireLog", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = "token: $token"
                Log.d("FireLog", msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })*/
    }

    private fun setupBottomBar(navigationView: BottomNavigationView, defaultSection: String) {
        val fragment = when(defaultSection) {
            "home" -> homeFragment
            "anime" -> animeListFragment
            "manga" -> mangaListFragment
            "profile" -> profileFragment
            else -> homeFragment
        }
        fragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
        navigationView.selectedItemId = when(defaultSection) {
            "home" -> R.id.navigation_home
            "anime" -> R.id.navigation_anime_list
            "manga" -> R.id.navigation_manga_list
            "profile" -> R.id.navigation_profile
            else -> R.id.navigation_home
        }
        navigationView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment = homeFragment
            when (item.itemId) {
                R.id.navigation_home -> selectedFragment = homeFragment
                R.id.navigation_anime_list -> selectedFragment = animeListFragment
                R.id.navigation_manga_list -> selectedFragment = mangaListFragment
                R.id.navigation_profile -> selectedFragment = profileFragment
            }
            val fade = MaterialFadeThrough()
            selectedFragment.enterTransition = fade
            selectedFragment.exitTransition = fade
            fragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, selectedFragment)
                .commit()

            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun setupTransitions() {
        val fade = MaterialFadeThrough()

        homeFragment.enterTransition = fade
        homeFragment.exitTransition = fade

        animeListFragment.enterTransition = fade
        animeListFragment.exitTransition = fade

        mangaListFragment.enterTransition = fade
        mangaListFragment.exitTransition = fade

        profileFragment.enterTransition = fade
        profileFragment.exitTransition = fade
    }

    fun openSearch(view: View) {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }
    fun openSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivityForResult(intent, 77)
        bottomSheetDialog.dismiss()
    }
    fun openDonations(view: View) {
        Toast.makeText(this, "( /^ω^)/♪♪", Toast.LENGTH_SHORT).show()
        bottomSheetDialog.dismiss()
    }
    fun openShare(view: View) {
        ShareCompat.IntentBuilder.from(this@MainActivity)
            .setType("text/plain")
            .setChooserTitle("")
            .setText("https://play.google.com/store/apps/details?id=com.axiel7.moelist")
            .startChooser()
        bottomSheetDialog.dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==77 && resultCode== Activity.RESULT_OK) {
            val themeChanged :Boolean = data?.extras?.get("themeChanged") as Boolean
            if (themeChanged) {
                this@MainActivity.recreate()
            }
        }
    }
}