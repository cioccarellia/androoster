package com.andreacioccarelli.androoster.ui.backup

import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import com.andreacioccarelli.androoster.R
import com.andreacioccarelli.androoster.ui.base.BaseActivity

class UIBackup : BaseActivity() {

    val fab: FloatingActionButton get() = findViewById(R.id.fab)
    val toolbar_layout: CollapsingToolbarLayout get() = findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.close_activity)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}
