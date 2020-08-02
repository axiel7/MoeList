package com.axiel7.moelist.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.AnimeListAdapter
import com.axiel7.moelist.model.AnimeList
import com.axiel7.moelist.rest.MalApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.RuntimeException

class HomeFragment : Fragment() {

    //private lateinit var testRecyclerView: RecyclerView
    //private lateinit var animes: List<AnimeList>
    //private lateinit var animeListAdapter: AnimeListAdapter
    //private var retrofit: Retrofit? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        //testRecyclerView = root.findViewById(R.id.test_recycler)

        return root
    }

    /*private fun connectAndGetApiData() {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl("https://api.myanimelist.net/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val malApiService = retrofit!!.create(MalApiService::class.java)
        val call = malApiService.getAnimeList("one", 100,0,"")
        call.enqueue(object : Callback<AnimeList> {
            override fun onResponse(call: Call<AnimeList>, response: Response<AnimeList>) {
                Log.d("MoeLog", call.request().toString())
            }

            override fun onFailure(call: Call<AnimeList>, t: Throwable) {
                Log.e("MoeLog", t.toString())
            }
        })
    }*/
}