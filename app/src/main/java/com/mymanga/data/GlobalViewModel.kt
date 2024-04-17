package com.mymanga.data

import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mymanga.controller.Controller
import com.mymanga.data.datatypes.Chapter
import com.mymanga.data.datatypes.Manga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GlobalViewModel : ViewModel() {
    private var loading = false
    private val _mangas = MutableLiveData<List<Manga>>()
    val mangas: LiveData<List<Manga>>
        get() = _mangas

    init {
        _mangas.value = mutableListOf()
    }

    fun add(manga: Manga) {
        val currentList = _mangas.value.orEmpty().toMutableList()
        currentList.add(manga)
        _mangas.value = currentList
    }

    fun deleteByName(mangaName: String) {
        val currentList = _mangas.value.orEmpty().toMutableList()
        val iterator = currentList.iterator()
        while (iterator.hasNext()) {
            val manga = iterator.next()
            if (manga.name == mangaName) {
                iterator.remove()
                break
            }
        }
        _mangas.value = currentList
    }

    fun getChaptersFromTargetManga(tManga: String): List<Chapter> {
        val mangaList = _mangas.value.orEmpty()
        val targetManga = mangaList.find { it.name == tManga }
        return targetManga?.chapters ?: emptyList()
    }

    fun loadManga(activity: Activity, application: Application) {
        if (!loading) {
            loading = true
            viewModelScope.launch {
                println("Loading Manga")
                withContext(Dispatchers.IO) {
                    val cw = ContextWrapper(activity)
                    // path to /data/data/yourapp/app_data/imageDir
                    val directory = cw.getDir("imageDir", AppCompatActivity.MODE_PRIVATE)

                    val tempMangaList: MutableList<Manga> = mutableListOf()

                    val mangaList = directory.list()
                    if (mangaList != null) {
                        for (i in mangaList.indices) {
                            val chapterList = loadChapterListForTargetManga(mangaList[i], cw, application)
                            val manga = Manga(mangaList[i], chapterList)
                            tempMangaList.add(manga)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        _mangas.value = tempMangaList
                    }
                }
                println("Done loading")
                loading = false
            }
        }
    }

    private fun loadChapterListForTargetManga(targetManga: String, cw: ContextWrapper, application: Application): List<Chapter> {
        val directory = cw.getDir("imageDir", AppCompatActivity.MODE_PRIVATE)
        val mangaPath = File(directory.absolutePath + "/" + targetManga)

        val chapterList: MutableList<Chapter> = mutableListOf()
        mangaPath.listFiles()?.forEach { chapterFile ->
            val chapterName = chapterFile.name
            val imageList = mutableListOf<Bitmap?>()
            chapterFile.listFiles()?.forEach { imageFile ->
                val image = Controller(application).loadImageFromStorage(imageFile.absolutePath, application)
                imageList.add(image)
            }
            chapterList.add(Chapter(chapterName, imageList))
        }
        return chapterList
    }
}