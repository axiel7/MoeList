package com.axiel7.moelist.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.SearchAnimeAdapter
import com.axiel7.moelist.adapter.SearchMangaAdapter
import com.axiel7.moelist.databinding.ActivitySearchBinding
import com.axiel7.moelist.model.AnimeList
import com.axiel7.moelist.model.AnimeListResponse
import com.axiel7.moelist.model.MangaList
import com.axiel7.moelist.model.MangaListResponse
import com.axiel7.moelist.ui.details.AnimeDetailsActivity
import com.axiel7.moelist.ui.details.MangaDetailsActivity
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : BaseActivity() {

    private lateinit var searchItemsAnime: MutableList<AnimeList>
    private lateinit var searchItemsManga: MutableList<MangaList>
    private lateinit var searchAnimeAdapter: SearchAnimeAdapter
    private lateinit var searchMangaAdapter: SearchMangaAdapter
    private lateinit var searchType: String
    private var showNsfw = 0
    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        showNsfw = if (sharedPreferences.getBoolean("nsfw", false)) { 1 } else { 0 }

        window.statusBarColor = getColorFromAttr(R.attr.colorToolbar)

        setSupportActionBar(binding.searchToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.searchToolbar.setNavigationOnClickListener { onBackPressed() }

        binding.searchLoading.hide()

        val recyclerSearch = findViewById<RecyclerView>(R.id.recycler_search)
        searchItemsAnime = mutableListOf()
        searchItemsManga = mutableListOf()
        searchAnimeAdapter = SearchAnimeAdapter(
            searchItemsAnime,
            this,
            onClickListener = { itemView, animeList ->  openAnimeDetails(animeList.node.id, itemView)}
        )
        searchMangaAdapter = SearchMangaAdapter(
            searchItemsManga,
            this,
            onClickListener = { itemView, mangaList -> openMangaDetails(mangaList.node.id, itemView) }
        )
        recyclerSearch.adapter = searchAnimeAdapter

        val searchView: SearchView = binding.searchToolbar.findViewById(R.id.search_view)
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

        searchType = binding.searchTypeButton.text.toString()
        binding.searchTypeButton.setOnClickListener {
            searchView.setQuery("", false)
            searchView.requestFocus()
            if (searchType == "anime") {
                binding.searchTypeButton.text = getString(R.string.manga)
                searchType = "manga"
                recyclerSearch.adapter = searchMangaAdapter
            }
            else {
                binding.searchTypeButton.text = getString(R.string.anime)
                searchType = "anime"
                recyclerSearch.adapter = searchAnimeAdapter
            }
        }
    }
    private fun initAnimeSearch(search: String) {
        binding.searchLoading.show()
        val fields = "id,title,main_picture,mean,media_type,num_episodes,start_season"
        val call = malApiService.getAnimeList(search,null,null, showNsfw, fields)
        call.enqueue(object :Callback<AnimeListResponse> {
            override fun onResponse(call: Call<AnimeListResponse>, response: Response<AnimeListResponse>) {
                if (response.isSuccessful) {
                    val animeResponse = response.body()
                    binding.searchLoading.hide()
                    val results = animeResponse?.data!!
                    searchItemsAnime.clear()
                    searchItemsAnime.addAll(results)
                    searchAnimeAdapter.notifyDataSetChanged()
                }
                else if (response.code()==401) {
                    Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AnimeListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                binding.searchLoading.hide()
                Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
            }
        })
    }
    private fun initMangaSearch(search: String) {
        binding.searchLoading.show()
        val fields = "id,title,main_picture,mean,media_type,num_chapters,start_date"
        val call = malApiService.getMangaList(search,null,null, showNsfw, fields)
        call.enqueue(object :Callback<MangaListResponse> {
            override fun onResponse(call: Call<MangaListResponse>, response: Response<MangaListResponse>) {
                if (response.isSuccessful) {
                    val mangaResponse = response.body()
                    binding.searchLoading.hide()
                    val results = mangaResponse?.data!!
                    searchItemsManga.clear()
                    searchItemsManga.addAll(results)
                    searchMangaAdapter.notifyDataSetChanged()
                }
                else if (response.code()==401) {
                    Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MangaListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                binding.searchLoading.hide()
                Snackbar.make(binding.root, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
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