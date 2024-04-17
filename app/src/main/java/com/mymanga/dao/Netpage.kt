package com.mymanga.dao

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL

/**
 * Parent to all page source collector classes
 */
open class Netpage: NetpageDAO {
    override fun fetchPageSource(targetUrl: String?): String {
        var br: BufferedReader? = null
        val url = URL(targetUrl)
        var pageSource = ""
        try {
            br = BufferedReader(InputStreamReader(url.openStream()))
            var line: String
            while (br.readLine().also { line = it } != null) {
                pageSource += line + "\n"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            br?.close()
        }
        return pageSource
    }

    @Throws(IOException::class)
    override fun downloadImageList(imageUrls: List<String?>): List<ByteArray> {
        val responseList: MutableList<ByteArray> = java.util.ArrayList()
        for (i in imageUrls.indices) {
            val url = URL(imageUrls[i])
            try {
                val input: InputStream = BufferedInputStream(url.openStream())
                val out = ByteArrayOutputStream()
                val buf = ByteArray(1024)
                var n = 0
                while (-1 != input.read(buf).also { n = it }) {
                    out.write(buf, 0, n)
                }
                out.close()
                input.close()
                val response = out.toByteArray()
                responseList.add(response)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        return responseList
    }
}