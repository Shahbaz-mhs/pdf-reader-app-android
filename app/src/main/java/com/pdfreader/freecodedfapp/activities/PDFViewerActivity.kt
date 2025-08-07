package com.pdfreader.freecodedfapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.util.Constants
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.pdfreader.freecodedfapp.BuildConfig
import com.pdfreader.freecodedfapp.MyApp
import com.pdfreader.freecodedfapp.R
import com.pdfreader.freecodedfapp.data.DbHelper
import com.pdfreader.freecodedfapp.fragments.SettingsFragment
import com.pdfreader.freecodedfapp.fragments.TableContentsFragment
import com.pdfreader.freecodedfapp.helper.DataUpdatedEvent.PermanetlyDeleteEvent
import com.pdfreader.freecodedfapp.helper.ScrollHandle
import com.pdfreader.freecodedfapp.utils.AdManagerInterstitial
import com.pdfreader.freecodedfapp.utils.Utils
import com.shockwave.pdfium.PdfPasswordException
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.greenrobot.eventbus.EventBus
import java.io.File

class PDFViewerActivity : AppCompatActivity() {

    val TAG = PDFViewerActivity::class.java.simpleName
    private val mHideHandler = Handler()
    private val mShowPart2Runnable = Runnable { `lambda$new$2$PDFViewerActivity`() }

    var pageNumberTextview: TextView? = null
    var backgroundLoadPdf: AsyncTask<*, *, *>? = null
    var colorPrimaryDark = 0
    var colorPrimaryDarkNight = 0
    var context: Context? = null
    var dbHelper: DbHelper? = null
    var filePath: String? = null
    var fitPolicy: FitPolicy? = null
    var flags = 0
    var mActionBar: ActionBar? = null
    var mAdView: AdView? = null


    var mPassword = ""
    var nightModeEnabled = false
    var onPageChangeListener = OnPageChangeListener { i, i2 ->
        pageNumberTextview!!.text = (i + 1).toString() + " / " + i2

        // Show the pageNumberTextview when the page changes
        pageNumberTextview!!.visibility = View.VISIBLE

        // Hide it again after 3 seconds of no scroll
        Handler().removeCallbacksAndMessages(null) // Remove any existing delayed actions
        Handler().postDelayed({
            pageNumberTextview!!.visibility = View.GONE
        }, 3000)
    }


    var openPdfProgress: ProgressBar? = null
    var onLoadCompleteListener = OnLoadCompleteListener {
        openPdfProgress!!.visibility = View.GONE
        pageNumberTextview!!.visibility = View.VISIBLE

        // Use a Handler to hide the pageNumberTextView after 3 seconds
        Handler().postDelayed({
            pageNumberTextview!!.visibility = View.GONE
        }, 3000)

    }


    var pageNumber = 0
    var pdfContainer: LinearLayout? = null
    var pdfFileLocation: String? = null
    var pdfView: PDFView? = null
    private val mHidePart2Runnable = Runnable { pdfView!!.systemUiVisibility = 4615 }
    var sharedPreferences: SharedPreferences? = null
    var swipeHorizontalEnabled = false
    var toolbar: Toolbar? = null
    var uri: Uri? = null
    var onErrorListener = OnErrorListener { th ->
        if (th is PdfPasswordException) {
            showEnterPasswordDialog()
            MobileAds.initialize(this){}

            return@OnErrorListener
        }
        Toast.makeText(this@PDFViewerActivity, th.message, Toast.LENGTH_LONG).show()
        openPdfProgress!!.visibility = android.view.View.GONE
    }
    var view: View? = null
    private var AUTO_HIDE = false
    private var mMenu: Menu? = null
    private var mVisible = true
    private val mHideRunnable = Runnable { `lambda$new$3$PDFViewerActivity`() }
    var toggleFullScreen = View.OnClickListener { view -> `lambda$new$4$PDFViewerActivity`(view) }
    private var rememberLastPage = false
    private var showRemoveAds = false
    private var stayAwake = false

    @SuppressLint("MissingInflatedId")
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        if (MyApp.getInstance().isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        setContentView(R.layout.activity_pdf_viewer)
        toolbar = findViewById<View>(R.id.toolbar_home) as Toolbar
        val bannerView: View = findViewById(R.id.bannerView)
        val timerText: TextView = findViewById(R.id.timerText)
        var bannerTimerRelative = findViewById<View>(R.id.bannerTimerRelative) as RelativeLayout
        openPdfProgress = findViewById<View>(R.id.progress_bar_open_pdf) as ProgressBar
        pageNumberTextview = findViewById<View>(R.id.page_numbers) as TextView
        pdfView = findViewById<View>(R.id.pdfView) as PDFView
        pdfContainer = findViewById<View>(R.id.pdf_container) as LinearLayout
        context = this
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        mActionBar = supportActionBar
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences = defaultSharedPreferences
        stayAwake = defaultSharedPreferences.getBoolean(SettingsFragment.KEY_PREFS_STAY_AWAKE, true)
        rememberLastPage =
            sharedPreferences!!.getBoolean(SettingsFragment.KEY_PREFS_REMEMBER_LAST_PAGE, true)
        var i = 0
        AUTO_HIDE = sharedPreferences!!.getBoolean(SettingsFragment.AUTO_FULL_SCREEN_ENABLED, false)
        swipeHorizontalEnabled =
            sharedPreferences!!.getBoolean(SettingsFragment.SWIPE_HORIZONTAL_ENABLED, false)
        nightModeEnabled =
            sharedPreferences!!.getBoolean(SettingsFragment.NIGHT_MODE_ENABLED_PDFVIEW, false)
        val decorView = (context as Activity?)!!.window.decorView
        view = decorView
        flags = decorView.systemUiVisibility
        colorPrimaryDark = context!!.resources.getColor(R.color.colorPrimaryDark)
        colorPrimaryDarkNight = context!!.resources.getColor(R.color.colorPrimaryDarkNight)
        Constants.THUMBNAIL_RATIO = 0.7f
        val intent = intent
        pdfFileLocation =
            intent.getStringExtra(com.pdfreader.freecodedfapp.data.Constants.PDF_LOCATION)
        showRemoveAds = intent.getBooleanExtra(
            com.pdfreader.freecodedfapp.data.Constants.SHOW_REMOVE_ADS,
            false
        )
        uri = intent.data
        dbHelper = DbHelper.getInstance(this)
        pdfView!!.keepScreenOn = stayAwake
        if (rememberLastPage) {
            i = dbHelper!!.getLastOpenedPage(pdfFileLocation)
        }
        pageNumber = i
        val fitPolicy2 =
            if (Utils.isTablet(this) || swipeHorizontalEnabled) FitPolicy.HEIGHT else FitPolicy.WIDTH
        fitPolicy = fitPolicy2
        loadPdfFile(mPassword, pageNumber, swipeHorizontalEnabled, nightModeEnabled, fitPolicy2)
        pdfView!!.setOnClickListener(toggleFullScreen)
        setShowRemoveAds()


        // Initialize the Mobile Ads SDK
        MobileAds.initialize(this) { }

        // Show timer and banner ad
        bannerView.visibility = View.VISIBLE
        timerText.visibility = View.VISIBLE

// Create a countdown timer for 5 seconds
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                timerText.text = secondsLeft.toString() // Only display the number
            }

            override fun onFinish() {
                // Hide the banner ad and timer after 5 seconds
                bannerTimerRelative.visibility = View.GONE
//                bannerView.visibility = View.GONE
//                timerText.visibility = View.GONE
            }
        }.start()







    }

    public override fun onResume() {
        super.onResume()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val asyncTask = backgroundLoadPdf
        asyncTask?.cancel(true)
    }

//    override fun onBackPressed() {
//        // Check if this activity was opened from another activity
//        if (isTaskRoot) {
//            // If this is the root activity, navigate to the previous activity
//            super.onBackPressed()
//        } else {
//            // Otherwise, simply finish this activity and go back
//            finish()
//        }
//
//        // Cancel any background task that might be running
//        backgroundLoadPdf?.cancel(true)
//    }

    public override fun onDestroy() {
        super.onDestroy()
        val asyncTask = backgroundLoadPdf
        asyncTask?.cancel(true)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_pdf_viewer, menu)
        mMenu = menu
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        val findItem = mMenu!!.findItem(R.id.action_toggle_view)
        val findItem2 = mMenu!!.findItem(R.id.action_toggle_night_mode)
        setupToggleSwipeIcons(findItem, swipeHorizontalEnabled)
        setupNightModeIcons(findItem2, nightModeEnabled)
        Handler(Looper.getMainLooper()).postDelayed({
        }, 2000)
        return true
    }

    public override fun onActivityResult(i: Int, i2: Int, intent: Intent?) {
        super.onActivityResult(i, i2, intent)
        if (i == 7 && i2 == -1) {
            pdfView!!.jumpTo(intent!!.getIntExtra(com.pdfreader.freecodedfapp.data.Constants.PAGE_NUMBER, pdfView!!.currentPage) - 1, true)
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_bookmark -> addPageBookmark(this, filePath, pdfView!!.currentPage + 1)
            R.id.action_contents -> showContents(filePath)
            R.id.action_delete -> showDeleteConfirmDialog(filePath)
            R.id.action_jump_to_page -> jumpToPage()
            R.id.action_pdf_tools -> showPdfTools()
            R.id.action_print -> print() //R.id.action_print
            R.id.action_share -> share()
            R.id.action_share_as_picture -> shareAsPicture()
            R.id.action_toggle_night_mode -> toggleNightMode(menuItem)
            R.id.action_toggle_view -> togglePDFView(menuItem)
        }
        return super.onOptionsItemSelected(menuItem)
    }

    public override fun onStop() {
        super.onStop()
        sharedPreferences!!.edit().putInt(TableContentsFragment.SAVED_STATE, 0).apply()
        if (rememberLastPage && !TextUtils.isEmpty(pdfFileLocation)) {
            AsyncTask.execute { `lambda$onStop$0$PDFViewerActivity`() }
        }
    }

    /* synthetic */   fun `lambda$onStop$0$PDFViewerActivity`() {
        dbHelper!!.addLastOpenedPage(filePath, pdfView!!.currentPage)
    }

//    fun loadPdfFile(str: String?, i: Int, z: Boolean, z2: Boolean, fitPolicy2: FitPolicy?) {
//        val configurator: PDFView.Configurator?
//        val uri2 = uri
//        if (uri2 != null) {
//            try {
//                filePath = uri2.path
//                mActionBar!!.title = File(filePath).name as CharSequence
//            } catch (e: Exception) {
//                mActionBar!!.title = "View PDF"
//                e.printStackTrace()
//            }
//            configurator = pdfView!!.fromUri(uri)
//        } else if (!TextUtils.isEmpty(pdfFileLocation)) {
//            filePath = pdfFileLocation
//            val file = File(pdfFileLocation)
//            val str2 = TAG
//            Log.d(str2, "path from selection " + file.path)
//            mActionBar!!.title = file.name as CharSequence
//            val fromFile = pdfView!!.fromFile(file)
//            AsyncTask.execute(object : Runnable {
//                /* synthetic */ var `f$1`: File? = null
//                override fun run() {
//                    `lambda$loadPdfFile$1$PDFViewerActivity`(`f$1`)
//                }
//
//                init {
//                    `f$1` = file
//                }
//            })
//            configurator = fromFile
//        } else {
//            configurator = null
//        }
//        configurator?.scrollHandle(
//            ScrollHandle(
//                this
//            )
//        )?.password(str)
//            ?.enableAnnotationRendering(true)?.pageFitPolicy(fitPolicy2)?.spacing(6)?.defaultPage(i)
//            ?.swipeHorizontal(z)?.autoSpacing(z)?.pageFling(z)?.pageSnap(z)?.nightMode(z2)
//            ?.onPageChange(onPageChangeListener)?.onLoad(onLoadCompleteListener)?.onError(
//                onErrorListener
//            )
//            ?.load()
//    }

    fun loadPdfFile(
        str: String?,
        i: Int,
        z: Boolean,
        z2: Boolean,
        fitPolicy2: FitPolicy?
    ) {
        val configurator: PDFView.Configurator?
        val uri2 = uri
        if (uri2 != null) {
            try {
                filePath = uri2.path
                mActionBar!!.title = File(filePath).name as CharSequence
            } catch (e: Exception) {
                mActionBar!!.title = "View PDF"
                e.printStackTrace()
            }
            configurator = pdfView!!.fromUri(uri)
        } else if (!TextUtils.isEmpty(pdfFileLocation)) {
            filePath = pdfFileLocation
            val file = File(pdfFileLocation)
            val str2 = TAG
            Log.d(str2, "path from selection " + file.path)
            mActionBar!!.title = file.name as CharSequence
            val fromFile = pdfView!!.fromFile(file)
            AsyncTask.execute(object : Runnable {
                /* synthetic */ var `f$1`: File? = null
                override fun run() {
                    `lambda$loadPdfFile$1$PDFViewerActivity`(`f$1`)
                }

                init {
                    `f$1` = file
                }
            })
            configurator = fromFile
        } else {
            configurator = null
        }

        configurator
            ?.scrollHandle(ScrollHandle(this))
            ?.password(str)
            ?.enableAnnotationRendering(true)
            ?.pageFitPolicy(fitPolicy2)
//            ?.spacing(6)
            ?.defaultPage(i)
            ?.swipeHorizontal(z)
            ?.onPageChange(onPageChangeListener)?.onLoad(onLoadCompleteListener)?.onError(
                onErrorListener
            )
            ?.autoSpacing(z)
            ?.pageFling(z)
            ?.pageSnap(z)
            ?.nightMode(z2)
            ?.onLoad(onLoadCompleteListener)
            ?.onError(onErrorListener)
            ?.load()
    }

    // Function to handle the fake last page logic
//    private fun showAdContainer() {
//        adContainer.visibility = View.VISIBLE
//    }


    /* synthetic */   fun `lambda$loadPdfFile$1$PDFViewerActivity`(file: File?) {
        dbHelper!!.addRecentPDF(file!!.absolutePath)
    }

    fun getName(uri2: Uri?): String {
        val query = contentResolver.query(
            uri2!!,
            null as Array<String?>?,
            null as String?,
            null as Array<String?>?,
            null as String?
        )
            ?: return "Unknown"
        val columnIndex = query.getColumnIndex("_display_name")
        query.moveToFirst()
        val string = query.getString(columnIndex)
        query.columnNames
        query.close()
        return string
    }

    fun showShareAsPicture(uri2: Uri?) {
    }

    /* synthetic */   fun `lambda$new$2$PDFViewerActivity`() {
        val supportActionBar = supportActionBar
        supportActionBar?.show()
    }

    /* synthetic */   fun `lambda$new$4$PDFViewerActivity`(view2: View?) {
        toggle()
    }

    fun togglePDFView(menuItem: MenuItem) {
        swipeHorizontalEnabled =
            sharedPreferences!!.getBoolean(SettingsFragment.SWIPE_HORIZONTAL_ENABLED, false)
        val z = sharedPreferences!!.getBoolean(SettingsFragment.NIGHT_MODE_ENABLED_PDFVIEW, false)
        val edit = sharedPreferences!!.edit()
        setupToggleSwipeIcons(menuItem, !swipeHorizontalEnabled)
        if (swipeHorizontalEnabled) {
            loadPdfFile(
                mPassword,
                pdfView!!.currentPage,
                !swipeHorizontalEnabled,
                z,
                FitPolicy.WIDTH
            )
            edit.putBoolean(SettingsFragment.SWIPE_HORIZONTAL_ENABLED, !swipeHorizontalEnabled)
                .apply()
            Toast.makeText(context, "Vertical swipe enabled", Toast.LENGTH_SHORT).show()
            return
        }
        loadPdfFile(mPassword, pdfView!!.currentPage, !swipeHorizontalEnabled, z, FitPolicy.HEIGHT)
        edit.putBoolean(SettingsFragment.SWIPE_HORIZONTAL_ENABLED, !swipeHorizontalEnabled).apply()
        Toast.makeText(context, "Horizontal swipe enabled", Toast.LENGTH_SHORT).show()
    }

    fun addPageBookmark(context2: Context, str: String?, i: Int) {
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context2)
        val editText = EditText(context2)
        editText.setHint(R.string.enter_title)
        val f = context2.resources.displayMetrics.density
        materialAlertDialogBuilder.setTitle(R.string.add_bookmark)
            .setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                /* synthetic */ var `f$1`: Context? = null
                /* synthetic */ var `f$2`: EditText? = null
                /* synthetic */ var `f$3`: String? = null
                /* synthetic */ var `f$4` = 0
                override fun onClick(dialogInterface: DialogInterface, i: Int) {
                    `lambda$addPageBookmark$5$PDFViewerActivity`(
                        `f$1`,
                        `f$2`,
                        `f$3`,
                        `f$4`,
                        dialogInterface,
                        i
                    )
                }

                init {
                    `f$1` = context2
                    `f$2` = editText
                    `f$3` = str
                    `f$4` = i
                }
            } as DialogInterface.OnClickListener)
            .setNegativeButton(R.string.cancel, null as DialogInterface.OnClickListener?)
        val create = materialAlertDialogBuilder.create()
        val i2 = (24.0f * f).toInt()
        create.setView(editText, i2, (8.0f * f).toInt(), i2, (f * 5.0f).toInt())
        create.show()
    }

    /* synthetic */   fun `lambda$addPageBookmark$5$PDFViewerActivity`(
        context2: Context?,
        editText: EditText?,
        str: String?,
        i: Int,
        dialogInterface: DialogInterface?,
        i2: Int
    ) {
        DbHelper.getInstance(context2).addBookmark(
            str,
            if (TextUtils.isEmpty(editText!!.text.toString())) getString(R.string.bookmark) else editText.text.toString(),
            i
        )
        Toast.makeText(
            context2,
            getString(R.string.page) + " " + i + " " + getString(R.string.bookmark_added),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun showContents(str: String?) {
    }

    fun jumpToPage() {
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context!!)
        materialAlertDialogBuilder.setTitle(R.string.jump_to_page)
            .setView(R.layout.dialog_jump_to_page).setPositiveButton(
                R.string.ok, null as DialogInterface.OnClickListener?
            ).setNegativeButton(R.string.cancel, null as DialogInterface.OnClickListener?)

        val aa: Drawable? = ContextCompat.getDrawable(context!!, R.drawable.popup_bg)
        aa!!.setTint(ContextCompat.getColor(context!!, R.color.background_color_day_night))
        materialAlertDialogBuilder.setBackground(aa)
        val create = materialAlertDialogBuilder.create()
        create.show()
        create.getButton(-1).setOnClickListener(object : View.OnClickListener {
            /* synthetic */ var `f$1`: TextInputEditText? = null
            /* synthetic */ var `f$2`: AlertDialog? = null
            override fun onClick(view: View) {
                `lambda$jumpToPage$6$PDFViewerActivity`(`f$1`, `f$2`, view)
            }

            init {
                `f$1` = create.findViewById(R.id.jump_to)
                `f$2` = create
            }
        })
    }

    /* synthetic */   fun `lambda$jumpToPage$6$PDFViewerActivity`(
        textInputEditText: TextInputEditText?,
        alertDialog: AlertDialog?,
        view2: View?
    ) {
        if (textInputEditText != null && textInputEditText.text != null) {
            val obj = textInputEditText.text.toString()
            if (isValidPageNumber(obj)) {
                alertDialog!!.dismiss()
                pdfView!!.jumpTo(obj.toInt() - 1, true)
                return
            }
            textInputEditText.error = getString(R.string.invalid_page_number)
        }
    }

    fun toggleNightMode(menuItem: MenuItem) {
        val z = sharedPreferences!!.getBoolean(SettingsFragment.NIGHT_MODE_ENABLED_PDFVIEW, false)
        nightModeEnabled = z
        setupNightModeIcons(menuItem, !z)
        pdfView!!.setNightMode(!nightModeEnabled)
        pdfView!!.invalidate()
        sharedPreferences!!.edit()
            .putBoolean(SettingsFragment.NIGHT_MODE_ENABLED_PDFVIEW, !nightModeEnabled).apply()
        setupToggleSwipeIcons(
            mMenu!!.findItem(R.id.action_toggle_view),
            sharedPreferences!!.getBoolean(SettingsFragment.SWIPE_HORIZONTAL_ENABLED, false)
        )
    }

    fun showPdfTools() {
    }

    fun setupToggleSwipeIcons(menuItem: MenuItem, z: Boolean) {
        if (z) {
            menuItem.setIcon(R.drawable.ic_action_swipe_vertical)
            menuItem.setTitle(R.string.swipe_vertical)
            return
        }
        menuItem.setIcon(R.drawable.ic_action_swipe_horizontal)
        menuItem.setTitle(R.string.swipe_horizontal)
    }

    fun setupNightModeIcons(menuItem: MenuItem, z: Boolean) {
        val resources = context!!.resources
        if (z) {
            val i = getResources().configuration.uiMode
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                menuItem.setIcon(R.drawable.ic_action_light_mode_night)
            } else {
                menuItem.setIcon(R.drawable.ic_action_light_mode)
            }
            menuItem.setTitle(R.string.light_mode)
            pdfView!!.setBackgroundColor(resources.getColor(R.color.colorPDFViewBgNight))
            return
        }
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            menuItem.setIcon(R.drawable.ic_action_night_mode_night)
        } else {
            menuItem.setIcon(R.drawable.ic_action_night_mode_light)
        }
        menuItem.setTitle(R.string.night_mode)
        pdfView!!.setBackgroundColor(context!!.resources.getColor(R.color.background_color_day_night))
    }

//    fun showEnterPasswordDialog() {
//        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context!!)
//        materialAlertDialogBuilder.setTitle(R.string.password_protected)
//            .setPositiveButton(R.string.ok, null as DialogInterface.OnClickListener?)
//            .setCancelable(false).setView(
//            R.layout.dialog_edit_password
//        ).setNegativeButton(
//            R.string.cancel,
//            DialogInterface.OnClickListener { dialogInterface, i ->
//                `lambda$showEnterPasswordDialog$7$PDFViewerActivity`(
//                    dialogInterface,
//                    i
//                )
//            })
//        val aa: Drawable? = ContextCompat.getDrawable(context!!, R.drawable.popup_bg)
//        aa!!.setTint(ContextCompat.getColor(context!!, R.color.background_color_day_night))
//        materialAlertDialogBuilder.setBackground(aa);
//        val create = materialAlertDialogBuilder.create()
//        create.show()
//        val textInputEditText = create.findViewById<View>(R.id.input_text) as TextInputEditText?
//        create.getButton(-1).setOnClickListener(View.OnClickListener {
//            val pDDocument: PDDocument
//            if (textInputEditText != null && textInputEditText.text != null) {
//                mPassword = textInputEditText.text.toString()
//                if (!TextUtils.isEmpty(mPassword)) {
//                    try {
//                        val str = TAG
//                        Log.d(str, "This is a path " + filePath)
//                        pDDocument = if (uri != null) {
//                            PDDocument.load(
//                                this@PDFViewerActivity.contentResolver.openInputStream(
//                                    uri!!
//                                ), mPassword
//                            )
//                        } else {
//                            PDDocument.load(File(filePath), mPassword)
//                        }
//                        pDDocument.close()
//                        loadPdfFile(
//                            mPassword,
//                            pageNumber,
//                            swipeHorizontalEnabled,
//                            nightModeEnabled,
//                            fitPolicy
//                        )
//                        create.dismiss()
//                    } catch (e: Exception) {
//                        if (e is InvalidPasswordException) {
//                            textInputEditText.error = context!!.getString(R.string.invalid_password)
//                            Log.d(TAG, "Invalid Password")
//                            return@OnClickListener
//                        }
//                        e.printStackTrace()
//                    }
//                } else {
//                    textInputEditText.error = context!!.getString(R.string.invalid_password)
//                    Log.d(TAG, "Invalid Password")
//                }
//            }
//        })
//    }

    fun showEnterPasswordDialog() {
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context!!)
        materialAlertDialogBuilder.setTitle(R.string.password_protected)
            .setPositiveButton(R.string.ok, null as DialogInterface.OnClickListener?)
            .setCancelable(false)
            .setView(R.layout.dialog_edit_password)
            .setNegativeButton(
                R.string.cancel
            ) { dialogInterface, i ->
                `lambda$showEnterPasswordDialog$7$PDFViewerActivity`(dialogInterface, i)
            }

        val create = materialAlertDialogBuilder.create()
        create.show()


        val adView = create.findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView?.loadAd(adRequest)




        // Set up password input handling as before
        val textInputEditText = create.findViewById<View>(R.id.input_text) as TextInputEditText?
        create.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val pDDocument: PDDocument
            if (textInputEditText != null && textInputEditText.text != null) {
                mPassword = textInputEditText.text.toString()
                if (!TextUtils.isEmpty(mPassword)) {
                    try {
                        pDDocument = if (uri != null) {
                            PDDocument.load(this@PDFViewerActivity.contentResolver.openInputStream(uri!!), mPassword)
                        } else {
                            PDDocument.load(File(filePath), mPassword)
                        }
                        pDDocument.close()
                        loadPdfFile(mPassword, pageNumber, swipeHorizontalEnabled, nightModeEnabled, fitPolicy)
                        create.dismiss()
                    } catch (e: Exception) {
                        if (e is InvalidPasswordException) {
                            textInputEditText.error = context!!.getString(R.string.invalid_password)
                            return@setOnClickListener
                        }
                        e.printStackTrace()
                    }
                } else {
                    textInputEditText.error = context!!.getString(R.string.invalid_password)
                }
            }
        }
    }

    /* synthetic */   fun `lambda$showEnterPasswordDialog$7$PDFViewerActivity`(
        dialogInterface: DialogInterface?,
        i: Int
    ) {
        finish()
    }

    fun isValidPageNumber(str: String?): Boolean {
        if (!TextUtils.isEmpty(str) && TextUtils.isDigitsOnly(str)) {
            val pageCount = pdfView!!.pageCount
            try {
                val intValue = Integer.valueOf(str).toInt()
                return !(intValue <= 0 || intValue > pageCount)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    private fun toggle() {
        if (mVisible) {
            `lambda$new$3$PDFViewerActivity`()
            return
        }
        show()
        if (AUTO_HIDE) {
            delayedHide(com.pdfreader.freecodedfapp.data.Constants.AUTO_HIDE_DELAY_MILLIS)
        }
    }

    fun `lambda$new$3$PDFViewerActivity`() {
        val supportActionBar = supportActionBar
        supportActionBar?.hide()
        mVisible = false
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, 1)
    }

    private fun show() {
        pdfView!!.systemUiVisibility = 1536
        mVisible = true
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, 1)
    }

    private fun delayedHide(i: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, i.toLong())
    }

    fun setShowRemoveAds() {
        if (showRemoveAds) {
            Snackbar.make(findViewById(R.id.pdf_container), R.string.dont_like_ads, 4000).setAction(
                R.string.remove,
                View.OnClickListener { view -> `lambda$setShowRemoveAds$8$PDFViewerActivity`(view) })
                .show()
        }
    }

    /* synthetic */   fun `lambda$setShowRemoveAds$8$PDFViewerActivity`(view2: View?) {
        Utils.showSubscriptionOptions(applicationContext)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun print() {
        val uri2 = uri
        if (uri2 != null) {
            Utils.print(this, uri2)
        } else {
            Utils.print(
                this, Uri.fromFile(
                    File(
                        filePath
                    )
                )
            )
        }
    }

    private fun share() {
        val uri =
            FileProvider.getUriForFile(this@PDFViewerActivity,
                BuildConfig.APPLICATION_ID + ".provider", File(filePath))
        val uris = ArrayList<Uri>()
        uris.add(uri)
        shareFile(uris)
    }

    private fun shareFile(uris: ArrayList<Uri>) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND_MULTIPLE
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "application/pdf"
        startActivity(Intent.createChooser(intent, "Select app to send message…"))
    }

    private fun shareAsPicture() {
        val uri2 = uri
        if (uri2 != null) {
            showShareAsPicture(uri2)
            return
        }
        try {
            showShareAsPicture(Uri.fromFile(File(filePath)))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, R.string.cant_share_file, Toast.LENGTH_LONG).show()
        }
    }

    fun setupFeatureDiscoverty() {
        val defaultSharedPreferences =
            android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        if (defaultSharedPreferences.getBoolean("prefs_show_tools_tutorial", true)) {
            TapTargetView.showFor(
                this as Activity,
                TapTarget.forToolbarMenuItem(
                    toolbar,
                    R.id.action_pdf_tools,
                    getString(R.string.pdf_tools) as CharSequence,
                    getString(R.string.show_tools_hint) as CharSequence
                ).titleTextColor(R.color.color54).tintTarget(false),
                object : TapTargetView.Listener() {
                    override fun onTargetCancel(tapTargetView: TapTargetView) {
                        super.onTargetCancel(tapTargetView)
                        defaultSharedPreferences.edit()
                            .putBoolean("prefs_show_tools_tutorial", false).apply()
                    }

                    override fun onTargetClick(tapTargetView: TapTargetView) {
                        super.onTargetClick(tapTargetView)
                        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                            /* synthetic */ var `f$1`: SharedPreferences? = null
                            override fun run() {
                                `lambda$onTargetClick$0$PDFViewerActivity$6`(`f$1`)
                            }

                            init {
                                `f$1` = defaultSharedPreferences
                            }
                        }, 200)
                    }

                    /* synthetic */   fun `lambda$onTargetClick$0$PDFViewerActivity$6`(
                        sharedPreferences: SharedPreferences?
                    ) {
                        showPdfTools()
                        sharedPreferences!!.edit().putBoolean("prefs_show_tools_tutorial", false)
                            .apply()
                    }
                } as TapTargetView.Listener)
        }
    }

    fun showLoadedAd(activity: Activity?) {
        val ad = AdManagerInterstitial.getAd()
        if (ad != null && !AdManagerInterstitial.adShowed) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    AdManagerInterstitial.adShowed = true
                    AdManagerInterstitial.createAd(activity)
                }
            }
            ad.show(activity!!)
        }
    }

    private fun showDeleteConfirmDialog(str: String?) {
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(context!!)
        materialAlertDialogBuilder.setTitle(R.string.permanently_delete_file)
            .setPositiveButton(R.string.delete) { dialogInterface, i ->
                `lambda$showDeleteConfirmDialog$10$PDFViewerActivity`(str, dialogInterface, i)
            }
            .setNegativeButton(R.string.cancel, null)
        materialAlertDialogBuilder.show()
    }

    private fun `lambda$showDeleteConfirmDialog$10$PDFViewerActivity`(
        str: String?,
        dialogInterface: DialogInterface?,
        i: Int
    ) {
        val file = File(str)
        if (file.delete()) {
            // Also delete the thumbnail file
            File(context!!.cacheDir.toString() + "/Thumbnails/" + Utils.removeExtension(file.name) + ".jpg").delete()

            // Update media scanner after deletion
            MediaScannerConnection.scanFile(
                context,
                arrayOf(str),
                null,
                object : MediaScannerConnection.OnScanCompletedListener {
                    override fun onScanCompleted(path: String, uri: Uri) {
                        EventBus.getDefault().post(PermanetlyDeleteEvent())
                        Log.d(TAG, "File deleted $path")

                        // Show toast after the file is deleted
                        Toast.makeText(context, "File deleted successfully", Toast.LENGTH_LONG).show()

                        // Introduce a small delay before closing the activity to ensure the toast is shown
                        Handler(Looper.getMainLooper()).postDelayed({
                            finish()  // Close activity after delay
                        }, 2000)
                    }
                }
            )
        } else {
            // If file deletion failed, show error message
            Toast.makeText(context, "Can't delete file", Toast.LENGTH_LONG).show()
        }
    }


//    fun showAdmobBannerAd() {
//        MobileAds.initialize(this)
//        var adView = AdView(this)
//        mAdView = adView
//        adView.adUnitId = getString(R.string.admob_banner_ad_id)
//        loadBanner()
//        mAdView!!.adListener = object : AdListener() {
//            override fun onAdClicked() {}
//            override fun onAdClosed() {}
//            override fun onAdOpened() {}
//            override fun onAdLoaded() {
//                Log.d(TAG, "Admob AD Loaded")
//            }
//
//            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                val str = TAG
//                Log.d(str, "Admob AD Filed to load. " + loadAdError.message)
//            }
//        }
//    }

//    private fun loadBanner() {
//        val build = AdRequest.Builder().build()
//      //  mAdView!!.adSize = adSize
//        mAdView!!.loadAd(build)
//    }

    private val adSize: AdSize
        private get() {
            val defaultDisplay = windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            defaultDisplay.getMetrics(displayMetrics)
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                this,
                (displayMetrics.widthPixels.toFloat() / displayMetrics.density).toInt()
            )
        }
}