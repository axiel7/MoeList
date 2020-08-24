package com.axiel7.moelist.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.SearchAnimeAdapter
import com.axiel7.moelist.adapter.SearchMangaAdapter
import com.axiel7.moelist.model.AnimeList
import com.axiel7.moelist.model.AnimeListResponse
import com.axiel7.moelist.model.MangaList
import com.axiel7.moelist.model.MangaListResponse
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.ui.details.MangaDetailsActivity
import com.axiel7.moelist.utils.CreateOkHttpClient
import com.axiel7.moelist.utils.RefreshToken
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.axiel7.moelist.utils.Urls
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var searchItemsAnime: MutableList<AnimeList>
    private lateinit var searchItemsManga: MutableList<MangaList>
    private lateinit var searchAnimeAdapter: SearchAnimeAdapter
    private lateinit var searchMangaAdapter: SearchMangaAdapter
    private lateinit var buttonType: Button
    private lateinit var searchType: String
    private lateinit var snackBarView: View
    private var showNsfw = false
    private var retrofit: Retrofit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
             window.setDecorFitsSystemWindows(false)
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }

        SharedPrefsHelpers.init(this)
        sharedPref = SharedPrefsHelpers.instance!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        showNsfw = sharedPreferences.getBoolean("nsfw", false)

        val toolbar = findViewById<Toolbar>(R.id.search_toolbar)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        snackBarView = findViewById(R.id.search_layout)
        loadingBar = findViewById(R.id.search_loading)
        loadingBar.hide()
        noResultsText = findViewById(R.id.no_result_text)

        val recyclerSearch = findViewById<RecyclerView>(R.id.recycler_search)
        searchItemsAnime = mutableListOf()
        searchItemsManga = mutableListOf()
        searchAnimeAdapter = SearchAnimeAdapter(
            searchItemsAnime,
            R.layout.list_item_search_result,
            this,
            onClickListener = { itemView, animeList ->  openAnimeDetails(animeList.node.id, itemView)}
        )
        searchMangaAdapter = SearchMangaAdapter(
            searchItemsManga,
            R.layout.list_item_search_result,
            this,
            onClickListener = { itemView, mangaList -> openMangaDetails(mangaList.node.id, itemView) }
        )
        recyclerSearch.adapter = searchAnimeAdapter

        val searchView: SearchView = toolbar.findViewById(R.id.search_view)
        val searchViewIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchViewIcon.visibility = View.GONE
        searchViewIcon.setImageDrawable(null)
        searchView.queryHint = getString(R.string.search)
        searchView.setIconifiedByDefault(false)
        searchView.requestFocus()
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    if (searchType == "manga") {
                        initMangaSearch(query)
                    }
                    else {
                        initAnimeSearch(query)
                    }
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        buttonType = findViewById(R.id.search_type_button)
        searchType = buttonType.text.toString()
        buttonType.setOnClickListener {
            searchView.setQuery("", false)
            searchView.requestFocus()
            if (searchType == "anime") {
                buttonType.text = getString(R.string.manga)
                searchType = "manga"
                recyclerSearch.adapter = searchMangaAdapter
            }
            else {
                buttonType.text = getString(R.string.anime)
                searchType = "anime"
                recyclerSearch.adapter = searchAnimeAdapter
            }
        }

        createRetrofitAndApiService()
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
    private fun initAnimeSearch(search: String) {
        loadingBar.show()
        val fields = "id,title,main_picture,mean,media_type,num_episodes,start_season"
        val call = malApiService.getAnimeList(search,null,null, showNsfw, fields)
        call.enqueue(object :Callback<AnimeListResponse> {
            override fun onResponse(call: Call<AnimeListResponse>, response: Response<AnimeListResponse>) {
                if (response.isSuccessful) {
                    val animeResponse = response.body()
                    loadingBar.hide()
                    val results = animeResponse?.data!!
                    searchItemsAnime.clear()
                    searchItemsAnime.addAll(results)
                    searchAnimeAdapter.notifyDataSetChanged()
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
                Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }
    private fun initMangaSearch(search: String) {
        loadingBar.show()
        val fields = "id,title,main_picture,mean,media_type,num_chapters,start_date"
        val call = malApiService.getMangaList(search,null,null, showNsfw, fields)
        call.enqueue(object :Callback<MangaListResponse> {
            override fun onResponse(call: Call<MangaListResponse>, response: Response<MangaListResponse>) {
                if (response.isSuccessful) {
                    val mangaResponse = response.body()
                    loadingBar.hide()
                    val results = mangaResponse?.data!!
                    searchItemsManga.clear()
                    searchItemsManga.addAll(results)
                    searchMangaAdapter.notifyDataSetChanged()
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

            override fun onFailure(call: Call<MangaListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                loadingBar.hide()
                Snackbar.make(snackBarView, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun openAnimeDetails(animeId: Int?, view: View) {
        val poster = view.findViewById<ImageView>(R.id.anime_poster)
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, poster, poster.transitionName)
        val intent = Intent(this, AnimeDetailsActivity::class.java)
        intent.putExtra("animeId", animeId)
        startActivity(intent, bundle.toBundle())
    }
    private fun openMangaDetails(mangaId: Int?, view: View) {
        val poster = view.findViewById<ImageView>(R.id.anime_poster)
        val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, poster, poster.transitionName)
        val intent = Intent(this, MangaDetailsActivity::class.java)
        intent.putExtra("mangaId", mangaId)
        startActivity(intent, bundle.toBundle())
    }
}