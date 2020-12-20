package com.axiel7.moelist.ui.details

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.widget.ContentLoadingProgressBar
import coil.Coil
import coil.request.ImageRequest
import coil.size.ViewSizeResolver
import com.axiel7.moelist.R
import com.axiel7.moelist.ui.BaseActivity
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.igreenwood.loupe.extensions.createLoupe
import com.igreenwood.loupe.extensions.setOnViewTranslateListener

class FullPosterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        findViewById<View>(android.R.id.content).transitionName = "shared_poster_container"
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 250L
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_poster)

        val loadingBar = findViewById<ContentLoadingProgressBar>(R.id.loading_poster)
        loadingBar.show()
        val imageUrl = intent.extras?.getString("posterUrl", "")
        val container = findViewById<RelativeLayout>(R.id.poster_container)
        val poster = findViewById<ImageView>(R.id.anime_poster)

        val imageLoader = Coil.imageLoader(this)

        val request = ImageRequest.Builder(this)
            .data(imageUrl)
            .crossfade(true)
            .crossfade(300)
            .error(R.drawable.ic_launcher_foreground)
            .size(ViewSizeResolver(poster))
            .target { result ->
                poster.setImageDrawable(result)
                loadingBar.hide()
                val loupe = createLoupe(poster, container) {
                    setOnViewTranslateListener(
                        onDismiss = { finish() }
                    )
                }
            }
            .build()
        imageLoader.enqueue(request)
    }
}