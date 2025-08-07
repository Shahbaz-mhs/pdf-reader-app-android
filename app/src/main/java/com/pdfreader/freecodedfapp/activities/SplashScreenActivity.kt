package com.pdfreader.freecodedfapp.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.airbnb.lottie.LottieAnimationView
import com.pdfreader.freecodedfapp.MyApp
import com.pdfreader.freecodedfapp.R
import com.pdfreader.freecodedfapp.data.Constants
import com.pdfreader.freecodedfapp.utils.Utils

class SplashScreenActivity : AppCompatActivity() {

    private var TAG: String = "SplashScreenActivity"
    private var context: Context? = null

    private var appIconSplash: ImageView? = null
    private var splashLoader: LottieAnimationView? = null
    private var appNameSplash: TextView? = null

    private var alertDialogBuilder: AlertDialog.Builder? = null
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (MyApp.getInstance().isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        setContentView(R.layout.activity_splash_screen)

        appIconSplash = findViewById(R.id.appIconSplash)
        splashLoader = findViewById(R.id.splashLoader)
        appNameSplash = findViewById(R.id.appNameSplash)

        appNameSplash!!.alpha = 0f
        appNameSplash!!.animate()
            .translationY(appNameSplash!!.height.toFloat())
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(1000)
            .translationY(appNameSplash!!.height.toFloat())
            .alpha(1f)
            .setDuration(1200).startDelay = 1500

        appIconSplash!!.alpha = 0f
        appIconSplash!!.animate()
            .translationY(appIconSplash!!.height.toFloat())
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(1000)
            .translationY(appIconSplash!!.height.toFloat())
            .alpha(1f)
            .setDuration(1200).startDelay = 1500


        splashLoader!!.alpha = 0f
        splashLoader!!.animate()
            .translationY(splashLoader!!.height.toFloat())
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(1000)
            .translationY(splashLoader!!.height.toFloat())
            .alpha(1f)
            .setDuration(1200).startDelay = 1500

    }


//    override fun onResume() {
//        super.onResume()
//
//        if (!Utils.isPermissionGranted(this)) {
//            val alertDialogBuilder: AlertDialog.Builder
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                alertDialogBuilder = AlertDialog.Builder(this)
//                    .setTitle("All Files Permission Required")
//                    .setMessage("Due to Android 11 restrictions, this app requires permission to access all files.")
//                    .setPositiveButton("Allow") { dialogInterface, i -> takePermission() }
//                    .setNegativeButton("Deny") { dialogInterface, i ->
//                        Toast.makeText(applicationContext, "${getString(R.string.app_name)} cannot function without this permission.", Toast.LENGTH_LONG).show()
//                        exitApp()
//                    }
//                    .setIcon(R.drawable.pdf)
//            } else {
//                alertDialogBuilder = AlertDialog.Builder(this)
//                    .setTitle("Storage Permission Required")
//                    .setMessage("Please allow storage permission.")
//                    .setPositiveButton("Allow") { dialogInterface, i -> takePermission() }
//                    .setNegativeButton("Deny") { dialogInterface, i ->
//                        Toast.makeText(applicationContext, "${getString(R.string.app_name)} cannot function without the permission.", Toast.LENGTH_LONG).show()
//                        exitApp()
//                    }
//                    .setIcon(R.drawable.pdf)
//            }
//
//            val alertDialog = alertDialogBuilder.create()
//
//            Handler().postDelayed({
//                alertDialog.show()
//            }, 4000)
//
//        } else {
//            Log.d(TAG, "Permission Granted")
//            goToMainScreen()
//        }
//    }





    override fun onResume() {
        super.onResume()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!isManageStoragePermissionGranted()) {
                showManageStoragePermissionDialog()
            } else {
                Log.d(TAG, "All Files Access Permission Granted")
                goToMainScreen()
            }
        } else {
            if (!Utils.isPermissionGranted(this)) {
                showStoragePermissionDialog()
            } else {
                Log.d(TAG, "Storage Permission Granted")
                goToMainScreen()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun isManageStoragePermissionGranted(): Boolean {
        return Environment.isExternalStorageManager()
    }

    private fun showManageStoragePermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("All Files Permission Required")
            .setMessage("Due to Android 11 restrictions, this app requires permission to access all files.")
            .setPositiveButton("Allow") { _, _ -> requestManageStoragePermission() }
            .setNegativeButton("Deny") { _, _ ->
                Toast.makeText(applicationContext, "${getString(R.string.app_name)} cannot function without this permission.", Toast.LENGTH_LONG).show()
                exitApp()
            }
            .setIcon(R.drawable.pdf)
            .show()
    }

    private fun requestManageStoragePermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, 101)
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivityForResult(intent, 101)
        }
    }

    private fun showStoragePermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission Required")
            .setMessage("Please allow storage permission.")
            .setPositiveButton("Allow") { _, _ -> requestStoragePermissions() }
            .setNegativeButton("Deny") { _, _ ->
                Toast.makeText(applicationContext, "${getString(R.string.app_name)} cannot function without the permission.", Toast.LENGTH_LONG).show()
                exitApp()
            }
            .setIcon(R.drawable.pdf)
            .show()
    }

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 101
        )
    }

    // Add this method to handle permission request results:
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) { // Check for your permission request code
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission(s) granted, proceed with file access
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (isManageStoragePermissionGranted()) {
                        Log.d(TAG, "All Files Access Permission Granted")
                        goToMainScreen()
                    } else {
                        // User might have denied permission in settings, handle accordingly
                        Toast.makeText(this, "All Files Access Permission Required", Toast.LENGTH_SHORT).show()
                        // You can provide options to retry or explain the need for permission
                    }
                } else {
                    Log.d(TAG, "Storage Permission Granted")
                    goToMainScreen()
                }
            } else {
                // Permission(s) denied, handle accordingly
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                // You can provide options to retry or explain the need for permission
            }
        }
    }




//    override fun onResume() {
//        super.onResume()
//
//        if (!Utils.isPermissionGranted(this)) {
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                alertDialogBuilder = AlertDialog.Builder(this)
//                    .setTitle("All files permission")
//                    .setMessage("Due to Android 11 restrictions, this app requires all files permission")
//                    .setPositiveButton("Allow") { dialogInterface, i -> takePermission() }
//                    .setNegativeButton("Deny") { dialogInterface, i ->
//                        Toast.makeText(applicationContext, getString(R.string.app_name) + " cannot function without the permission", Toast.LENGTH_LONG).show()
//                        exitApp()
//                    }
//                    .setIcon(R.drawable.pdf)
//
//                alertDialog = alertDialogBuilder!!.create()
//
//            } else {
//                alertDialogBuilder = AlertDialog.Builder(this)
//                    .setTitle("All files permission")
//                    .setMessage("Please allow storage permission")
//                    .setPositiveButton("Allow") { dialogInterface, i -> takePermission() }
//                    .setNegativeButton("Deny") { dialogInterface, i ->
//                        Toast.makeText(applicationContext, getString(R.string.app_name) + " cannot function without the permission", Toast.LENGTH_LONG).show()
//                        exitApp()
//                    }
//                    .setIcon(R.drawable.pdf)
//
//                alertDialog = alertDialogBuilder!!.create()
//
//            }
//
//            Handler().postDelayed(object: Runnable {
//                override fun run() {
//                    alertDialog!!.show()
//                }
//            }, 4000)
//
//        } else {
//            //Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_LONG).show()
//            Log.d(TAG, "Permission Granted")
//            goToMainScreen()
//        }
//    }
//
//    private fun takePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            try {
//
//                ActivityCompat.requestPermissions(
//                    this, arrayOf(
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    ), 101
//                )
//
//                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
//                intent.addCategory("android.intent.category.DEFAULT")
//                val uri = Uri.fromParts("package", packageName, null)
//                intent.data = uri
//                startActivityForResult(intent, 101)
//            } catch (e: Exception) {
//                e.printStackTrace()
//
//                ActivityCompat.requestPermissions(
//                    this, arrayOf(
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    ), 101
//                )
//
//                val intent = Intent()
//                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
//                startActivityForResult(intent, 101)
//            }
//        } else {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ), 101
//            )
//        }
//    }



//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (grantResults.size > 0) {
//            if (requestCode == 101) {
//                val readExt = grantResults[0] == PackageManager.PERMISSION_GRANTED
//                if (!readExt) {
//                  //  takePermission()
//                }
//            }
//        }
//    }

    private fun goToMainScreen() {

//        Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()

        Handler().postDelayed(object: Runnable {
            override fun run() {
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            }
        }, Constants.SPLASH_SCREEN_TIMEOUT.toLong())

    }

    private fun exitApp() {
        finish()
    }

}