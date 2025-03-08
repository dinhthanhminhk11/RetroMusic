package code.name.monkey.retromusic.extensions

import android.content.Context
import android.widget.ImageView
import code.name.monkey.retromusic.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

fun loadImage(context: Context, imageUrl: String?, imageView: ImageView) {
    val options = RequestOptions()
        .centerCrop()
        .placeholder(R.drawable.imageloading)
        .error(R.drawable.imageerror)

    Glide.with(context)
        .load(imageUrl)
        .apply(options)
        .into(imageView)
}

fun loadImageAvatar(context: Context, imageUrl: String?, imageView: ImageView) {
    val options = RequestOptions()
        .centerCrop()
        .placeholder(R.drawable.imageloading)
        .error(R.drawable.avd_artist)

    Glide.with(context)
        .load(imageUrl)
        .apply(options)
        .into(imageView)
}