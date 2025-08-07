package com.pdfreader.freecodedfapp.data

object Constants {

    const val SPLASH_SCREEN_TIMEOUT = "4000"
    const val URL_PRIVACY_POLICY = "https://example.com/"
    const val URL_APP_PLAY_STORE = "https://play.google.com/store/apps/details?id="

    const val ONESIGNAL_APP_ID = "########-####-####-####-############"
    const val PURCHASE_PRODUCT_ID = "android.test.purchased"
    const val APP_NAME = "SlimPDF"

    // MainActivity
    const val DAYS_FOR_FLEXIBLE_UPDATE = 14
    const val PDF_LOCATION = "com.example.slimpdfapp.PDF_LOCATION"
    const val SHOW_REMOVE_ADS = "com.example.slimpdfapp.SHOW_REMOVE_ADS"
    const val UPDATE_REQUEST_CODE = 4
    const val PICK_PDF_REQUEST = 1
    @JvmField
    var GRID_VIEW_ENABLED = "prefs_grid_view_enabled"
    @JvmField
    var GRID_VIEW_NUM_OF_COLUMNS = "prefs_grid_view_num_of_columns"
    var IS_SUBSCRIBED = true

    // PDFViewerActivity
    const val CONTENTS_PDF_PATH = "com.example.slimpdfapp.CONTENTS_PDF_PATH"
    const val PAGE_NUMBER = "com.example.slimpdfapp.PAGE_NUMBER"
    const val AUTO_HIDE_DELAY_MILLIS = 10000
    const val UI_ANIMATION_DELAY = 1

    // SearchActivity
    const val ARG_PARAM1 = "param1"
    const val ARG_PARAM2 = "param2"
    const val RC_READ_EXTERNAL_STORAGE = 1
    @JvmField
    var MORE_OPTIONS_TIP = "prefs_more_options_tip"

    // OrganizeImagesActivity & SelectImagesActivity
    const val IMAGE_URIS = "com.example.slimpdfapp.IMAGE_URIS"
    @JvmField
    var ORGANIZE_PAGES_TIP = "prefs_organize_pages"

}