package com.axiel7.moelist.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.SearchAdapter
import com.axiel7.moelist.model.AnimeList
import com.axiel7.moelist.model.AnimeListResponse
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.utils.CreateOkHttpClient
import com.axiel7.moelist.utils.RefreshToken
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.axiel7.moelist.utils.Urls
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    private lateinit var malApiService: MalApiService
    private lateinit var loadingBar: ContentLoadingProgressBar
    private lateinit var noResultsText: TextView
    private lateinit var searchItems: MutableList<AnimeList>
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var fields: String
    private var retrofit: Retrofit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_search)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            window.decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }

        SharedPrefsHelpers.init(this)
        sharedPref = SharedPrefsHelpers.instance!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()

        val toolbar = findViewById<Toolbar>(R.id.search_toolbar)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        loadingBar = findViewById(R.id.search_loading)
        loadingBar.hide()
        noResultsText = findViewById(R.id.no_result_text)

        val recyclerSearch = findViewById<RecyclerView>(R.id.recycler_search)
        searchItems = mutableListOf()
        searchAdapter = SearchAdapter(
            searchItems,
            R.layout.list_item_search_result,
            onClickListener = { _, animeList ->  openDetails(animeList.node.id)}
        )
        recyclerSearch.adapter = searchAdapter

        fields = "id,title,main_picture,mean,media_type,num_episodes,start_season"

        createRetrofitAndApiService()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)

        val searchMenu = menu?.findItem(R.id.app_bar_search)

        val searchView :SearchView = searchMenu?.actionView as SearchView
        searchView.queryHint = "Search"
        searchView.maxWidth = Int.MAX_VALUE
        searchView.isIconified = false
        searchView.requestFocus()

        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    initSearch(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }
    private fun createRetrofitAndApiService() {
        if (retrofit==null) {
            retrofit = if (MainActivity.httpClient!=null) {
                Retrofit.Builder()
                    .baseUrl(Urls.apiBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(MainActivity.httpClient!!)
                    .build()
            } else {
                Retrofit.Builder()
                    .baseUrl(Urls.apiBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(CreateOkHttpClient.createOkHttpClient(this, true))
                    .build()
            }
        }
        malApiService = retrofit?.create(MalApiService::class.java)!!
    }
    private fun initSearch(search: String) {
        loadingBar.show()
        val call = malApiService.getAnimeList(search,null,null, fields)
        call.enqueue(object :Callback<AnimeListResponse> {
            override fun onResponse(call: Call<AnimeListResponse>, response: Response<AnimeListResponse>) {
                if (response.isSuccessful) {
                    val animeResponse = response.body()
                    loadingBar.hide()
                    val results = animeResponse?.data!!
                    searchItems.clear()
                    searchItems.addAll(results)
                    searchAdapter.notifyDataSetChanged()
                }
                //TODO (not tested)
                else if (response.code()==401) {
                    val tokenResponse = RefreshToken.getNewToken(refreshToken)
                    accessToken = tokenResponse?.access_token.toString()
                    refreshToken = tokenResponse?.refresh_token.toString()
                    sharedPref.saveString("accessToken", accessToken)
                    sharedPref.saveString("refreshToken", refreshToken)

                    call.clone()
                }
            }

            override fun onFailure(call: Call<AnimeListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                loadingBar.hide()
                Toast.makeText(this@SearchActivity, "Error connecting to server", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openDetails(animeId: Int?) {
        val intent = Intent(this, AnimeDetailsActivity::class.java)
        intent.putExtra("animeId", animeId)
        startActivity(intent)
    }
}