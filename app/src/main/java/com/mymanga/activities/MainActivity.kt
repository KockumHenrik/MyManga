package com.mymanga.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.mymanga.R
import com.mymanga.controller.Controller

class MainActivity : AppCompatActivity() {

    private val controller = Controller.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun downloadManga(view: View) {
        Thread {
            run {
                val txtFieldMangaUrl = findViewById<TextInputEditText>(R.id.txtFieldMangaUrl)
                val url = txtFieldMangaUrl.text.toString()
                if (url.isNotEmpty()) {
                    Snackbar.make(
                        view,
                        "Started Downloading",
                        BaseTransientBottomBar.LENGTH_LONG
                    )
                        .show()
                    try {
                        controller.startDownload(url, this)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }

    fun goToMangaView(view: View?) {
        startActivity(Intent(this@MainActivity, ViewActivity::class.java))
        finish()
    }


}