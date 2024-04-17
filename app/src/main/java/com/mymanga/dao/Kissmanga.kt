package com.mymanga.dao

import java.io.IOException

class Kissmanga : Netpage() {

    @Throws(IOException::class)
    private fun getMangaOverviewSource(targetUrl: String): String {
        val fullSource = fetchPageSource(targetUrl)
        return fullSource.split(
            ("<div class=\"head\">\n" +
                    "                  <div>Chapter name</div>\n" +
                    "                  <div>Day Added</div>\n" +
                    "                </div>").toRegex()
        ).dropLastWhile { it.isEmpty() }.toTypedArray<String>()[1].split(
            ("<div class=\"bigBarContainer full\">\n" +
                    "          <div class=\"barTitle full\">\n" +
                    "            <h2>Popular Manga on Kissmanga</h2>\n" +
                    "          </div>").toRegex()
        ).dropLastWhile { it.isEmpty() }.toTypedArray<String>()[0]
    }

    @Throws(IOException::class)
    fun getChapterList(targetUrl: String): MutableList<String> {
        val chapterSource = getMangaOverviewSource(targetUrl)
        val eachChapterSource = chapterSource.split(
            "\n" +
                    "                \n" +
                    "                "
        )
        val chapters: MutableList<String> = ArrayList()
        for (i in 1 until eachChapterSource.size) {
            val c = eachChapterSource[i].split("<a href=".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
                .split(">".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                .split("</a>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                .replace("</a", "")
                .replace("\n", "")
                .replace("\\s+".toRegex(), " ")
            chapters.add(c)
        }
        return chapters
    }

    @Throws(IOException::class)
    fun getUrlList(targetUrl: String): List<String> {
        val chapterSource = getMangaOverviewSource(targetUrl)
        val eachChapterSource = chapterSource.split(
            ("""
                
                """).toRegex()
        ).dropLastWhile { it.isEmpty() }.toTypedArray()
        val urls: MutableList<String> = ArrayList()
        for (i in 1 until eachChapterSource.size) {
            val u =
                eachChapterSource[i].split("<a href=\"".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1]
                    .split(
                        ("\"\n" +
                                "                        title=").toRegex()
                    ).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            urls.add(u)
        }
        return urls
    }

    fun getChapterMap(targetUrl: String): Map<String, String> {
        val chapterMap: MutableMap<String, String> = HashMap()
        val chapters = getChapterList(targetUrl)
        val urls = getUrlList(targetUrl)
        for (i in chapters.indices) {
            chapterMap[chapters[i].split(" - ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]] =
                urls[i]
        }
        return chapterMap
    }

    private val baseUrl = "https://kissmanga.org"

    @Throws(IOException::class)
    fun getImageUrlList(targetUrl: String?): List<String> {
        val pageSource: String = fetchPageSource(baseUrl + targetUrl)
        val eachLine = pageSource.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val imgSourcesTemp: MutableList<String> = java.util.ArrayList()
        for (s in eachLine) {
            if (s.contains("img src")) {
                imgSourcesTemp.add(s)
            }
        }
        imgSourcesTemp.removeAt(imgSourcesTemp.size - 1)
        imgSourcesTemp.removeAt(0)
        val imageSources: MutableList<String> = java.util.ArrayList()
        for (i in imgSourcesTemp) {
            val tmp = i.split("<img src=\"".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1].split("\">".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0]
            imageSources.add(tmp)
        }
        return imageSources
    }

}