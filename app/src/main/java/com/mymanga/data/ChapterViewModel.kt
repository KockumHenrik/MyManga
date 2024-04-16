package com.mymanga.data

import android.app.Activity
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class ChapterViewModel : ViewModel() {
    private val _chapterList = MutableLiveData<Array<File>>()
    val chapterList: LiveData<Array<File>>
        get() = _chapterList

    init {
        _chapterList.value = arrayOf()
    }

    fun loadChapters(currentManga: String, activity: Activity) {
        _chapterList.value = getAvailableChapterList(currentManga, activity)
    }

    fun getAvailableChapterList(currentManga: String, activity: Activity): Array<File>? {
        val cw = ContextWrapper(activity)
        val directory = cw.getDir("imageDir", AppCompatActivity.MODE_PRIVATE)
        val mangaPath = File(directory.absolutePath + "/" + currentManga)
        return mangaPath.listFiles()
    }
}