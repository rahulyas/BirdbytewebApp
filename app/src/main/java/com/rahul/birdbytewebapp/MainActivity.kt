package com.rahul.birdbytewebapp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorMessageTextView: TextView
    // Load a URL into the WebView
    var url = "https://birdbyteweb.com"

    companion object {
        private const val REQUEST_CALL_PHONE_PERMISSION = 1
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        errorMessageTextView = findViewById(R.id.errorMessageTextView)

        // Check if the CALL_PHONE permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted, proceed with the dialing action
            dialPhoneNumber()
        } else {
            // Request the CALL_PHONE permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CALL_PHONE),
                REQUEST_CALL_PHONE_PERMISSION
            )
        }

        // Enable JavaScript (optional)
        val settings = webView.settings
        settings.javaScriptEnabled = true


        // Enable phone number linking
        settings.setSupportMultipleWindows(false)
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        // Handle page navigation within the WebView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // Check if the URL is a phone number
                view?.loadUrl(request?.url.toString())
                return true
            }
        }

        // Handle progress and alert dialogs (optional)
        webView.webChromeClient = object : WebChromeClient() {
            // Handle progress changes
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                updateProgressBar(newProgress)
            }
        }


        webView.loadUrl(url)
    }

    // Handle back button press (optional)
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun updateProgressBar(progress: Int) {
        progressBar.progress = progress

        if (progress == 100) {
            // Hide the progress bar when the page is fully loaded
            progressBar.visibility = View.GONE
        } else {
            // Show the progress bar while the page is loading
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun dialPhoneNumber() {
        if (url.startsWith("tel:")) {
            Log.i(TAG, "shouldOverrideUrlLoading: == ${Uri.parse(url).toString()} ")

            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
            // Check if there's an activity that can handle the intent before starting it
            if (dialIntent.resolveActivity(packageManager) != null) {
                startActivity(dialIntent)
            } else {
                // Handle the case where no activity can handle the dial intent
                println("No activity can handle the dial intent.")
            }
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CALL_PHONE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with the dialing action
                    dialPhoneNumber()
                } else {
                    // Permission denied, handle it as needed (e.g., show a message)
                    println("CALL_PHONE permission denied.")
                }
            }
        }
    }
}
