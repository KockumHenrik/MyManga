package com.mymanga.controller

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mymanga.dao.Kissmanga
import com.mymanga.dao.ReadManga
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

class Controller (private val application: Application) {

    private val kissmanga = Kissmanga()
    private val readmanga = ReadManga()

    private val KISSMANGA = "kissmanga"
    private val READMANGA = "readmanga"

    fun startDownload(targetUrl: String, activity: Activity) {
        when (identifySourcePage(targetUrl)) {
            KISSMANGA -> downloadFromKissManga(targetUrl, activity)
            READMANGA -> downloadFromReadManga(targetUrl, activity)
        }
    }

    private fun identifySourcePage(targetUrl: String): String {
        val url = targetUrl.lowercase(Locale.ROOT)
        return if (url.contains(KISSMANGA)) {
            KISSMANGA
        }else if(url.contains(READMANGA)){
            READMANGA
        } else {
            return ""
        }
    }

    private fun downloadFromReadManga(targetUrl: String, activity: Activity) {
        val chapterList = readmanga.getChapterList(targetUrl)
        val chapterMap = readmanga.getChapterMap(targetUrl)
        val mangaName = readmanga.getMangaName(targetUrl)
        chapterList.reverse()

        for(c in chapterList){
            val imageSources: List<String> = readmanga.getImageUrlList(chapterMap[c])
            val images: List<ByteArray> = readmanga.downloadImageList(imageSources)

            val chapterName = c

            var chapterIndex = 0
            for (b in images) {
                saveToInternalStorage(
                    BitmapFactory.decodeByteArray(b, 0, b.size),
                    activity,
                    mangaName,
                    chapterName,
                    chapterIndex
                )
                chapterIndex++
                println("-Downloaded Chapter/image: " + c.trim { it <= ' ' } + "/" + chapterIndex)
            }
        }
    }

    private fun downloadFromKissManga(targetUrl: String, activity: Activity) {
        val chapterList = kissmanga.getChapterList(targetUrl)
        val chapterMap = kissmanga.getChapterMap(targetUrl)
        chapterList.reverse()

        for (c in chapterList) {
            val imageSources: List<String> = kissmanga.getImageUrlList(chapterMap
                [c.split(" - ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]]
            )
            val images: List<ByteArray> = kissmanga.downloadImageList(imageSources)

            val mangaName = c.split(" - ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0].trim { it <= ' ' }
            val chapterName = c.split(" - ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1].trim { it <= ' ' }

            var chapterIndex = 0
            for (b in images) {
                saveToInternalStorage(
                    BitmapFactory.decodeByteArray(b, 0, b.size),
                    activity,
                    mangaName,
                    chapterName,
                    chapterIndex
                )
                chapterIndex++
                println("-Downloaded Chapter/image: " + c.trim { it <= ' ' } + "/" + chapterIndex)
            }
        }
    }


    private fun saveToInternalStorage(
        bitmapImage: Bitmap,
        activity: Activity,
        mangaName: String,
        chapterName: String,
        index: Int
    ): String {
        val cw = ContextWrapper(activity.applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        // Create imageDir
        val mangaPath = File(directory.absolutePath + "/" + mangaName)
        if (!mangaPath.exists()) {
            mangaPath.mkdir()
        }
        val chapterPath = File(mangaPath.absolutePath + "/" + chapterName)
        if (!chapterPath.exists()) {
            chapterPath.mkdir()
        }
        val myPath = File(chapterPath, "$index.jpg")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(myPath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.absolutePath
    }

    fun loadImageFromStorage(path: String?): Bitmap? {
        var b: Bitmap? = null
        try {
            val f = File(path)
            val fis = FileInputStream(f)
            b = BitmapFactory.decodeStream(fis)
            fis.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return b
    }

     fun loadImageFromStorage(filePath: String, application: Application): Bitmap? {
        val options = BitmapFactory.Options().apply {
            // Set inSampleSize to downsample the image
            inSampleSize = 2
        }
        return try {
            val file = File(filePath)
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteFromInternalStorage(mangaName: String, activity: Activity) {
        val cw = ContextWrapper(activity.applicationContext)
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        val mangaPath = File(directory.absolutePath + "/" + mangaName)
        if (mangaPath.exists()) {
            try {
                mangaPath.deleteRecursively()
                println("Delete Successfully deleted manga folder: $mangaName")
            } catch (e: Exception) {
                println("Delete Failed to delete manga folder: $mangaName")
                println(e)
            }
        } else {
            println("Delete Manga folder does not exist: $mangaName")
        }
    }

}