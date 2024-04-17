package com.mymanga.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import com.mymanga.R
import com.mymanga.controller.Controller
import com.mymanga.controller.MangaApplication
import com.mymanga.utils.DoubleClickListener

class ReadActivity : AppCompatActivity() {

    private lateinit var controller: Controller
    private var currentChapter: Int = 0
    private var currentManga: String? = null

    private lateinit var newMangaView: LinearLayout
    private lateinit var spinner: Spinner
    private lateinit var scrollView: ScrollView
    private val viewModel by lazy { (application as MangaApplication).viewModel }
    private var adapter: ArrayAdapter<String>? = null

    private var renderingThread: Thread? = null
    private var shouldRender: Boolean = true // Flag to control rendering

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        controller = Controller(application)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        supportActionBar?.hide()

        currentManga = intent.extras?.getString("mangaName")

        spinner = Spinner(this)
        newMangaView = findViewById(R.id.layoutMangaRead)
        newMangaView.addView(spinner)
        scrollView = findViewById(R.id.scrollViewRead)

        viewModel.loadManga(this, application)
        viewModel.mangas.observe(this) {
            fillDropDown()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun fillDropDown() {
        adapter?.clear()
        val items = mutableListOf<String>()
        val chapterList = viewModel.getChaptersFromTargetManga(currentManga!!)
        for (c in chapterList) {
            c.name?.let { items.add(it) }
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
        val chapterList = viewModel.getChaptersFromTargetManga(currentManga!!)
        chapterList[currentChapter].images?.let {
            for (image in it) {
                val imgView = ImageView(this)
                imgView.setImageBitmap(image)
                imgView.background = getDrawable(R.drawable.border)
                imgView.setAdjustViewBounds(true)
                imgView.setOnClickListener(
                    DoubleClickListener(callback = object :
                        DoubleClickListener.Callback {
                        override fun doubleClicked() {
                            viewModel.loadManga(this@ReadActivity, application)
                            if (currentChapter < chapterList.size - 1) {
                                currentChapter++
                                spinner.setSelection(currentChapter)
                            }
                        }
                    })
                )
                runOnUiThread {
                    newMangaView.addView(imgView)
                }
            }
        }
        scrollView.post { scrollView.scrollTo(0, 0) }
    }

    fun goToMangaViewFromRead(view: View) {
        stopRendering()
        newMangaView.removeAllViews()
        startActivity(Intent(this@ReadActivity, ViewActivity::class.java))
        finish()
    }
}
