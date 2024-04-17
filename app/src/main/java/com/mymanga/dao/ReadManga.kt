package com.mymanga.dao

import java.io.IOException

class ReadManga: Netpage() {

    @Throws(IOException::class)
    private fun getMangaOverviewSource(targetUrl: String): String {
        val fullSource = fetchPageSource(targetUrl)
        return fullSource.split(
            ("<div id=\"cmtb-0\" class=\"cm-tabs-content  active novels-detail-chapters\">\n" +
                    "                                <ul>\n" +
                    "\n" +
                    "\n" +
                    "                                                                            <li>")
                .toRegex()
        ).dropLastWhile { it.isEmpty() }.toTypedArray<String>()[1].split(
            ("</li>\n" +
                    "                                                                    </ul>\n" +
                    "                            </div>\n" +
                    "                        \n" +
                    "                    </div>\n" +
                    "\n" +
                    "                    <div class=\"col-md-12 mt-3 mb-3\">\n" +
                    "                        <div class=\"similar-novels p-3\">").toRegex()
        ).dropLastWhile { it.isEmpty() }.toTypedArray<String>()[0]
    }

    @Throws(IOException::class)
    fun getMangaName(targetUrl: String): String {
        val source = fetchPageSource(targetUrl)
        return source.split("<title>".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray<String>()[1]
            .split("</title>".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray<String>()[0]
            .replace("\n", "")
            .replace("\\s+".toRegex(), " ")
    }
    @Throws(IOException::class)
    fun getChapterList(targetUrl: String): MutableList<String> {
        val chapterSource = getMangaOverviewSource(targetUrl)
        val eachChapterSource = chapterSource.split("</li>")
        val chapters: MutableList<String> = mutableListOf()
        for(chap in eachChapterSource){
            val c = chap.split("<a href=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                .split(">".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                .split("</a>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                .replace("</a", "")
                .replace("\n", "")
                .replace("\\s+".toRegex(), " ")
            val chPlusNumb = c.split(" ")
            chapters.add("${chPlusNumb[1]} ${chPlusNumb[2]}")
        }
        return chapters
    }

    @Throws(IOException::class)
    fun getUrlList(targetUrl: String): List<String> {
        val chapterSource = getMangaOverviewSource(targetUrl)
        val eachChapterSource = chapterSource.split("</li>".toRegex()
        ).dropLastWhile { it.isEmpty() }.toTypedArray()
        val urls: MutableList<String> = mutableListOf()
        for(chap in eachChapterSource){
            val u = chap.split("<a href=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                .split(">".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            urls.add(u.replace("\"", ""))
        }
        return urls
    }

    fun getChapterMap(targetUrl: String): Map<String, String> {
        val chapterMap: MutableMap<String, String> = HashMap()
        val chapters = getChapterList(targetUrl)
        val urls = getUrlList(targetUrl)
        for(i in chapters.indices){
            chapterMap[chapters[i]]  = urls[i]
        }
        return chapterMap
    }

    @Throws(IOException::class)
    fun getImageUrlList(targetUrl: String?): List<String> {
        val pageSource = fetchPageSource(targetUrl)
        val eachLine = pageSource.split("\n".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val imgSourcesTemp: MutableList<String> = mutableListOf()
        for(s in eachLine){
            if(s.contains("img src") && !s.contains("logo")){
                imgSourcesTemp.add(s)
            }
        }
        imgSourcesTemp.removeAt(imgSourcesTemp.size-1)
        imgSourcesTemp.removeAt(imgSourcesTemp.size-1)
        val imageSources: MutableList<String> = mutableListOf()
        for(i in imgSourcesTemp) {
            if (!i.contains("</a>")) {
                val tmp = i.split("<img src=\"".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1].split("\">".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0]
                imageSources.add(tmp)
            }
        }
        return imageSources
    }
}