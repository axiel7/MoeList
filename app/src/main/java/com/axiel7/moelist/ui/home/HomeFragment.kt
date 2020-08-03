package com.axiel7.moelist.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.R
import com.axiel7.moelist.adapter.AnimeListAdapter
import com.axiel7.moelist.model.AnimeListResponse
import com.axiel7.moelist.rest.MalApiService
import com.axiel7.moelist.rest.ServiceApiGenerator
import com.axiel7.moelist.utils.RefreshToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var testRecyclerView: RecyclerView
    private lateinit var animeListAdapter: AnimeListAdapter
    private lateinit var accessToken: String
    private lateinit var refreshToken: String

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        testRecyclerView = root.findViewById(R.id.test_recycler)
        sharedPref = context?.getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)!!
        accessToken = sharedPref.getString("accessToken", "").toString()
        refreshToken = sharedPref.getString("refreshToken", "").toString()

        connectAndGetApiData()

        return root
    }

    private fun connectAndGetApiData() {

        val malApiService = ServiceApiGenerator.createService(MalApiService::class.java, accessToken)
        val call = malApiService.getAnimeList("beastars", 10,0,"id,title,main_picture")
        enqueueCall(call)
    }
    private fun enqueueCall(call: Call<AnimeListResponse>) {
        call.enqueue(object : Callback<AnimeListResponse> {
            override fun onResponse(call: Call<AnimeListResponse>, response: Response<AnimeListResponse>) {
                Log.d("MoeLog", call.request().toString())

                if (response.isSuccessful) {
                    val animesResponse = response.body()!!
                    val animeList = animesResponse.data
                    Log.d("MoeLog", animesResponse.toString())

                    animeListAdapter =
                        context?.let {
                            AnimeListAdapter(
                                animeList,
                                R.layout.list_item_anime_grid,
                                it
                            )
                        }!!
                    testRecyclerView.adapter = animeListAdapter
                }
                //TODO else if error -> invalid_token 401 Unauthorized
                else {
                    val tokenResponse = RefreshToken.getNewToken(refreshToken)
                    accessToken = tokenResponse?.access_token.toString()
                    refreshToken = tokenResponse?.refresh_token.toString()
                    with (sharedPref.edit()) {
                        putString("accessToken", accessToken)
                        putString("refreshToken", refreshToken)
                        apply()
                    }
                    enqueueCall(call)
                }
            }

            override fun onFailure(call: Call<AnimeListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
            }
        })
    }
}