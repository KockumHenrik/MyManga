package com.mymanga.data.datatypes

import android.graphics.Bitmap

data class Chapter (
    val name: String? = null,
    val images: List<Bitmap?>? = null
)