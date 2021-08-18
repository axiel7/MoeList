package com.axiel7.moelist.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.transition.TransitionManager
import coil.load
import coil.transform.CircleCropTransformation
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ActivityMainBinding
import com.axiel7.moelist.ui.base.BaseActivity
import com.axiel7.moelist.ui.base.BaseFragment
import com.axiel7.moelist.ui.login.LoginActivity
import com.axiel7.moelist.ui.profile.ProfileViewModel
import com.axiel7.moelist.utils.InsetsHelper.addSystemWindowInsetToPadding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.collectLatest

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private val currentFragment
        get() = (navHostFragment.childFragmentManager.primaryNavigationFragment as? BaseFragment<*>)
    val root get() = binding.root
    val toolbarHeight get() = binding.appbarLayout.root.height
    val bottomNavHeight get() = binding.navView.height

    override fun preCreate() {
        super.preCreate()
        //setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        //setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
    }

    override fun setup() {
        //launch login
        if (!App.isUserLogged || App.accessToken == "null") {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val defaultSection = sharedPref.getString("default_section", "anime")

        when (sharedPref.getString("theme", "follow_system")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "follow_system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "amoled" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        binding.appbarLayout.mainToolbar.setNavigationOnClickListener { navigate(R.id.action_global_hostSearchFragment) }
        binding.appbarLayout.mainToolbar.setOnClickListener { navigate(R.id.action_global_hostSearchFragment) }

        binding.appbarLayout.profilePicture.setOnClickListener { navigate(R.id.action_global_fragmentProfile) }
        loadUser(sharedPref.getString("userPicture", null))

        val section = if (!intent.action.isNullOrEmpty() && intent.action != "android.intent.action.MAIN") {
            intent.action
        } else { // if the action is MAIN then use the saved pref
            defaultSection
        }

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val graph = navController.navInflater.inflate(R.navigation.main_graph)
        graph.setStartDestination(
            when(section) {
                "home" -> R.id.navigation_home
                "anime" -> R.id.navigation_anime_list
                "manga" -> R.id.navigation_manga_list
                else -> R.id.navigation_home
            }
        )
        navController.graph = graph
        navHostFragment.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        navHostFragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.animeDetailsFragment -> {
                    showBottomBar(false)
                    showToolbar(false)
                }
                R.id.mangaDetailsFragment -> {
                    showBottomBar(false)
                    showToolbar(false)
                }
                R.id.hostSearchFragment -> {
                    showBottomBar(false)
                    showToolbar(false)
                }
                R.id.fragmentProfile -> {
                    showBottomBar(false)
                    showToolbar(false)
                }
                R.id.animeRankingFragment -> {
                    showBottomBar(false)
                    showToolbar(false)
                }
                R.id.mangaRankingFragment -> {
                    showBottomBar(false)
                    showToolbar(false)
                }
                R.id.seasonalFragment -> {
                    showBottomBar(false)
                    showToolbar(false)
                }
                else -> {
                    showBottomBar()
                    showToolbar()
                }
            }
        }

        binding.navHostFragment.addSystemWindowInsetToPadding(bottom = true)
    }

    fun navigate(
        @IdRes idAction: Int,
        bundle: Bundle? = null,
        sharedView: View? = null
    ) {
        if (sharedView != null) {
            //val extras = FragmentNavigatorExtras(sharedView to sharedView.transitionName)
            navController.navigate(idAction, bundle, null, null)
        }
        else {
            navController.navigate(idAction, bundle)
        }
    }

    fun showBottomBar(show: Boolean = true) {
        binding.navView.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    fun showToolbar(show: Boolean = true) {
        binding.appbarLayout.root.setExpanded(show)
    }

    private fun loadUser(userPicture: String?) {
        if (userPicture == null) {
            val profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
            profileViewModel.getUser()

            launchLifecycleStarted {
                profileViewModel.user.collectLatest {
                    it?.let {
                        sharedPref.saveString("userPicture", it.picture)
                        binding.appbarLayout.profilePicture.load(it.picture) {
                            transformations(CircleCropTransformation())
                            error(R.drawable.ic_round_account_circle_24)
                        }
                    }
                }
            }
        }
        else {
            binding.appbarLayout.profilePicture.load(userPicture) {
                transformations(CircleCropTransformation())
                error(R.drawable.ic_round_account_circle_24)
            }
        }
    }

}