package com.mymanga.controller

import android.app.Application
import android.content.Context
import com.mymanga.data.ChapterViewModel
import androidx.lifecycle.ViewModelProvider
import com.mymanga.data.MangaViewModel

class MangaApplication: Application() {

    private var ctx: Context? = null

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext
    }

    val viewModel: MangaViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this).create(MangaViewModel::class.java)
    }
}