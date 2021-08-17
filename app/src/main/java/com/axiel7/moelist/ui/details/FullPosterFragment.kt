package com.axiel7.moelist.ui.details

import android.view.LayoutInflater
import android.view.ViewGroup
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.FragmentFullPosterBinding
import com.axiel7.moelist.ui.base.BaseFragment

class FullPosterFragment : BaseFragment<FragmentFullPosterBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFullPosterBinding
        get() = FragmentFullPosterBinding::inflate

    override fun setup() {
        binding.loadingPoster.show()
        val imageUrl = arguments?.getString("poster_url")

        binding.animePoster.load(imageUrl) {
            crossfade(true)
            crossfade(300)
            error(R.drawable.ic_launcher_foreground)
            //size(ViewSizeResolver(binding.animePoster))
            listener(onSuccess = {_, _ -> binding.loadingPoster.hide() })
        }
    }
}