package com.axiel7.moelist.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import coil.load
import coil.transform.CircleCropTransformation
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.animelist.AnimeListFragment
import com.axiel7.moelist.ui.details.EditAnimeFragment
import com.axiel7.moelist.ui.details.EditMangaFragment
import com.axiel7.moelist.ui.home.HomeFragment
import com.axiel7.moelist.ui.mangalist.MangaListFragment
import com.axiel7.moelist.ui.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.platform.MaterialFadeThrough
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), EditAnimeFragment.OnDataPass, EditMangaFragment.OnDataPass {

    private val fragmentManager = supportFragmentManager
    private val homeFragment = HomeFragment()
    private val animeListFragment = AnimeListFragment()
    private val mangaListFragment = MangaListFragment()
    private val profileFragment = ProfileFragment()
    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //shared preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val isTokenNull = MyApplication.accessToken == "null"
        val defaultSection = sharedPreferences.getString("default_section", "home")!!

        when(sharedPreferences.getString("theme", "follow_system")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "follow_system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "amoled" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        //toolbar
        setSupportActionBar(main_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        main_toolbar.setNavigationOnClickListener { openSearch(main_toolbar) }
        //main_toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_round_menu_24)

        //window.statusBarColor = ContextCompat.getColor(this, R.color.colorBackground)

        // bottom sheet
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_main)
        profile_picture.setOnClickListener { bottomSheetDialog.show() }

        //launch login
        if (!MyApplication.isUserLogged || isTokenNull) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            //bottom nav and fragments
            setupTransitions()
            setupBottomBar(nav_view, defaultSection)
            loadUser(sharedPreferences.getString("userPicture", null))
        }
    }

    private fun setupBottomBar(navigationView: BottomNavigationView, defaultSection: String) {
        // check if the app is being opened by a home screen shortcut
        val intentAction = intent.action
        val section = if (!intentAction.isNullOrEmpty() && intentAction != "android.intent.action.MAIN") {
            intentAction
        } else { // if the action is MAIN then use the saved pref
            defaultSection
        }
        val fragment = when(section) {
            "home" -> homeFragment
            "anime" -> animeListFragment
            "manga" -> mangaListFragment
            "profile" -> profileFragment
            else -> homeFragment
        }
        fragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
        navigationView.selectedItemId = when(section) {
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

    private fun loadUser(userPicture: String?) {
        if (userPicture == null) {
            profileFragment.getUser()
        }
        else {
            profile_picture.load(userPicture) {
                crossfade(true)
                crossfade(500)
                transformations(CircleCropTransformation())
                error(R.drawable.ic_round_account_circle_24)
            }
        }
    }

    fun openSearch(view: View) {
        val intent = Intent(this, SearchActivity::class.java)
        val bundle = ActivityOptionsCompat
            .makeSceneTransitionAnimation(this, main_toolbar, main_toolbar.transitionName).toBundle()
        startActivity(intent, bundle)
    }
    fun openSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivityForResult(intent, 77)
        bottomSheetDialog.dismiss()
    }
    fun openDonations(view: View) {
        val intent = Intent(this, DonationActivity::class.java)
        startActivity(intent)
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
            this@MainActivity.recreate()
        }
        else if (requestCode==2 && resultCode==Activity.RESULT_OK) {
            MyApplication.isUserLogged = true
        }
    }

    override fun onAnimeEntryUpdated(updated: Boolean) {
        val intent = Intent()
        intent.putExtra("entryUpdated", updated)
        animeListFragment.onActivityResult(17, RESULT_OK, intent)
    }

    override fun onMangaEntryUpdated(updated: Boolean) {
        val intent = Intent()
        intent.putExtra("entryUpdated", updated)
        mangaListFragment.onActivityResult(17, RESULT_OK, intent)
    }
}