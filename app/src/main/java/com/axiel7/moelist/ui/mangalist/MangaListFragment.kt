package com.axiel7.moelist.ui.mangalist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.axiel7.moelist.MyApplication
import com.axiel7.moelist.MyApplication.Companion.malApiService
import com.axiel7.moelist.R
import com.axiel7.moelist.UseCases
import com.axiel7.moelist.adapter.EndListReachedListener
import com.axiel7.moelist.adapter.MyMangaListAdapter
import com.axiel7.moelist.adapter.PlusButtonTouchedListener
import com.axiel7.moelist.model.MyMangaListStatus
import com.axiel7.moelist.model.UserMangaList
import com.axiel7.moelist.model.UserMangaListResponse
import com.axiel7.moelist.ui.details.EditMangaFragment
import com.axiel7.moelist.ui.details.MangaDetailsActivity
import com.axiel7.moelist.utils.ResponseConverter
import com.axiel7.moelist.utils.SharedPrefsHelpers
import com.axiel7.moelist.utils.StringFormat
import com.axiel7.moelist.utils.Urls
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_mangalist.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MangaListFragment : Fragment() {

    private lateinit var sharedPref: SharedPrefsHelpers
    private lateinit var mangaListAdapter: MyMangaListAdapter
    private var mangaListResponse: UserMangaListResponse? = null
    private lateinit var mangaList: MutableList<UserMangaList>
    private lateinit var listStatus: String
    private lateinit var sortMode: String
    private var defaultStatus: Int? = null
    private var defaultSort: Int = 0
    private var showNsfw = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = SharedPrefsHelpers.instance!!

        showNsfw = if (sharedPref.getBoolean("nsfw", false)) { 1 } else { 0 }

        defaultStatus = sharedPref.getInt("checkedStatusButtonManga", R.id.reading_button)
        if (defaultStatus==null || defaultStatus==0) {
            defaultStatus = R.id.reading_button
        }
        changeStatusFilter(defaultStatus!!)
        defaultSort = sharedPref.getInt("sortManga", 0)
        changeSortFilter(defaultSort)

        if (MyApplication.animeDb?.userMangaListDao()?.getUserMangaListByStatus(listStatus)!=null) {
            mangaList = MyApplication.animeDb?.userMangaListDao()?.getUserMangaListByStatus(listStatus)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_mangalist, container, false)
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            loading_mangalist.hide()
            if (mangaList.isEmpty()) {
                loading_mangalist.show()
            }
        }

        mangaListAdapter =
            MyMangaListAdapter(
                mangaList,
                R.layout.list_item_mangalist,
                requireContext(),
                onClickListener = { itemView, userMangaList, pos ->
                    openDetails(userMangaList.node.id, itemView, pos) },
                onLongClickListener = { _, userMangaList, pos ->
                    val editSheet =
                        EditMangaFragment(userMangaList.list_status,
                            userMangaList.node.id,
                            userMangaList.node.num_chapters ?: 0,
                            userMangaList.node.num_volumes ?: 0, pos)
                    editSheet.show(parentFragmentManager, "Edit")
                }
            )
        mangaListAdapter.setEndListReachedListener(object : EndListReachedListener {
            override fun onBottomReached(position: Int, lastPosition: Int) {
                if (mangaListResponse!=null) {
                    val nextPage: String? = mangaListResponse?.paging?.next
                    if (nextPage!=null) {
                        val getMoreCall = malApiService.getNextMangaListPage(nextPage)
                        initMangaListCall(getMoreCall, false, null, lastPosition)
                    }
                }
            }
        })
        mangaListAdapter.setPlusButtonTouchedListener(object : PlusButtonTouchedListener {
            override fun onButtonTouched(view: View, position: Int) {
                val animeId = mangaListAdapter.getAnimeId(position)
                val watchedEpisodes = mangaListAdapter.getWatchedEpisodes(position)
                val totalEpisodes = mangaListAdapter.getTotalEpisodes(position)
                if (totalEpisodes!=null) {
                    if (watchedEpisodes==totalEpisodes.minus(1)) {
                        addOneEpisode(animeId, watchedEpisodes.plus(1), "completed", position)
                    }
                    else {
                        addOneEpisode(animeId, watchedEpisodes?.plus(1), null, position)
                    }
                }
            }
        })

        mangalist_recycler.adapter = mangaListAdapter

        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filters_manga, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)
        filters_fab.setOnClickListener { bottomSheetDialog.show() }

        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.status_radio_group)
        val defaultCheck = defaultStatus!!
        radioGroup.check(defaultCheck)
        changeStatusFilter(defaultCheck)
        radioGroup.setOnCheckedChangeListener{ _, i ->
            changeStatusFilter(i)
            sharedPref.saveInt("checkedStatusButton", i)
            mangaList.clear()
            if (MyApplication.animeDb?.userMangaListDao()?.getUserMangaListByStatus(listStatus)!=null) {
                val mangaList2 = MyApplication.animeDb?.userMangaListDao()?.getUserMangaListByStatus(listStatus)!!
                mangaList.addAll(mangaList2)
                mangaListAdapter.notifyDataSetChanged()
            }
            bottomSheetDialog.dismiss()
            initCalls(true, null)
        }

        val sortView = dialogView.findViewById<LinearLayoutCompat>(R.id.sort)
        val sortSummary = sortView.findViewById<TextView>(R.id.sort_mode)
        sortSummary.text = StringFormat.formatSortOption(sortMode, requireContext())
        sortView?.setOnClickListener {
            val items = arrayOf(requireContext().getString(R.string.sort_title),
                requireContext().getString(R.string.sort_score),
                requireContext().getString(R.string.sort_last_updated))
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.sort))
                .setNeutralButton(resources.getString(R.string.cancel)) { _, _ ->
                    // Respond to neutral button press
                }
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                    // Respond to positive button press
                    sharedPref.saveInt("sortManga", defaultSort)
                    sortSummary.text = StringFormat.formatSortOption(sortMode, requireContext())
                    bottomSheetDialog.dismiss()
                    initCalls(true, null)
                }
                // Single-choice items (initialized with checked item)
                .setSingleChoiceItems(items, defaultSort) { _, which ->
                    // Respond to item chosen
                    defaultSort = which
                    changeSortFilter(which)
                }
                .show()
        }

        mangalist_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    //scroll down
                    filters_fab.hide()
                } else if (dy < 0) {
                    //scroll up
                    filters_fab.show()
                }
            }
        })

        initCalls(true, null)
    }
    private fun initCalls(shouldClear: Boolean, position: Int?) {
        val mangaListCall = if (listStatus == "all") {
            // To return all manga, don't specify status field.
            malApiService.getUserMangaList(null, "list_status,num_chapters,media_type,status", sortMode, showNsfw)
        } else {
            malApiService.getUserMangaList(listStatus, "list_status,num_chapters,media_type,status", sortMode, showNsfw)
        }
        if (isAdded) {
            loading_mangalist.isRefreshing = true
            initMangaListCall(mangaListCall, shouldClear, position, null)
        }
    }
    private fun initMangaListCall(call: Call<UserMangaListResponse>, shouldClear: Boolean, position: Int?, lastPosition: Int?) {
        call.enqueue(object: Callback<UserMangaListResponse> {
            override fun onResponse(call: Call<UserMangaListResponse>, response: Response<UserMangaListResponse>) {

                if (response.isSuccessful && isAdded) {
                    val responseOld = ResponseConverter
                        .stringToUserMangaListResponse(sharedPref.getString("mangaListResponse$listStatus", ""))
                    if (responseOld!=response.body() || mangaList.isEmpty()) {
                        mangaListResponse = response.body()

                        val mangaList2 = mangaListResponse!!.data
                        for (manga in mangaList2) {
                            manga.status = manga.list_status?.status
                        }
                        if (shouldClear) {
                            sharedPref.saveString("mangaListResponse$listStatus",
                                ResponseConverter.userMangaListResponseToString(mangaListResponse))
                            MyApplication.animeDb?.userMangaListDao()?.deleteUserMangaList(mangaList)
                            mangaList.clear()
                        }
                        when {
                            shouldClear -> {
                                mangaList.addAll(mangaList2)
                                mangaListAdapter.notifyDataSetChanged()
                            }
                            lastPosition != null -> {
                                mangaList.addAll(mangaList2)
                                mangaListAdapter.notifyItemRangeInserted(lastPosition, mangaList2.size)
                            }
                            position != null -> {
                                mangaList[position] = mangaList2[position]
                                mangaListAdapter.notifyItemChanged(position)
                            }
                        }
                        MyApplication.animeDb?.userMangaListDao()?.insertUserMangaList(mangaList)
                        mangaListAdapter.notifyDataSetChanged()
                    }
                    else {
                        mangaListResponse = responseOld
                    }
                }

                else if (response.code()==401) {
                    if (isAdded) {
                        UseCases.logOut(requireContext())
                    }
                }
            }

            override fun onFailure(call: Call<UserMangaListResponse>, t: Throwable) {
                Log.e("MoeLog", t.toString())
                if (isAdded) {
                    loading_mangalist.hide()
                    Snackbar.make(mangalist_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                }
            }

        })
    }
    private fun addOneEpisode(mangaId: Int, chaptersRead: Int?, status: String?, position: Int) {
        if (isAdded) {
            loading_mangalist.show()
        }
        val shouldNotUpdate = chaptersRead==null
        if (!shouldNotUpdate) {
            val updateListCall = malApiService
                .updateMangaList(Urls.apiBaseUrl + "manga/$mangaId/my_list_status", status, null, chaptersRead, null)
            updateListCall.enqueue(object : Callback<MyMangaListStatus> {
                override fun onResponse(call: Call<MyMangaListStatus>, response: Response<MyMangaListStatus>) {
                    if (response.isSuccessful && isAdded) {
                        initCalls(false, position)
                    }
                    else if (isAdded) {
                        Snackbar.make(mangalist_layout, getString(R.string.error_updating_list), Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MyMangaListStatus>, t: Throwable) {
                    Log.d("MoeLog", t.toString())
                    if (isAdded) {
                        Snackbar.make(mangalist_layout, getString(R.string.error_server), Snackbar.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            if (isAdded) {
                loading_mangalist.hide()
                Snackbar.make(mangalist_layout, getString(R.string.no_changes), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    private fun changeStatusFilter(radioButton: Int) {
        listStatus = when(radioButton) {
            R.id.all_button -> "all"
            R.id.reading_button -> "reading"
            R.id.completed_button -> "completed"
            R.id.onhold_button -> "on_hold"
            R.id.dropped_button -> "dropped"
            R.id.ptr_button -> "plan_to_read"
            else -> "reading"
        }
    }
    private fun changeSortFilter(radioButton: Int) {
        sortMode = when(radioButton) {
            0 -> "manga_title"
            1 -> "list_score"
            2 -> "list_updated_at"
            else -> "manga_title"
        }
    }
    private fun openDetails(mangaId: Int?, view: View?, position: Int) {
        if (view!=null) {
            val poster = view.findViewById<FrameLayout>(R.id.poster_container)
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), poster, poster.transitionName)
            val intent = Intent(context, MangaDetailsActivity::class.java)
            intent.putExtra("mangaId", mangaId)
            intent.putExtra("position", position)
            startActivityForResult(intent, 17, bundle.toBundle())
        }
        else {
            val intent = Intent(context, MangaDetailsActivity::class.java)
            intent.putExtra("mangaId", mangaId)
            intent.putExtra("position", position)
            startActivityForResult(intent, 17)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==17 && resultCode== Activity.RESULT_OK) {
            val shouldUpdate :Boolean = data?.extras?.getBoolean("entryUpdated", false) ?: false
            val position: Int? = data?.extras?.getInt("position")
            if (shouldUpdate) {
                initCalls(false, position)
            }
        }
    }
}