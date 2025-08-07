package com.pdfreader.freecodedfapp.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pdfreader.freecodedfapp.MyApp
import com.pdfreader.freecodedfapp.R
import com.pdfreader.freecodedfapp.adapters.SelectImagesAdapter
import com.pdfreader.freecodedfapp.data.Constants
import com.pdfreader.freecodedfapp.data.DbHelper
import com.pdfreader.freecodedfapp.utils.Utils
import java.io.File
import java.util.*

class SelectImagesActivity : AppCompatActivity(), SelectImagesAdapter.OnImageSelectedListener {

    private var context: Context? = null
    private var dbHelper: DbHelper? = null
    private var imagesRecyclerView: RecyclerView? = null
    private var numberOfColumns = 0
    private var progressBar: ProgressBar? = null
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Night mode setup
        if (MyApp.getInstance().isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_select_images)
        setSupportActionBar(findViewById(R.id.toolbar_select_images) as Toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val spinner = findViewById<Spinner>(R.id.spinner_img_directories)
        imagesRecyclerView = findViewById(R.id.recycler_view_select_images)
        progressBar = findViewById(R.id.progress_bar_select_images)
        dbHelper = DbHelper.getInstance(this)
        context = this

        // Determine number of columns based on device orientation
        val i = if (Utils.isTablet(this)) 6 else 3
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences = defaultSharedPreferences
        numberOfColumns = defaultSharedPreferences.getInt(Constants.GRID_VIEW_NUM_OF_COLUMNS, i)

        // Set up spinner listener
        spinner.setSelection(0)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, j: Long) {
                when (i) {
                    0 -> LoadImages("/").execute()
                    1 -> LoadImages("/DCIM/").execute()
                    2 -> LoadImages("/Download/").execute()
                    3 -> LoadImages("/Pictures/").execute()
                    4 -> LoadImages("/WhatsApp/Media/WhatsApp Images/").execute()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onMultiSelectedPDF(arrayList: ArrayList<String>) {
        val intent = Intent(this, OrganizeImagesActivity::class.java)
        intent.putStringArrayListExtra(Constants.IMAGE_URIS, arrayList)
        startActivity(intent)
    }

    inner class LoadImages(private val imageDir: String) : AsyncTask<Void?, Void?, Void?>() {
        private var adapter: SelectImagesAdapter? = null

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar!!.visibility = View.VISIBLE
        }


        fun getAllImagesSortedByDateDesc(context: Context?, imageDir: String): List<String> {
            val imagesList = mutableListOf<String>()

            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED)
            val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
            val selectionArgs = arrayOf("%$imageDir%")

            // Sort by DATE_ADDED in descending order
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            val cursor: Cursor? = context?.contentResolver?.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                while (cursor.moveToNext()) {
                    val imagePath = cursor.getString(dataIndex)
                    imagesList.add(imagePath)
                }
            }

            return imagesList
        }

        override fun doInBackground(vararg voidArr: Void?): Void? {
            val sortedImagePaths = getAllImagesSortedByDateDesc(context, imageDir)

            // Convert List<String> to List<Uri>
            val sortedImageUris = sortedImagePaths.map { path -> Uri.fromFile(File(path)) }

            // Set the List<Uri> to your adapter
            adapter = SelectImagesAdapter(context, sortedImageUris)

            return null
        }


        override fun onPostExecute(voidR: Void?) {
            super.onPostExecute(voidR)
            progressBar!!.visibility = View.GONE
            imagesRecyclerView!!.layoutManager = GridLayoutManager(context, numberOfColumns, RecyclerView.VERTICAL, false)
            imagesRecyclerView!!.adapter = adapter
        }
    }
}