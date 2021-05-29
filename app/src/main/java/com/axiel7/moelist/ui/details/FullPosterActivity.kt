package com.axiel7.moelist.ui.details

import android.os.Bundle
import coil.Coil
import coil.request.ImageRequest
import coil.size.ViewSizeResolver
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ActivityFullPosterBinding
import com.axiel7.moelist.ui.BaseActivity
import com.igreenwood.loupe.extensions.createLoupe
import com.igreenwood.loupe.extensions.setOnViewTranslateListener

class FullPosterActivity : BaseActivity() {

    private lateinit var binding: ActivityFullPosterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullPosterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loadingPoster.show()
        val imageUrl = intent.extras?.getString("posterUrl", "")

        val imageLoader = Coil.imageLoader(this)

        val request = ImageRequest.Builder(this)
            .data(imageUrl)
            .crossfade(true)
            .crossfade(300)
            .error(R.drawable.ic_launcher_foreground)
            .size(ViewSizeResolver(binding.animePoster))
            .target { result ->
                binding.animePoster.setImageDrawable(result)
                binding.loadingPoster.hide()
                createLoupe(binding.animePoster, binding.posterContainer) {
                    setOnViewTranslateListener(
                        onDismiss = { finish() }
                    )
                }
            }
            .build()
        imageLoader.enqueue(request)
    }
}