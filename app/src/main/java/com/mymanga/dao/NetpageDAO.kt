package com.mymanga.dao

import java.io.IOException

interface NetpageDAO {
    fun fetchPageSource(targetUrl: String?): String
    @Throws(IOException::class)
    fun downloadImageList(imageUrls: List<String?>): List<ByteArray>
}