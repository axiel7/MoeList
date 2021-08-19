package com.axiel7.moelist.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.axiel7.moelist.App
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ActivityMainBinding
import com.axiel7.moelist.ui.base.BaseActivity
import com.axiel7.moelist.ui.login.LoginActivity
import com.axiel7.moelist.ui.profile.ProfileViewModel
import com.axiel7.moelist.utils.Constants.ERROR_SERVER
import com.axiel7.moelist.utils.Constants.RESPONSE_ERROR
import com.axiel7.moelist.utils.InsetsHelper.addSystemWindowInsetToPadding
import com.axiel7.moelist.utils.UseCases.logOut
import kotlinx.coroutines.flow.collectLatest

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    val root get() = binding.root
    val bottomNavHeight get() = binding.navView.height

    override fun setup() {
        //launch login
        if (!App.isUserLogged || App.accessToken == "null") {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding.appbarLayout.mainToolbar.setNavigationOnClickListener { navigate(R.id.action_global_hostSearchFragment) }
        binding.appbarLayout.mainToolbar.setOnClickListener { navigate(R.id.action_global_hostSearchFragment) }

        binding.appbarLayout.profilePicture.setOnClickListener { navigate(R.id.action_global_fragmentProfile) }
        loadUser(sharedPref.getInt("userId", -1))

        val defaultSection = sharedPref.getString("default_section", "anime")
        val section = if (!intent.action.isNullOrEmpty() && intent.action != "android.intent.action.MAIN") {
            intent.action
        } else { // if the action is MAIN then use the saved pref
            defaultSection
        }
        val destinationId = when(section) {
            "home" -> R.id.navigation_home
            "anime" -> R.id.navigation_anime_list
            "manga" -> R.id.navigation_manga_list
            else -> R.id.navigation_home
        }

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.navView.selectedItemId = destinationId
        binding.navView.menu.findItem(destinationId)?.isChecked = true
        val graph = navController.navInflater.inflate(R.navigation.main_graph)
        graph.setStartDestination(destinationId)
        navController.graph = graph
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
                R.id.fullPosterFragment -> {
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
            val extras = FragmentNavigatorExtras(sharedView to sharedView.transitionName)
            navController.navigate(idAction, bundle, null, extras)
        }
        else {
            navController.navigate(idAction, bundle, null, null)
        }
    }

    fun showBottomBar(show: Boolean = true) {
        binding.navView.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    fun showToolbar(show: Boolean = true) {
        binding.appbarLayout.root.setExpanded(show)
    }

    private fun loadUser(id: Int) {
        profileViewModel.getUser(id)
        launchLifecycleStarted {
            profileViewModel.user.collectLatest {
                it?.let {
                    binding.appbarLayout.profilePicture.load(it.picture) {
                        transformations(CircleCropTransformation())
                        error(R.drawable.ic_round_account_circle_24)
                    }
                }
            }
        }
        launchLifecycleStarted {
            profileViewModel.response.collectLatest {
                if (it.first == RESPONSE_ERROR && it.second != ERROR_SERVER) {
                    if (it.second.contains("token")) logOut()
                }
            }
        }
    }

}