package com.mymanga.controller

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mymanga.dao.KissmangaDAO
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

class Controller (private val application: Application) {

    private val kissmangaDAO = KissmangaDAO()

    private val KISSMANGA = "kissmanga"

    fun startDownload(targetUrl: String, activity: Activity) {
        when (identifySourcePage(targetUrl)) {
            KISSMANGA -> downloadFromKissManga(targetUrl, activity)

        }
    }

    private fun identifySourcePage(targetUrl: String): String {
        val url = targetUrl.lowercase(Locale.ROOT)
        return if (url.contains(KISSMANGA)) {
            KISSMANGA
        } else {
            return ""
        }
    }

    fun downloadFromKissManga(targetUrl: String, activity: Activity) {
        val chapterList = kissmangaDAO.getChapterList(targetUrl)
        val chapterMap = kissmangaDAO.getChapterMap(targetUrl)
        chapterList.reverse()

        for (c in chapterList) {
            val imageSources: List<String> = kissmangaDAO.getImageUrlList(chapterMap
                [c.split(" - ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]]
            )
            val images: List<ByteArray> = kissmangaDAO.downloadImageList(imageSources)

            var chapterIndex = 0
            for (b in images) {
                saveToInternalStorage(
                    BitmapFactory.decodeByteArray(b, 0, b.size),
                    activity,
                    c,
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
        imageName: String,
        index: Int
    ): String {
        val cw = ContextWrapper(activity.applicationContext)
        // path to /data/data/yourapp/app_data/imageDir
        val directory = cw.getDir("imageDir", Context.MODE_PRIVATE)
        // Create imageDir
        val mangaName = imageName.split(" - ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[0].trim { it <= ' ' }
        val chapterName = imageName.split(" - ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[1].trim { it <= ' ' }
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