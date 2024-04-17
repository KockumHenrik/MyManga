package com.mymanga.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mymanga.R
import com.mymanga.controller.Controller
import com.mymanga.controller.MangaApplication

class ViewActivity: AppCompatActivity() {

    private lateinit var newMangaView: LinearLayout
    private lateinit var controller: Controller
    private val viewModel by lazy { (application as MangaApplication).viewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
        supportActionBar?.show()

        controller = Controller(application)
        newMangaView = findViewById(R.id.layoutMangaView)

        viewModel.loadManga(this, application)
        viewModel.mangas.observe(this){
            renderScreen()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun renderScreen(){
        newMangaView.removeAllViews()
//        val mangaList = mangaViewModel.mangaList.value
        val mangaList = viewModel.mangas.value
        if(mangaList != null) {
            for (manga in mangaList) {
                // Inflate the custom view
                val customMangaItem =
                    layoutInflater.inflate(R.layout.view_resource, null) as LinearLayout
                val textMangaName = customMangaItem.findViewById<TextView>(R.id.textMangaName)
                val btnDelete = customMangaItem.findViewById<ImageButton>(R.id.btnDelete)

                // Set the manga name
                textMangaName.text = manga.name

                // Set onClickListener for custom view
                customMangaItem.setOnClickListener {
                    val intent = Intent(this@ViewActivity, ReadActivity::class.java)
                    intent.putExtra("mangaName", textMangaName.text.toString())
                    startActivity(intent)
                }

                btnDelete.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            btnDelete.setBackgroundColor(Color.DKGRAY)
                            Snackbar.make(
                                this.window.decorView,
                                "Deleting ${textMangaName.text}",
                                BaseTransientBottomBar.LENGTH_LONG
                            )
                                .show()
                            deleteButtonPressed(textMangaName.text.toString())
                        }

                        MotionEvent.ACTION_UP -> {
                            btnDelete.setBackgroundColor(Color.WHITE)
                        }
                    }
                    true
                }
                // Add the custom view to the LinearLayout
                newMangaView.addView(customMangaItem)
            }
        }
    }

    private fun deleteButtonPressed(mangaName: String){
        println("Delete button pressed")
        viewModel.deleteByName(mangaName)
        controller.deleteFromInternalStorage(mangaName, this)
        viewModel.loadManga(this, application)
    }

    fun goToNewMangaView(view: View?){
        startActivity(Intent(this@ViewActivity, MainActivity::class.java))
        finish()
    }

    fun refresh(view: View?){
        println("Refresh button pressed")
        viewModel.loadManga(this, application)
    }
}