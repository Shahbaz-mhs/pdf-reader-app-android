package com.pdfreader.freecodedfapp.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.pdfreader.freecodedfapp.adapters.SelectImagesAdapter.OnImageSelectedListener
import com.pdfreader.freecodedfapp.data.DbHelper
import androidx.recyclerview.widget.RecyclerView
import android.widget.ProgressBar
import android.widget.AdapterView
import android.content.SharedPreferences
import android.content.Intent
import android.os.Bundle
import com.pdfreader.freecodedfapp.R
import android.widget.Spinner
import android.os.AsyncTask
import com.pdfreader.freecodedfapp.adapters.SelectImagesAdapter
import android.os.Environment
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import com.pdfreader.freecodedfapp.MyApp
import com.pdfreader.freecodedfapp.data.Constants
import com.pdfreader.freecodedfapp.utils.Utils
import java.util.ArrayList

class `SelectImagesActivity-old` : AppCompatActivity(), OnImageSelectedListener {

    var context: Context? = null

    var dbHelper: DbHelper? = null

    var imagesRecyclerView: RecyclerView? = null

    var numberOfColumns = 0

    var progressBar: ProgressBar? = null
    var selectedListener: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, j: Long) {
                if (i == 0) {
                    LoadImages("/").execute(*arrayOfNulls<Void>(0))
                } else if (i == 1) {
                    LoadImages("/DCIM/").execute(*arrayOfNulls<Void>(0))
                } else if (i == 2) {
                    LoadImages("/Download/").execute(*arrayOfNulls<Void>(0))
                } else if (i == 3) {
                    LoadImages("/Pictures/").execute(*arrayOfNulls<Void>(0))
                } else if (i == 4) {
                    LoadImages("/WhatsApp/Media/WhatsApp Images/").execute(*arrayOfNulls<Void>(0))
                }
            }
        }
    private var sharedPreferences: SharedPreferences? = null
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        if (MyApp.getInstance().isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        setContentView(R.layout.activity_select_images)
        setSupportActionBar(findViewById<View>(R.id.toolbar_select_images) as Toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val spinner = findViewById<View>(R.id.spinner_img_directories) as Spinner
        imagesRecyclerView = findViewById<View>(R.id.recycler_view_select_images) as RecyclerView
        progressBar = findViewById<View>(R.id.progress_bar_select_images) as ProgressBar
        dbHelper = DbHelper.getInstance(this)
        context = this
        val i = if (Utils.isTablet(this)) 6 else 3
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences = defaultSharedPreferences
        numberOfColumns = defaultSharedPreferences.getInt(Constants.GRID_VIEW_NUM_OF_COLUMNS, i)
        spinner.setSelection(0)
        spinner.onItemSelectedListener = selectedListener
    }

    override fun onMultiSelectedPDF(arrayList: ArrayList<String>) {
        val intent = Intent(this, OrganizeImagesActivity::class.java)
        intent.putStringArrayListExtra(Constants.IMAGE_URIS, arrayList)
        startActivity(intent)
    }

    inner class LoadImages(private val imageDir: String) : AsyncTask<Void?, Void?, Void?>() {
        private var adapter: SelectImagesAdapter? = null

        public override fun onPreExecute() {
            super.onPreExecute()
            progressBar!!.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg voidArr: Void?): Void? {
            val `access$100` = dbHelper
            adapter = SelectImagesAdapter(
                context,
                `access$100`!!.getAllImages(
                    Environment.getExternalStorageDirectory().toString() + imageDir
                )
            )
            return null
        }

        public override fun onPostExecute(voidR: Void?) {
            super.onPostExecute(voidR)
            progressBar!!.visibility = View.GONE
            imagesRecyclerView!!.layoutManager = GridLayoutManager(
                context,
                numberOfColumns,
                RecyclerView.VERTICAL,
                false
            )
            imagesRecyclerView!!.adapter = adapter
        }
    }

    companion object {
        val TAG = `SelectImagesActivity-old`::class.java.simpleName
    }
}