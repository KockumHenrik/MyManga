package com.mymanga.data

import android.app.Activity
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MangaViewModel() : ViewModel() {
    private val _mangaList = MutableLiveData<List<String>>()
    val mangaList: LiveData<List<String>>
        get() = _mangaList

    init {
        _mangaList.value = mutableListOf()
    }

    fun addManga(mangaName: String) {
        val currentList = mangaList.value.orEmpty().toMutableList()
        currentList.add(mangaName)
        _mangaList.value = currentList
    }

    fun deleteManga(mangaName: String) {
        val currentList = mangaList.value.orEmpty().toMutableList()
        currentList.remove(mangaName)
        _mangaList.value = currentList
    }

    fun loadManga(activity: Activity) {
        val cw = ContextWrapper(activity)
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", AppCompatActivity.MODE_PRIVATE)

        val arr = directory.list()
        if (arr != null) {
            for (i in arr.indices) {
                addManga(arr[i])
            }
        }
    }
}