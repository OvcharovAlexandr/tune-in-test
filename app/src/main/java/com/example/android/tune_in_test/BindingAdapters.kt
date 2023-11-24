package com.example.android.tune_in_test

import android.view.View
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.tune_in_test.network.TuneInProperty
import com.example.android.tune_in_test.overview.ItemListAdapter
import com.example.android.tune_in_test.overview.TuneInStatus

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<TuneInProperty>?) {
    val adapter = recyclerView.adapter as ItemListAdapter
    adapter.submitList(data)
}

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let{
        val imgUri = it.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .apply(RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image))
            .into(imgView)
    }
}

@BindingAdapter("tuneInApiStatus")
fun bindStatus(statusImageView: ImageView, status: TuneInStatus?) {
    when (status) {
        TuneInStatus.LOADING -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.loading_animation)
        }
        TuneInStatus.ERROR -> {
            statusImageView.visibility = View.VISIBLE
            statusImageView.setImageResource(R.drawable.ic_connection_error)
        }
        TuneInStatus.DONE -> {
            statusImageView.visibility = View.GONE
        }
        else -> {}
    }
}