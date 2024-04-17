package com.mymanga.controller

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.mymanga.data.GlobalViewModel

class MangaApplication: Application() {

    private var ctx: Context? = null

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext
    }

    val viewModel: GlobalViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this).create(GlobalViewModel::class.java)
    }
}