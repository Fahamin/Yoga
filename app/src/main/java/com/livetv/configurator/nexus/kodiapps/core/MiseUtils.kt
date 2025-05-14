package com.livetv.configurator.nexus.kodiapps.core

import android.Manifest
import android.R
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Base64
import android.util.Log
import android.view.WindowManager.BadTokenException
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Date
import java.util.Locale


object MiseUtils {
    var dialog: AlertDialog? = null
    var progressDialog: ProgressDialog? = null
    const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: Int = 123

    fun clearCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            deleteDirectory(cacheDir)  // Clears app's cache directory
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun checkPermission(context: Context?): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (context as Activity?)!!,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    val alertBuilder = AlertDialog.Builder(context)
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle("Permission necessary")
                    alertBuilder.setMessage("External storage permission is necessary")
                    alertBuilder.setPositiveButton(
                        R.string.yes
                    ) { dialog, which ->
                        ActivityCompat.requestPermissions(
                            (context as Activity?)!!,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                        )
                    }
                    val alert = alertBuilder.create()
                    alert.show()
                } else {
                    ActivityCompat.requestPermissions(
                        (context as Activity?)!!,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                    )
                }
                return false
            } else {
                return true
            }
        } else {
            return true
        }
    }

    fun isOnline(context: Context): Boolean {
        val connectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info = connectivity.allNetworkInfo
            if (info != null) {
                for (i in info.indices) {
                    Log.w("INTERNET:", i.toString())
                    if (info[i].state == NetworkInfo.State.CONNECTED) {
                        Log.w("INTERNET:", "connected!")
                        return true
                    }
                }
            }
        }
        return false
    }

    // Helper function to recursively delete a directory
    private fun deleteDirectory(directory: File): Boolean {
        if (directory.isDirectory) {
            directory.listFiles()?.forEach {
                deleteDirectory(it)
            }
        }
        return directory.delete()
    }

    fun showLoader(context: Context?, msg: String?) {
        val activity = context as Activity?
        if (context != null) {
            try {
                if (!context.isFinishing) {
                    progressDialog = ProgressDialog(activity)
                    progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    //Without this user can hide loader by tapping outside screen
                    progressDialog!!.setCancelable(true)
                    progressDialog!!.setMessage(msg)
                    progressDialog!!.show()
                }
            } catch (e: BadTokenException) {
                throw RuntimeException(e)
            }

        }
    }

    fun cancelLoader() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog!!.dismiss()
        }

    }


    fun canceltempLoader() {
        Handler(Looper.getMainLooper()).postDelayed({ dialog!!.dismiss() }, 6000)
    }


    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        }
    }

    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height
        val width = if (aspectRatio > 1) maxWidth else (maxHeight * aspectRatio).toInt()
        val height = if (aspectRatio > 1) (maxWidth / aspectRatio).toInt() else maxHeight
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }


    fun getFileUri(context: Context, file: File?): Uri {
        return FileProvider.getUriForFile(
            context, context.packageName + ".provider",
            file!!
        )
    }

    private fun scanMediaFIle(mContext: Activity, file: File) {
        MediaScannerConnection.scanFile(
            mContext,
            arrayOf(file.absolutePath),
            null
        ) { path, uri -> }
    }

    fun generateOriginalFile(mContext: Activity, IMAGE_EXTENSION: String): File? {
        var file: File? = null
        try {
            val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Camera"
            )
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null
                }
            }
            val timeStamp: String = System.currentTimeMillis().toString()
            file =
                File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + IMAGE_EXTENSION)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        if (file != null) {
            scanMediaFIle(mContext, file)
        }

        return file
    }

    fun resizeAndEncodeImagePrescription(
        context: Context, // Add Context to access content resolver
        imageUri: Uri,    // Use Uri instead of File
        maxWidth: Int,
        maxHeight: Int,
        quality: Int = 100
    ): String {
        // Load the image into a Bitmap from the Uri
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Calculate the aspect ratio and resize dimensions
        val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        val (newWidth, newHeight) = if (originalBitmap.width > originalBitmap.height) {
            // Landscape
            maxWidth to (maxWidth / aspectRatio).toInt()
        } else {
            // Portrait
            (maxHeight * aspectRatio).toInt() to maxHeight
        }

        // Resize the Bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

        // Convert the resized Bitmap to Base64
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun resizeAndEncodeImage(
        file: File,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int = 100
    ): String {
        // Load the image into a Bitmap
        val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)

        // Calculate the aspect ratio and resize dimensions
        val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        val (newWidth, newHeight) = if (originalBitmap.width > originalBitmap.height) {
            // Landscape
            maxWidth to (maxWidth / aspectRatio).toInt()
        } else {
            // Portrait
            (maxHeight * aspectRatio).toInt() to maxHeight
        }

        // Resize the Bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

        // Convert the resized Bitmap to Base64
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    fun encodeImageToBase64(file: File, quality: Int = 100): String {
        // Load the image as a Bitmap
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        // Compress the Bitmap into a ByteArrayOutputStream with a quality factor
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        // Convert the ByteArrayOutputStream to a Base64 string
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)


    }

    fun encodeImageToBase64FromBitmap(
        originalBitmap: Bitmap, maxWidth: Int,
        maxHeight: Int, quality: Int = 100
    ): String {

        // Calculate the aspect ratio and resize dimensions
        val aspectRatio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
        val (newWidth, newHeight) = if (originalBitmap.width > originalBitmap.height) {
            // Landscape
            maxWidth to (maxWidth / aspectRatio).toInt()
        } else {
            // Portrait
            (maxHeight * aspectRatio).toInt() to maxHeight
        }

        // Resize the Bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

        // Convert the resized Bitmap to Base64
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)


    }

    // This method sets up and enqueues the WorkRequest

    fun getTimeStamp(): String {
        val tsLong = System.currentTimeMillis()
        val ts = tsLong.toString()
        val millisecond = ts.toLong()
        return DateFormat.format("dd-MM-yyyy hh:mm:ss a", Date(millisecond)).toString();
    }


    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    fun showSettingsAlert(context: Context) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)

        // Setting Dialog Title
        // Setting Dialog Message
        alertDialog.setMessage("Please enable location..")

        // On pressing Settings button
        alertDialog.setPositiveButton("ok") { dialog, which ->
            val intent = Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS
            )
            context.startActivity(intent)
        }
        alertDialog.show()
    }

    fun canGetLocation(context: Context): Boolean {
        val result = true
        val lm: LocationManager
        var gpsEnabled = false
        var networkEnabled = false
        lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // exceptions will be thrown if provider is not permitted.
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: java.lang.Exception) {
        }
        try {
            networkEnabled = lm
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: java.lang.Exception) {
        }
        return gpsEnabled && networkEnabled
    }

    fun makeDateString(currentMonth: Int, currentYear: Int): String {
        val lastTwoDigits = currentYear % 100
        var mnt = getMonthFormat(currentMonth)
        var date = "$mnt-$lastTwoDigits"
        return date
    }

    fun getMonthFormat(month: Int): String? {
        if (month == 1) return "JAN"
        if (month == 2) return "FEB"
        if (month == 3) return "MAR"
        if (month == 4) return "APR"
        if (month == 5) return "MAY"
        if (month == 6) return "JUN"
        if (month == 7) return "JUL"
        if (month == 8) return "AUG"
        if (month == 9) return "SEP"
        if (month == 10) return "OCT"
        if (month == 11) return "NOV"
        return if (month == 12) "DEC" else "JAN"

        //default should never happen
    }


    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            // Create a file in the cache directory
            val cacheDir = context.externalCacheDir
            val file = File(cacheDir, "${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output) // Save bitmap to file
            }
            Uri.fromFile(file) // Return the Uri of the saved image
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if saving fails
        }
    }


    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        var path: String? = null

        // Check if the URI is a content URI
        if (uri.scheme == "content") {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (it.moveToFirst()) {
                    path = it.getString(columnIndex)
                }
            }
        } else if (uri.scheme == "file") {
            // If the URI is a file URI
            path = uri.path
        }

        return path
    }

    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            // Get InputStream from URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            // Decode InputStream to Bitmap
            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            // Convert Bitmap to ByteArray
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            // Encode ByteArray to Base64
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun getCompleteAddressString(context: Context, latitude: Double, longitude: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(context!!, Locale.getDefault())
        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            val returnedAddress: Address = addresses!![0]
            val strReturnedAddress = StringBuilder("")
            for (i in 0..returnedAddress.maxAddressLineIndex) {
                strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
            }
            strAdd = strReturnedAddress.toString()
            Log.w("My Current", strReturnedAddress.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("loction address", "Canont get Address!")
        }
        return strAdd
    }

    fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            var i = 0
            while (i < children.size) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
                i++
            }
        }
        assert(dir != null)
        return dir!!.delete()
    }

    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = true

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    if ("primary".equals(type, true)) {
                        return "${Environment.getExternalStorageDirectory()}/${split[1]}"
                    }
                    // Handle non-primary volumes if needed
                }

                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    if (id.startsWith("raw:")) {
                        return id.removePrefix("raw:")
                    }
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        id.toLong()
                    )
                    return getDataColumn(context, contentUri, null, null)
                }

                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    val contentUri = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            }
        } else if ("content".equals(uri.scheme, true)) {
            // MediaStore and other file-based ContentProviders
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, true)) {
            // File URI
            return uri.path
        }

        return null
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

}

