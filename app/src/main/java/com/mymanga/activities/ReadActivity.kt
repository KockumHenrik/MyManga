
package com.mymanga.activities

import android.annotation.SuppressLint
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.mymanga.R
import com.mymanga.controller.Controller
import com.mymanga.data.ChapterViewModel
import com.mymanga.utils.DoubleClickListener
import java.io.File
import java.util.Date

class ReadActivity : AppCompatActivity() {

    private val controller = Controller.getInstance()
    private var currentChapter: Int = 0
    private var currentManga: String? = null

    private lateinit var newMangaView: LinearLayout
    private lateinit var spinner: Spinner
    private lateinit var scrollView: ScrollView
    private lateinit var viewModel: ChapterViewModel
    var adapter: ArrayAdapter<String>? = null

    private var renderingThread: Thread? = null
    private var shouldRender: Boolean = true // Flag to control rendering

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        supportActionBar?.hide()

        viewModel = ViewModelProvider(this)[ChapterViewModel::class.java]
        currentManga = intent.extras?.getString("mangaName")
        viewModel.loadChapters(currentManga!!, this)

        spinner = Spinner(this)
        newMangaView = findViewById(R.id.layoutMangaRead)
        newMangaView.addView(spinner)
        scrollView = findViewById(R.id.scrollViewRead)

        viewModel.chapterList.observe(this){
            fillDropDown()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun fillDropDown() {
//        val cw = ContextWrapper(this.applicationContext)
//        val directory = cw.getDir("imageDir", MODE_PRIVATE)
//        val mangaPath = File(directory.absolutePath + "/" + currentManga)
//        val chapters = mangaPath.listFiles()

        adapter?.clear()

        val items = mutableListOf<String>()

        for (c in viewModel.chapterList.value!!) {
            val chapterPathArray = c.toString().split("/")
            val chapterName = chapterPathArray[chapterPathArray.size - 1]
            items.add(chapterName)
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentChapter = position
                stopRendering()
                shouldRender = true // Reset the rendering flag
                renderImages()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
        spinner.setSelection(currentChapter)
    }

    private fun stopRendering() {
        shouldRender = false // Stop rendering immediately
        renderingThread?.interrupt()
        renderingThread = null
        runOnUiThread {
            newMangaView.removeAllViews()
            newMangaView.addView(spinner)
        }
    }

    private fun renderImages() {
        runOnUiThread {
            newMangaView.removeAllViews()
            newMangaView.addView(spinner)
        }
        val chapters = viewModel.getAvailableChapterList(currentManga!!, this@ReadActivity)
        val i = chapters?.get(currentChapter)
        val indexes = i?.listFiles()
        for (j in indexes!!) {
            if (!shouldRender) return // Check if rendering should continue
            val imgView = ImageView(this)
            val b = controller.loadImageFromStorage(j.absolutePath)
            imgView.setImageBitmap(b)
            imgView.background = getDrawable(R.drawable.border)
            imgView.setAdjustViewBounds(true)
            imgView.setOnClickListener(DoubleClickListener(callback = object :
                DoubleClickListener.Callback {
                override fun doubleClicked() {
                    if (chapters != null) {
                        viewModel.loadChapters(currentManga!!, this@ReadActivity)
                        if (currentChapter < chapters.size - 1) {
                            currentChapter++
                            spinner.setSelection(currentChapter)
                        }
                    }
                }
            }))
            runOnUiThread {
                newMangaView.addView(imgView)
            }
        }
        scrollView.post{scrollView.scrollTo(0,0)}
    }

    fun goToMangaViewFromRead(view: View) {
        stopRendering()
        newMangaView.removeAllViews()
        startActivity(Intent(this@ReadActivity, ViewActivity::class.java))
        finish()
    }
}
