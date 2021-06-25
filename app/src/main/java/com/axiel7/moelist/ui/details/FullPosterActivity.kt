package com.axiel7.moelist.ui.details

import android.view.LayoutInflater
import coil.load
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ActivityFullPosterBinding
import com.axiel7.moelist.ui.base.BaseActivity

class FullPosterActivity : BaseActivity<ActivityFullPosterBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityFullPosterBinding
        get() = ActivityFullPosterBinding::inflate

    override fun setup() {
        binding.loadingPoster.show()
        val imageUrl = intent.extras?.getString("posterUrl", "")

        binding.animePoster.load(imageUrl) {
            crossfade(true)
            crossfade(300)
            error(R.drawable.ic_launcher_foreground)
            //size(ViewSizeResolver(binding.animePoster))
            listener(onSuccess = {_, _ -> binding.loadingPoster.hide() })
        }
    }
}