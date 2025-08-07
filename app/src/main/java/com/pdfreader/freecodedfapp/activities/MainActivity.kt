package com.pdfreader.freecodedfapp.activities

import androidx.appcompat.app.AppCompatActivity
import com.pdfreader.freecodedfapp.fragments.RecentPdfFragment.OnRecentPdfClickListener
import com.pdfreader.freecodedfapp.adapters.DevicePdfsAdapter
import com.pdfreader.freecodedfapp.adapters.RecentPdfsAdapter.OnHistoryPdfClickListener
import com.pdfreader.freecodedfapp.adapters.StarredPDFAdapter.OnStaredPdfClickListener
import com.pdfreader.freecodedfapp.helper.MaterialSearchView
import com.pdfreader.freecodedfapp.ads.MyInterstitialAds.InterAdClickListner
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout
import androidx.drawerlayout.widget.DrawerLayout
import android.content.SharedPreferences
import android.view.SubMenu
import com.pdfreader.freecodedfapp.models.PdfModel
import com.pdfreader.freecodedfapp.ads.MyInterstitialAds
import android.os.Bundle
import com.pdfreader.freecodedfapp.R
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener
import com.pdfreader.freecodedfapp.helper.DataUpdatedEvent.PdfRenameEvent
import com.pdfreader.freecodedfapp.helper.DataUpdatedEvent.PermanetlyDeleteEvent
import android.content.Intent
import com.pdfreader.freecodedfapp.fragments.SettingsFragment
import com.pdfreader.freecodedfapp.utils.AdManagerInterstitial
import androidx.annotation.RequiresApi
import android.os.Build
import com.pdfreader.freecodedfapp.fragments.DevicePdfFragment
import com.pdfreader.freecodedfapp.fragments.RecentPdfFragment
import com.pdfreader.freecodedfapp.fragments.StarredPdfFragment
import android.os.AsyncTask
import com.google.android.play.core.appupdate.AppUpdateInfo
import android.annotation.SuppressLint
import androidx.appcompat.view.menu.MenuBuilder
import com.google.android.gms.ads.FullScreenContentCallback
import com.pdfreader.freecodedfapp.data.DbHelper
import com.pdfreader.freecodedfapp.utils.Utils.BackgroundGenerateThumbnails
import com.pdfreader.freecodedfapp.helper.DataUpdatedEvent.ToggleGridViewEvent
import com.pdfreader.freecodedfapp.helper.DataUpdatedEvent.SortListEvent
import android.text.TextUtils
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import android.app.Activity
import android.app.Dialog
import android.content.IntentSender.SendIntentException
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.pdfreader.freecodedfapp.BuildConfig
import com.pdfreader.freecodedfapp.MyApp
import com.pdfreader.freecodedfapp.data.Constants
import com.pdfreader.freecodedfapp.utils.LocaleUtils
import com.pdfreader.freecodedfapp.utils.Utils
import org.greenrobot.eventbus.EventBus
import java.io.File

class MainActivity : AppCompatActivity(), OnRecentPdfClickListener,
    DevicePdfsAdapter.OnPdfClickListener, OnHistoryPdfClickListener, OnStaredPdfClickListener,
    MaterialSearchView.OnQueryTextListener, InterAdClickListner {

    val TAG = MainActivity::class.java.simpleName
    var SHOW_AD_WHEN_LOADED = false

    // public BillingClient billingClient;/* access modifiers changed from: private */
    var appUpdateManager: AppUpdateManager? = null
    var coordinatorLayout: RelativeLayout? = null
    var currLanguage: String? = null
    var fab: FloatingActionButton? = null
    var navigationView: NavigationView? = null
    var onTabSelectedListener: OnTabSelectedListener = object : OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab) {}
        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabSelected(tab: TabLayout.Tab) {}
    }
    private val CLICKS_TILL_AD_SHOW = 4
    private var drawer: DrawerLayout? = null
    private var gridViewEnabled = false
    private var mMenu: Menu? = null
    var iv_home: ImageView? = null
    var iv_recent: ImageView? = null
    var iv_bookmark: ImageView? = null
    private var sharedPreferences: SharedPreferences? = null
    private var subMenu: SubMenu? = null
    private var isHistory = false
    private var pdfModel: PdfModel? = null
    private var myInterstitialAds: MyInterstitialAds? = null
    private var isPdfClick = false
    private var isStaredPdf = false
    var tv_home: TextView? = null
    var tv_recent: TextView? = null
    var tv_bookmark: TextView? = null
    var home: LinearLayout? = null
    var recent: LinearLayout? = null
    var starred: LinearLayout? = null

    private var abt_app_Dialog: Dialog? = null
    private var pdfClick = 0
    private var subMenu2: SubMenu? = null
    override fun onQueryTextSubmit(str: String): Boolean {
        return false
    }

    override fun onRecentPdfClick(uri: Uri) {}

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        try {
            // Show Toast to indicate start of onCreate
            //Toast.makeText(this, "OnCreate started", Toast.LENGTH_SHORT).show()

            // Set Night Mode
            if (MyApp.getInstance().isNightModeEnabled) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                //Toast.makeText(this, "Night Mode Enabled", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                //Toast.makeText(this, "Night Mode Disabled", Toast.LENGTH_SHORT).show()
            }

            // Set up Language
            LocaleUtils.setUpLanguage(this)
            //Toast.makeText(this, "Language Set Up", Toast.LENGTH_SHORT).show()

            setContentView(R.layout.activity_main)

            lightDarkMode()

            // Initialize Views
            iv_home = findViewById(R.id.iv_home)
            iv_recent = findViewById(R.id.iv_recent)
            iv_bookmark = findViewById(R.id.iv_book)
            tv_home = findViewById(R.id.tv_home)
            tv_recent = findViewById(R.id.tv_recent)
            tv_bookmark = findViewById(R.id.tv_book)
            home = findViewById(R.id.home)
            recent = findViewById(R.id.recent)
            starred = findViewById(R.id.starred)

            bottomBar()

            // Initialize Interstitial Ads
            myInterstitialAds = MyInterstitialAds(this, this)
            //Toast.makeText(this, "Interstitial Ads Initialized", Toast.LENGTH_SHORT).show()

            // Toolbar and Drawer Initialization
            val toolbar = findViewById<Toolbar>(R.id.toolbar_browse_pdf)
            drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
            // Removed Advance3DDrawerLayout-specific methods
            drawer!!.closeDrawer(GravityCompat.START)
            drawer!!.addDrawerListener(object : SimpleDrawerListener() {
            })
            initAppDrawer()
            //Toast.makeText(this, "initAppDrawer called", Toast.LENGTH_SHORT).show()
            // EventBus
            // Remove these lines that post sticky events during initialization
            // EventBus.getDefault().postSticky(PdfRenameEvent())
            // EventBus.getDefault().postSticky(PermanetlyDeleteEvent())

            // Floating Action Button
            fab = findViewById<FloatingActionButton>(R.id.fab)
            fab?.setOnClickListener {
                startActivity(Intent(this, SelectImagesActivity::class.java))
            }

            SHOW_AD_WHEN_LOADED = true
            setSupportActionBar(toolbar)

            // Preferences
            val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            sharedPreferences = defaultSharedPreferences

            // Check if sharedPreferences is not null before using
            currLanguage = sharedPreferences?.getString(SettingsFragment.KEY_PREFS_LANGUAGE, "en") ?: "en"
           // Toast.makeText(this, "Language Preference: $currLanguage", Toast.LENGTH_SHORT).show()

            // ActionBarDrawerToggle
            val actionBarDrawerToggle = ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            )
            drawer?.addDrawerListener(actionBarDrawerToggle)
            actionBarDrawerToggle.syncState()

            // Setup Navigation
            val navigationView = findViewById<NavigationView>(R.id.nav_view)
            navigationView?.let {
                setupTabs()
               // Toast.makeText(this, "Navigation Setup Complete", Toast.LENGTH_SHORT).show()
            }

            // Additional setups
            Utils.setUpRateUsDialog(this)
            checkForAppUpdate()
            AdManagerInterstitial.initialize(this)
            AdManagerInterstitial.createAd(this)
            dialogAboutApp()

        } catch (e: Exception) {
            // Show Toast with error message
            Toast.makeText(this, "Error in onCreate: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }






//    public override fun onCreate(bundle: Bundle?) {
//        super.onCreate(bundle)
////        Toast.makeText(this, "OnCreate -1 ", Toast.LENGTH_SHORT).show()
//        if (MyApp.getInstance().isNightModeEnabled) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        }
////        Toast.makeText(this, "OnCreate -2 ", Toast.LENGTH_SHORT).show()
//        LocaleUtils.setUpLanguage(this)
//        setContentView(R.layout.activity_main)
//        lightDarkMode()
//        iv_home = findViewById(R.id.iv_home)
//        iv_recent = findViewById(R.id.iv_recent)
//        iv_bookmark = findViewById(R.id.iv_book)
//        tv_home = findViewById(R.id.tv_home)
//        tv_recent = findViewById(R.id.tv_recent)
//        tv_bookmark = findViewById(R.id.tv_book)
//        home = findViewById(R.id.home)
//        recent = findViewById(R.id.recent)
//        starred = findViewById(R.id.starred)
//
//        Toast.makeText(this, "OnCreate -1 ", Toast.LENGTH_SHORT).show()
//        bottomBar()
//
//
//
//        myInterstitialAds = MyInterstitialAds(this, this)
//        Toast.makeText(this, "OnCreate -2 ", Toast.LENGTH_SHORT).show()
//        val toolbar = findViewById<View>(R.id.toolbar_browse_pdf) as Toolbar
//        drawer = findViewById<View>(R.id.drawer_layout) as Advance3DDrawerLayout
//        drawer!!.setViewScale(GravityCompat.START, 0.9f) //set height scale for main view (0f to 1f)
//        drawer!!.setViewElevation(
//            GravityCompat.START,
//            30f
//        ) //set main view elevation when drawer open (dimension)
//        drawer!!.setViewScrimColor(
//            GravityCompat.START,
//            ContextCompat.getColor(this, R.color.colorPrimary)
//        ) //set drawer overlay coloe (color)
//        drawer!!.drawerElevation = 30f //set drawer elevation (dimension)
//
//        drawer!!.setRadius(GravityCompat.START, 25f)
//        drawer!!.setViewRotation(GravityCompat.START, 0f)
//        drawer!!.closeDrawer(GravityCompat.START)
//        drawer!!.addDrawerListener(object : SimpleDrawerListener() {
//        })
//        initAppDrawer()
//        EventBus.getDefault().postSticky(PdfRenameEvent())
//        EventBus.getDefault().postSticky(PermanetlyDeleteEvent())
//        fab = findViewById<View>(R.id.fab) as FloatingActionButton
//        fab!!.setOnClickListener { view: View? ->
//            startActivity(
//                Intent(
//                    this,
//                    SelectImagesActivity::class.java
//                )
//            )
//        }
//        SHOW_AD_WHEN_LOADED = true
//        setSupportActionBar(toolbar)
//        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        sharedPreferences = defaultSharedPreferences
//        gridViewEnabled = defaultSharedPreferences.getBoolean(Constants.GRID_VIEW_ENABLED, false)
//        currLanguage = sharedPreferences!!.getString(SettingsFragment.KEY_PREFS_LANGUAGE, "en")
//        val actionBarDrawerToggle = ActionBarDrawerToggle(
//            this,
//            drawer,
//            toolbar,
//            R.string.navigation_drawer_open,
//            R.string.navigation_drawer_close
//        )
//        drawer!!.addDrawerListener(actionBarDrawerToggle)
//        actionBarDrawerToggle.syncState()
//        val navigationView2 = findViewById<View>(R.id.nav_view) as NavigationView
//        navigationView = navigationView2
//        setupTabs()
//        Utils.setUpRateUsDialog(this)
//
//        checkForAppUpdate()
//        AdManagerInterstitial.initialize(this)
//        AdManagerInterstitial.createAd(this)
//
//        dialogAboutApp()
//
//    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun bottomBar() {
        initBottomBarContent()

        iv_home?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.tint_color)
        tv_home?.setTextColor(ContextCompat.getColor(this, R.color.tint_color))

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.container, DevicePdfFragment())
            .commitAllowingStateLoss()  // Using commitAllowingStateLoss to avoid issues with state loss

        home?.setOnClickListener {
            Log.d(TAG, "BottomBar: Home button clicked")
            subMenu2?.item?.isVisible = true

            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, DevicePdfFragment())
                .commitAllowingStateLoss()

            initBottomBarContent()
            iv_home?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.tint_color)
            tv_home?.setTextColor(ContextCompat.getColor(this, R.color.tint_color))
        }

        recent?.setOnClickListener {
            Log.d(TAG, "BottomBar: Recent button clicked")
            subMenu2?.item?.isVisible = false

            initBottomBarContent()
            iv_recent?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.tint_color)
            tv_recent?.setTextColor(ContextCompat.getColor(this, R.color.tint_color))

            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, RecentPdfFragment())
                .commitAllowingStateLoss()
        }

        starred?.setOnClickListener {
            Log.d(TAG, "BottomBar: Starred button clicked")
            subMenu2?.item?.isVisible = false

            initBottomBarContent()
            iv_bookmark?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.tint_color)
            tv_bookmark?.setTextColor(ContextCompat.getColor(this, R.color.tint_color))

            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, StarredPdfFragment())
                .commitAllowingStateLoss()
        }
    }

    private fun initBottomBarContent() {
        iv_home?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray)
        iv_recent?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray)
        iv_bookmark?.backgroundTintList = ContextCompat.getColorStateList(this, R.color.gray)

        tv_home?.setTextColor(ContextCompat.getColor(this, R.color.gray))
        tv_recent?.setTextColor(ContextCompat.getColor(this, R.color.gray))
        tv_bookmark?.setTextColor(ContextCompat.getColor(this, R.color.gray))
    }




//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private fun bottomBar() {
//        initBottomBarContent()
//        iv_home!!.backgroundTintList = ContextCompat.getColorStateList(this, R.color.tint_color)
//        tv_home!!.setTextColor(ContextCompat.getColor(this, R.color.tint_color))
//        supportFragmentManager.beginTransaction().replace(R.id.container, DevicePdfFragment())
//            .commit()
//        home!!.setOnClickListener { v: View? ->
//            Log.d(TAG, "botombar: sssss")
//            subMenu2!!.item.isVisible = true
//            supportFragmentManager.beginTransaction().replace(R.id.container, DevicePdfFragment())
//                .commit()
//            initBottomBarContent()
//            iv_home!!.backgroundTintList = ContextCompat.getColorStateList(this, R.color.tint_color)
//            tv_home!!.setTextColor(ContextCompat.getColor(this, R.color.tint_color))
//        }
//        recent!!.setOnClickListener { v: View? ->
//            subMenu2!!.item.isVisible = false
//            initBottomBarContent()
//            iv_recent!!.backgroundTintList = ContextCompat.getColorStateList(this, R.color.tint_color)
//            tv_recent!!.setTextColor(ContextCompat.getColor(this, R.color.tint_color))
//            supportFragmentManager.beginTransaction().replace(R.id.container, RecentPdfFragment())
//                .commit()
//        }
//        starred!!.setOnClickListener { v: View? ->
//            subMenu2!!.item.isVisible = false
//            initBottomBarContent()
//            iv_bookmark!!.backgroundTintList = ContextCompat.getColorStateList(this, R.color.tint_color)
//            tv_bookmark!!.setTextColor(ContextCompat.getColor(this, R.color.tint_color))
//            supportFragmentManager.beginTransaction().replace(R.id.container,
//                StarredPdfFragment()
//            )
//                .commit()
//        }
//    }
//
//    private fun initBottomBarContent() {
//        iv_home!!.backgroundTintList =
//            ContextCompat.getColorStateList(this, R.color.gray)
//        iv_recent!!.backgroundTintList =
//            ContextCompat.getColorStateList(this, R.color.gray)
//        iv_bookmark!!.backgroundTintList =
//            ContextCompat.getColorStateList(this, R.color.gray)
//        tv_home!!.setTextColor(ContextCompat.getColor(this, R.color.gray))
//        tv_recent!!.setTextColor(ContextCompat.getColor(this, R.color.gray))
//        tv_bookmark!!.setTextColor(ContextCompat.getColor(this, R.color.gray))
//    }

    public override fun onResume() {
        super.onResume()
        //Toast.makeText(this, "OnResume -1 ", Toast.LENGTH_SHORT).show()
//        if (sharedPreferences == null) {
//            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        }

//        updateAppLanguage()
        AsyncTask.execute { `lambda$onResume$0$PDFListActivity`() }
        appUpdateManager!!.appUpdateInfo.addOnSuccessListener { obj ->
            `lambda$onResume$1$PDFListActivity`(
                obj as AppUpdateInfo
            )
        }
    }

    /* synthetic */ fun `lambda$onResume$0$PDFListActivity`() {
    }

    /* synthetic */ fun `lambda$onResume$1$PDFListActivity`(appUpdateInfo: AppUpdateInfo?) {
        if (appUpdateInfo!!.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
        }
    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {
            finishAffinity()
            super.onBackPressed()
        }
    }

    private fun initAppDrawer() {
        findViewById<View>(R.id.nav_about).setOnClickListener { v: View? ->
            drawer!!.closeDrawer(
                GravityCompat.START
            )

            abt_app_Dialog!!.show()
        }
        findViewById<View>(R.id.ll_rate_app).setOnClickListener { v: View? ->
            drawer!!.closeDrawer(
                GravityCompat.START
            )

            Utils.launchMarket(
                this
            )
        }
        findViewById<View>(R.id.nav_share).setOnClickListener { v: View? ->
            drawer!!.closeDrawer(
                GravityCompat.START
            )

            Utils.startShareActivity(
                this
            )
        }
        findViewById<View>(R.id.nav_inp).setOnClickListener { v: View? ->
            drawer!!.closeDrawer(
                GravityCompat.START
            )

            startActivity(
                Intent(
                    this,
                    PremiumActivity::class.java
                )
            )
        }
        findViewById<View>(R.id.nav_all_documents).setOnClickListener { v: View? ->
            drawer!!.closeDrawer(
                GravityCompat.START
            )
        }
        findViewById<View>(R.id.nav_privacy_policy).setOnClickListener { v: View? ->
            drawer!!.closeDrawer(
                GravityCompat.START
            )

            openUrl(Constants.URL_PRIVACY_POLICY)
        }
    }

    private fun openUrl(url: String) {
        if (url.isEmpty()) return
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun dialogAboutApp() {
        abt_app_Dialog = Dialog(this@MainActivity)
        abt_app_Dialog!!.setContentView(R.layout.popup_about_app_lay)
        abt_app_Dialog!!.getWindow()!!.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        val appVersion: TextView =
            abt_app_Dialog!!.findViewById(R.id.appVersion)

        val verName = BuildConfig.VERSION_NAME
        appVersion.text = verName
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenu = menu
        menuInflater.inflate(R.menu.main, menu)
        subMenu2 = mMenu!!.findItem(R.id.action_sort).subMenu
        subMenu = subMenu2
        subMenu2!!.clearHeader()
        MenuCompat.setGroupDividerEnabled(menu, true)

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        return true
    }

    public override fun onActivityResult(i: Int, i2: Int, intent: Intent?) {
        super.onActivityResult(i, i2, intent)
        if (i == 1 && i2 == -1) {
            val data = intent!!.data
            if (data != null) {
                var path = data.path
                if (path!!.contains(":")) {
                    path = path.split(":").toTypedArray()[1]
                }
                openFileInPdfView(File(path).absolutePath)
                return
            }
            Log.d(TAG, "Uri is null")
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_by_date_modified -> sortByDateModified()
            R.id.action_by_name -> sortByName()
            R.id.action_by_size -> sortBySize()
            R.id.action_clear_recent_pdfs -> clearRecent()
            R.id.action_order_by_ascending -> orderByAscending()
            R.id.action_order_by_descending -> orderByDescending()
            R.id.action_search ->  //this.searchView.openSearch();
                startActivity(Intent(this, SearchActivity::class.java))
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private var pdfClickCounter = 0
    // Add a separate counter variable for history clicks at the class level
    private var historyPdfClickCounter = 0
    // Add a separate counter variable for starred clicks at the class level
    private var staredPdfClickCounter = 0

    override fun onPdfClicked(pdfModel: PdfModel, i: Int) {
        isPdfClick = true
        this.pdfModel = pdfModel

        // Increment the click counter
        pdfClickCounter++

        // Show the ad every 5th click
        if (pdfClickCounter == 5) {
            Log.d(TAG, "onPdfClicked: Showing interstitial ad after 5 clicks")
            myInterstitialAds?.showAds()

            // Reset the counter after showing the ad
            pdfClickCounter = 0

            // Optionally, reload or recreate the ad if needed
            myInterstitialAds = MyInterstitialAds(this, this)
        } else {
            // Proceed without showing the ad
            onAdFail()
        }
    }

//    override fun onPdfClicked(pdfModel: PdfModel, i: Int) {
//        isPdfClick = true
//        this.pdfModel = pdfModel
//        if (i % 3 == 0) {
//            Log.d(TAG, "onPdfClicked: daaadda")
//            myInterstitialAds!!.showAds()
//            myInterstitialAds = MyInterstitialAds(this, this)
//        } else {
//            onAdFail()
//        }
//    }



    override fun onHistoryPdfClicked(pdfModel: PdfModel, i: Int) {
        this.pdfModel = pdfModel
        isHistory = true

        // Increment the history click counter
        historyPdfClickCounter++

        // Show the ad every 5th history click
        if (historyPdfClickCounter == 4) {
            Log.d(TAG, "onHistoryPdfClicked: Showing interstitial ad after 5 history clicks")
            myInterstitialAds?.showAds()

            // Reset the counter after showing the ad
            historyPdfClickCounter = 0

            // Optionally, reload or recreate the ad if needed
            myInterstitialAds = MyInterstitialAds(this, this)
        } else {
            // Proceed without showing the ad
            onAdFail()
        }
    }

//    override fun onHistoryPdfClicked(pdfModel: PdfModel, i: Int) {
//        this.pdfModel = pdfModel
//        isHistory = true
//        if (i % 3 == 0) {
//            Log.d(TAG, "onPdfClicked: daaadda")
//            myInterstitialAds!!.showAds()
//            myInterstitialAds = MyInterstitialAds(this, this)
//        } else {
//            onAdFail()
//        }
//    }


    override fun onStaredPdfClicked(pdfModel: PdfModel, i: Int) {
        this.pdfModel = pdfModel
        isStaredPdf = true

        // Increment the starred click counter
        staredPdfClickCounter++

        // Show the ad every 5th starred click
        if (staredPdfClickCounter == 5) {
            Log.d(TAG, "onStaredPdfClicked: Showing interstitial ad after 5 starred clicks")
            myInterstitialAds?.showAds()

            // Reset the counter after showing the ad
            staredPdfClickCounter = 0

            // Optionally, reload or recreate the ad if needed
            myInterstitialAds = MyInterstitialAds(this, this)
        } else {
            // Proceed without showing the ad
            onAdFail()
        }
    }

//    override fun onStaredPdfClicked(pdfModel: PdfModel, i: Int) {
//        this.pdfModel = pdfModel
//        isStaredPdf = true
//        if (i % 3 == 0) {
//            Log.d(TAG, "onPdfClicked: daaadda")
//            myInterstitialAds!!.showAds()
//            myInterstitialAds = MyInterstitialAds(this, this)
//        } else {
//            onAdFail()
//        }
//    }

    private fun openFileInPdfView(str: String) {
        pdfClick++
        val ad = AdManagerInterstitial.getAd()
        val intent = Intent(this, PDFViewerActivity::class.java)
        intent.putExtra(Constants.PDF_LOCATION, str)
        Log.d(TAG, "Pdf location $str")
        if (Constants.IS_SUBSCRIBED || ad == null || !SHOW_AD_WHEN_LOADED && pdfClick % CLICKS_TILL_AD_SHOW != 0) {
            startActivity(intent)
            return
        }
        ad.show(this)
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                SHOW_AD_WHEN_LOADED = false
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                AdManagerInterstitial.createAd(this@MainActivity)
                intent.putExtra(Constants.SHOW_REMOVE_ADS, true)
                this@MainActivity.startActivity(intent)
            }
        }
    }

    fun browsePDF(view: View?) {

    }

    private fun clearRecent() {
        DbHelper.getInstance(this).clearRecentPDFs()
        Toast.makeText(this, R.string.recent_cleared, Toast.LENGTH_SHORT).show()
    }

    fun showListView() {
        val edit = sharedPreferences!!.edit()
        edit.putBoolean(Constants.GRID_VIEW_ENABLED, false)
        edit.apply()
    }

    fun showGridView(i: Int) {
        BackgroundGenerateThumbnails(this).executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR,
            *arrayOfNulls(0)
        )
        val edit = sharedPreferences!!.edit()
        edit.putBoolean(Constants.GRID_VIEW_ENABLED, false)
        edit.putInt(Constants.GRID_VIEW_NUM_OF_COLUMNS, i)
        edit.apply()
        EventBus.getDefault().postSticky(ToggleGridViewEvent())
    }

    override fun onQueryTextChange(str: String): Boolean {
        Log.d(TAG, str)
        return true
    }

    private fun sortByName() {
        subMenu!!.findItem(R.id.action_by_name).isChecked = true
        val edit = sharedPreferences!!.edit()
        edit.putString(DbHelper.SORT_BY, "name")
        edit.apply()
        EventBus.getDefault().post(SortListEvent())
    }

    private fun sortByDateModified() {
        subMenu!!.findItem(R.id.action_by_date_modified).isChecked = true
        val edit = sharedPreferences!!.edit()
        edit.putString(DbHelper.SORT_BY, "date modified")
        edit.apply()
        EventBus.getDefault().post(SortListEvent())
    }

    private fun sortBySize() {
        subMenu!!.findItem(R.id.action_by_size).isChecked = true
        val edit = sharedPreferences!!.edit()
        edit.putString(DbHelper.SORT_BY, "size")
        edit.apply()
        EventBus.getDefault().post(SortListEvent())
    }

    private fun orderByAscending() {
        subMenu!!.findItem(R.id.action_order_by_ascending).isChecked = true
        val edit = sharedPreferences!!.edit()
        edit.putString(DbHelper.SORT_ORDER, "ascending")
        edit.apply()
        EventBus.getDefault().post(SortListEvent())
    }

    private fun orderByDescending() {
        subMenu!!.findItem(R.id.action_order_by_descending).isChecked = true
        val edit = sharedPreferences!!.edit()
        edit.putString(DbHelper.SORT_ORDER, "descending")
        edit.apply()
        EventBus.getDefault().post(SortListEvent())
    }


    private fun updateAppLanguage() {
        // Ensure sharedPreferences is not null
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        }

        val currentLanguage = sharedPreferences?.getString(SettingsFragment.KEY_PREFS_LANGUAGE, "en")

        // Check if currLanguage is different from the saved language
        if (!TextUtils.equals(currLanguage, currentLanguage)) {
            // Perform necessary language update
            recreate()  // Recreate the activity to apply the language change
        }
    }


//    private fun updateAppLanguage() {
//        if (!TextUtils.equals(
//                currLanguage,
//                sharedPreferences!!.getString(SettingsFragment.KEY_PREFS_LANGUAGE, "en")
//            )
//        ) {
//            recreate()
//        }
//    }

    private fun checkForAppUpdate() {
        val create = AppUpdateManagerFactory.create(this)
        appUpdateManager = create
        create.registerListener { installState ->
            `lambda$checkForAppUpdate$3$PDFListActivity`(
                installState as InstallState
            )
        }
        appUpdateManager!!.appUpdateInfo.addOnSuccessListener { obj ->
            `lambda$checkForAppUpdate$4$PDFListActivity`(
                obj as AppUpdateInfo
            )
        }
    }

    /* synthetic */ fun `lambda$checkForAppUpdate$3$PDFListActivity`(installState: InstallState) {
        val installStatus = installState.installStatus()
        if (installStatus == 2) {
            installState.installStatus()
            installState.installErrorCode()
        } else if (installStatus == 11) {
            popupSnackbarForCompleteUpdate()
        }
    }

    /* synthetic */ fun `lambda$checkForAppUpdate$4$PDFListActivity`(appUpdateInfo: AppUpdateInfo?) {
        if (appUpdateInfo!!.updateAvailability() == com.google.android.play.core.install.model.UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            try {
                appUpdateManager!!.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this as Activity, 4)
            } catch (e: SendIntentException) {
                e.printStackTrace()
            }
        }
    }

    private fun popupSnackbarForCompleteUpdate() {
        val make = Snackbar.make(findViewById(R.id.drawer_layout), R.string.update_downloaded, BaseTransientBottomBar.LENGTH_INDEFINITE)
        make.setAction(
            R.string.restart,
            View.OnClickListener { view ->
                `lambda$popupSnackbarForCompleteUpdate$5$PDFListActivity`(view)
            })
        make.setActionTextColor(resources.getColor(R.color.colorAccent))
        make.show()
    }

    /* synthetic */ fun `lambda$popupSnackbarForCompleteUpdate$5$PDFListActivity`(view: View?) {
        appUpdateManager!!.completeUpdate()
    }

    private fun setupTabs() {
        if (gridViewEnabled) {
            BackgroundGenerateThumbnails(this).executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                *arrayOfNulls(0)
            )
        }
    }

    private fun lightDarkMode() {
        val switchCompat: SwitchCompat = findViewById(R.id.switchCompat)
        val txtNightMode: TextView = findViewById(R.id.txtNightMode)
        val lightDarkModeImg: ImageView = findViewById(R.id.lightDarkModeImg)
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) switchCompat.setChecked(
            true
        )
        switchCompat.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    MyApp.getInstance().setIsNightModeEnabled(true)
                    val intent = intent
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

                    drawer!!.closeDrawer(
                        GravityCompat.START
                    )
                    finish()
                    startActivity(intent)
                } else {
                    MyApp.getInstance().setIsNightModeEnabled(false)
                    val intent = intent
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

                    drawer!!.closeDrawer(
                        GravityCompat.START
                    )
                    finish()
                    startActivity(intent)
                }
            }
        })
    }

    override fun onAdClosed() {
        if (isHistory) {
            isHistory = false
            openFileInPdfView(pdfModel!!.absolutePath)
        } else if (isPdfClick) {
            isPdfClick = false
            openFileInPdfView(pdfModel!!.absolutePath)
        } else if (isStaredPdf) {
            isStaredPdf = false
            openFileInPdfView(pdfModel!!.absolutePath)
        }
    }

    override fun onAdFail() {
        if (isHistory) {
            isHistory = false
            openFileInPdfView(pdfModel!!.absolutePath)
        } else if (isPdfClick) {
            isPdfClick = false
            openFileInPdfView(pdfModel!!.absolutePath)
        } else if (isStaredPdf) {
            isStaredPdf = false
            openFileInPdfView(pdfModel!!.absolutePath)
        }
    }
}