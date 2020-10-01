package com.udacity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        setSupportActionBar(toolbar)

        val fileUrl = intent.getStringExtra(FILE_URL_KEY)
        val status = intent.getStringExtra(STATUS_KEY)

        back.setOnClickListener {
            finish()
        }

        fileUrl?.let { fileNameTextView.text = it }
        status?.let { statusTextView.text = it }

    }
}
