package com.axiel7.moelist.ui.details

import android.os.Bundle
import coil.Coil
import coil.request.ImageRequest
import coil.size.ViewSizeResolver
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.BaseActivity
import com.igreenwood.loupe.extensions.createLoupe
import com.igreenwood.loupe.extensions.setOnViewTranslateListener
import kotlinx.android.synthetic.main.activity_full_poster.*

class FullPosterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_poster)

        loading_poster.show()
        val imageUrl = intent.extras?.getString("posterUrl", "")

        val imageLoader = Coil.imageLoader(this)

        val request = ImageRequest.Builder(this)
            .data(imageUrl)
            .crossfade(true)
            .crossfade(300)
            .error(R.drawable.ic_launcher_foreground)
            .size(ViewSizeResolver(anime_poster))
            .target { result ->
                anime_poster.setImageDrawable(result)
                loading_poster.hide()
                val loupe = createLoupe(anime_poster, poster_container) {
                    setOnViewTranslateListener(
                        onDismiss = { finish() }
                    )
                }
            }
            .build()
        imageLoader.enqueue(request)
    }
}