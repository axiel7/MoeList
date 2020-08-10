package com.axiel7.moelist.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import coil.Coil
import coil.api.load
import coil.request.LoadRequest
import coil.size.ViewSizeResolver
import com.axiel7.moelist.R
import com.igreenwood.loupe.Loupe

class FullPosterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_full_poster)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            window.decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
        val imageUrl = intent.extras?.getString("posterUrl", "")
        val container = findViewById<RelativeLayout>(R.id.poster_container)
        val poster = findViewById<ImageView>(R.id.anime_poster)

        val imageLoader = Coil.imageLoader(this)
        val request = LoadRequest.Builder(this)
            .data(imageUrl)
            .crossfade(true)
            .crossfade(300)
            .error(R.drawable.ic_launcher_foreground)
            .size(ViewSizeResolver(poster))
            .target(
                onStart = {
                    // Handle the placeholder drawable.
                },
                onSuccess = { result ->
                    // Handle the successful result.
                    poster.load(result)

                },
                onError = { error ->
                    // Handle the error drawable.
                    poster.load(error)
                }
            )
            .build()
        imageLoader.execute(request)

        val loupe = Loupe.create(poster, container) {
            onViewTranslateListener = object : Loupe.OnViewTranslateListener {

                override fun onStart(view: ImageView) {
                    // called when the view starts moving
                }

                override fun onViewTranslate(view: ImageView, amount: Float) {
                    // called whenever the view position changed
                }

                override fun onRestore(view: ImageView) {
                    // called when the view drag gesture ended
                }

                override fun onDismiss(view: ImageView) {
                    // called when the view drag gesture ended
                    finish()
                }
            }
        }
    }
}