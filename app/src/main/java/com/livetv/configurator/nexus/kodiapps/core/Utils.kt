package com.livetv.configurator.nexus.kodiapps.core

import android.Manifest
import android.app.AlarmManager
import android.app.Dialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.location.LocationManager
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Environment
import android.os.SystemClock
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.db.DataHelper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.TreeMap
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.math.roundToInt

class Prefs(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun setPref(key: String?, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    fun setPref(key: String?, value: String?) {
        editor.putString(key, value)
        editor.apply()
    }

    fun setPref(key: String?, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getPref(key: String?, def: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, def)
    }

    fun getPref(key: String?, def: Int): Int {
        return sharedPreferences.getInt(key, def)
    }

    fun getPref(key: String?, def: String?): String? {
        return sharedPreferences.getString(key, def)
    }


    fun setPref(key: String?, value: Float) {
        editor.putFloat(key, value)
        editor.apply()
    }

    fun getPref(key: String?, def: Float): Float {
        return sharedPreferences.getFloat(key, def)
    }



    fun validate(target: String, pattern: String): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            val r = Pattern.compile(pattern)
            r.matcher(target).matches()
        }
    }



    fun getDeviceWidth(context: Context): Int {
        try {
            val metrics = context.resources.displayMetrics
            return metrics.widthPixels
        } catch (e: Exception) {
            sendExceptionReport(e)
        }

        return 480
    }

    fun getDeviceHeight(context: Context): Int {
        try {
            val metrics = context.resources.displayMetrics
            return metrics.heightPixels
        } catch (e: Exception) {
            sendExceptionReport(e)
        }

        return 800
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isInternetConnected(mContext: Context?): Boolean {
        var outcome = false

        try {
            if (mContext != null) {
                val cm = mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                val networkInfos = cm.allNetworkInfo

                for (tempNetworkInfo in networkInfos) {
                    if (tempNetworkInfo.isConnected) {
                        outcome = true
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return outcome
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
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





    fun hideKeyBoard(c: Context, v: View) {
        val imm = c
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }


    fun getBold(c: Context): Typeface? {
        try {
            return Typeface.createFromAsset(
                c.assets,
                "lato_black.ttf"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun getMedium(c: Context): Typeface? {
        try {
            return Typeface.createFromAsset(
                c.assets,
//                "roboto-medium.ttf"
                "lato_black.ttf"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun getNormal(c: Context): Typeface? {
        try {
            /*return Typeface.createFromAsset(
                c.assets,
                "roboto-regular.ttf"
            )*/

            return Typeface.createFromAsset(
                c.assets,
                "lato_regular_.ttf"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun passwordValidator(password: String): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        //        String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";
        val PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[!@#$&*])(?=.*[0-9])(?=.*[a-z]).{6,15}$"


        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)
        return matcher.matches()
    }

    fun formatNo(str: String): String? {
        val number = removeComma(nullSafe(str))
        return if (!StringUtils.isEmpty(number)) {

            //            if (!finalStr.startsWith("$"))
            //                finalStr = "$" + finalStr;
            formatToComma(number)
        } else number

    }

    fun `formatNo$`(str: String): String {
        val number = removeComma(nullSafe(str))
        if (!StringUtils.isEmpty(number)) {

            var finalStr = formatToComma(number)

            if (!finalStr!!.startsWith("$"))
                finalStr = "$$finalStr"
            return finalStr
        }

        return number

    }

    fun formatToComma(str: String): String? {
        var number: String? =
            removeComma(nullSafe(str))
        if (!StringUtils.isEmpty(number)) {

            var finalStr: String
            if (number!!.contains(".")) {
                number = truncateUptoTwoDecimal(number)
                val decimalFormat = DecimalFormat("#.##")
                finalStr = decimalFormat.format(BigDecimal(number!!))
            } else {
                finalStr = number
            }

            finalStr =
                NumberFormat.getNumberInstance(Locale.US).format(java.lang.Double.valueOf(finalStr))
            return finalStr
        }

        return number
    }

    fun truncateUptoTwoDecimal(doubleValue: String): String {
        if (doubleValue != null) {
            var result: String = doubleValue
            val decimalIndex = result.indexOf(".")
            if (decimalIndex != -1) {
                val decimalString = result.substring(decimalIndex + 1)
                if (decimalString.length > 2) {
                    result = doubleValue.substring(0, decimalIndex + 3)
                } else if (decimalString.length == 1) {
                    //                    result = String.format(Locale.ENGLISH, "%.2f",
                    //                            Double.parseDouble(value));
                }
            }
            return result
        }

        return doubleValue
    }

    fun removeComma(str: String): String {
        var str = str
        try {
            if (!StringUtils.isEmpty(str)) {
                str = str.replace(",".toRegex(), "")
                try {
                    val format = NumberFormat.getCurrencyInstance()
                    val number = format.parse(str)
                    return number.toString()
                } catch (e: ParseException) {
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return str
    }



    fun sendExceptionReport(e: Exception) {
        e.printStackTrace()

        try {
        } catch (e1: Exception) {
            e1.printStackTrace()
        }

    }


    fun parseTime(time: Long, pattern: String): String {
        val sdf = SimpleDateFormat(
            pattern,
            Locale.getDefault()
        )
        return sdf.format(Date(time))
    }

    fun parseTime(time: String, pattern: String): Date {
        val sdf = SimpleDateFormat(
            pattern,
            Locale.getDefault()
        )
        try {
            return sdf.parse(time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return Date()
    }

    fun parseTimeForDate(time: Long, pattern: String): Date {
        val sdf = SimpleDateFormat(
            pattern,
            Locale.getDefault()
        )

        try {
            return sdf.parse(sdf.format(Date(time)))

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return Date()
    }


    fun parseTime(
        time: String, fromPattern: String,
        toPattern: String
    ): String {
        var sdf = SimpleDateFormat(
            fromPattern,
            Locale.getDefault()
        )
        try {
            val d = sdf.parse(time)
            sdf = SimpleDateFormat(toPattern, Locale.getDefault())
            return sdf.format(d)
        } catch (e: Exception) {
        }

        return ""
    }

    fun parseTimeUTCtoDefault(time: String, pattern: String): Date {

        var sdf = SimpleDateFormat(
            pattern,
            Locale.getDefault()
        )
        try {
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val d = sdf.parse(time)
            sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.timeZone = Calendar.getInstance().timeZone
            return sdf.parse(sdf.format(d))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Date()
    }

    fun parseTimeUTCtoDefault(time: Long): Date {

        try {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.timeInMillis = time
            val d = cal.time
            val sdf = SimpleDateFormat()
            sdf.timeZone = Calendar.getInstance().timeZone
            return sdf.parse(sdf.format(d))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Date()
    }

    fun parseTimeUTCtoDefault(time: String, fromPattern: String, toPattern: String): String {
        var sdf = SimpleDateFormat(
            fromPattern,
            Locale.getDefault()
        )
        try {
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val d = sdf.parse(time)
            sdf = SimpleDateFormat(toPattern, Locale.getDefault())
            sdf.timeZone = Calendar.getInstance().timeZone
            return sdf.format(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    fun parseTimeUTCtoDefaultGerman(time: String, fromPattern: String, toPattern: String): String {
        var sdf = SimpleDateFormat(
            fromPattern,
            Locale.getDefault()
        )
        try {
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val d = sdf.parse(time)
            sdf = SimpleDateFormat(toPattern, Locale.GERMAN)
            sdf.timeZone = Calendar.getInstance().timeZone
            return sdf.format(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    fun parseTimeUTCtoDefaultEnglish(time: String, fromPattern: String, toPattern: String): String {

        var sdf = SimpleDateFormat(
            fromPattern,
            Locale.getDefault()
        )
        try {
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val d = sdf.parse(time)
            sdf = SimpleDateFormat(toPattern, Locale.ENGLISH)
            sdf.timeZone = Calendar.getInstance().timeZone
            return sdf.format(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    fun parseTimeUTCtoDefaultTurkey(time: String, fromPattern: String, toPattern: String): String {
        var sdf = SimpleDateFormat(
            fromPattern,
            Locale.getDefault()
        )
        try {
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val d = sdf.parse(time)
            sdf = SimpleDateFormat(toPattern, Locale("tr", "TR"))
            sdf.timeZone = Calendar.getInstance().timeZone
            return sdf.format(d)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun convertToTimeZone(
        date: String,
        frompattern: String,
        topattern: String,
        defaultzoneid: String,
        convertzoneid: String
    ): String {
        //Debug.e("TAG", "convertToTimeZone() called with: date = [" + date + "], frompattern = [" + frompattern + "], topattern = [" + topattern + "], defaultzoneid = [" + defaultzoneid + "], convertzoneid = [" + convertzoneid + "]");
        var returnDate = date
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(convertzoneid))
            //Debug.e("Convert time zone", TimeZone.getDefault().getID());
            val inputDate = SimpleDateFormat(frompattern).parse(date)
            //Debug.e("Input Time", inputDate.toString());
            TimeZone.setDefault(TimeZone.getTimeZone(defaultzoneid))
            //Debug.e("Output time zone", TimeZone.getDefault().getID());
            val dateFormatGmt = SimpleDateFormat(topattern)
            dateFormatGmt.timeZone = TimeZone.getTimeZone(defaultzoneid)
            val dateFormatDefautl = SimpleDateFormat(topattern)
            val date1 = dateFormatDefautl.parse(dateFormatGmt.format(inputDate))
            val dateFormat = SimpleDateFormat(topattern)
            returnDate = dateFormat.format(date1)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            return returnDate
        }
    }

    fun daysBetween(startDate: Date, endDate: Date): Long {
        val sDate = getDatePart(startDate)
        val eDate = getDatePart(endDate)
        var daysBetween: Long = 0
        while (sDate.before(eDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1)
            daysBetween++
        }
        return daysBetween
    }

    fun getFullDayName(day: Int): String {
        val c = Calendar.getInstance()
        // date doesn't matter - it has to be a Monday
        // I new that first August 2011 is one ;-)
        c.set(2011, 7, 1, 0, 0, 0)
        c.add(Calendar.DAY_OF_MONTH, day)
        return String.format("%tA", c)
    }

    fun getDatePart(date: Date): Calendar {
        val cal = Calendar.getInstance()       // get calendar instance
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)            // set hour to midnight
        cal.set(Calendar.MINUTE, 0)                 // set minute in hour
        cal.set(Calendar.SECOND, 0)                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0)            // set millisecond in second
        return cal                                  // return the date part
    }

    fun nullSafe(content: String?): String {
        if (content == null) {
            return ""
        }
        return if (content.equals("null", ignoreCase = true)) {
            ""
        } else content
    }

    fun nullSafe(content: String, defaultStr: String): String {
        if (StringUtils.isEmpty(content)) {
            return defaultStr
        }
        return if (content.equals("null", ignoreCase = true)) {
            defaultStr
        } else content

    }

    fun nullSafeDash(content: String): String {
        if (StringUtils.isEmpty(content)) {
            return "-"
        }
        return if (content.equals("null", ignoreCase = true)) {
            "-"
        } else content
    }

    fun nullSafe(content: Int, defaultStr: String): String {
        return if (content == 0) {
            defaultStr
        } else "" + content
    }


    fun isSDcardMounted(): Boolean {
        val state = Environment.getExternalStorageState()
        return state == Environment.MEDIA_MOUNTED && state != Environment.MEDIA_MOUNTED_READ_ONLY
    }

    fun isGPSProviderEnabled(context: Context): Boolean {
        val locationManager = context
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun isNetworkProviderEnabled(context: Context): Boolean {
        val locationManager = context
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager
            .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun isLocationProviderEnabled(context: Context): Boolean {
        return isGPSProviderEnabled(context) || isNetworkProviderEnabled(
            context
        )
    }


    fun asList(str: String): ArrayList<String?> {
        return ArrayList<String?>(
            Arrays.asList(
                *str
                    .split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            )
        )
    }

    fun implode(data: ArrayList<String>): String {
        try {
            var devices = ""
            for (iterable_element in data) {
                devices = "$devices,$iterable_element"
            }
            if (devices.length > 0 && devices.startsWith(",")) {
                devices = devices.substring(1)
            }
            return devices
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun getOutputMediaFile(context: Context): File? {
        try {
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.
            val mediaStorageDir: File
            if (isSDcardMounted()) {

                mediaStorageDir =
                    File(context.getExternalFilesDir(Constant.FOLDER_NAME)?.absolutePath)
            } else {
                mediaStorageDir = File(
                    Environment.getRootDirectory(),
                    Constant.FOLDER_NAME
                )
            }

            // This location works best if you want the created images to be
            // shared
            // between applications and persist after your app has been
            // uninstalled.
            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null
                }
            }
            // Create a media file name
            val mediaFile = File(
                mediaStorageDir.path
                        + File.separator + Date().time + ".jpg"
            )
            mediaFile.createNewFile()
            return mediaFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun scanMediaForFile(context: Context, filePath: String) {
        resetExternalStorageMedia(context, filePath)
        notifyMediaScannerService(context, filePath)
    }

    fun resetExternalStorageMedia(context: Context, filePath: String): Boolean {
        try {
            if (Environment.isExternalStorageEmulated())
                return false
            val uri = Uri.parse("file://" + File(filePath))
            val intent = Intent(Intent.ACTION_MEDIA_MOUNTED, uri)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun notifyMediaScannerService(context: Context, filePath: String) {
        MediaScannerConnection.scanFile(
            context, arrayOf(filePath),
            null
        ) { path, uri ->

        }
    }

    fun cancellAllNotication(context: Context) {
        val notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    fun toInitCap(param: String?): String? {
        try {
            if (param != null && param.length > 0) {
                val charArray = param.toCharArray() // convert into char
                // array
                charArray[0] = Character.toUpperCase(charArray[0]) // set
                // capital
                // letter to
                // first
                // position
                return String(charArray) // return desired output
            } else {
                return ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return param
    }

    fun encodeTobase64(image: Bitmap): String {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        val imageEncoded = Base64.encodeToString(b, Base64.DEFAULT)


        return imageEncoded
    }

    fun decodeBase64(input: String): Bitmap {
        val decodedByte = Base64.decode(input, 0)
        return BitmapFactory
            .decodeByteArray(decodedByte, 0, decodedByte.size)
    }

    fun getExtenstion(urlPath: String): String {
        if (urlPath.contains(".")) {
            val extension = urlPath.substring(urlPath.lastIndexOf(".") + 1)
            return urlPath.substring(urlPath.lastIndexOf(".") + 1)
        }
        return ""
    }

    fun getFileName(urlPath: String): String {
        return if (urlPath.contains(".")) {
            urlPath.substring(urlPath.lastIndexOf("/") + 1)
        } else ""
    }

    fun dpToPx(context: Context, `val`: Int): Float {
        val r = context.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            `val`.toFloat(),
            r.displayMetrics
        )
    }

    fun spToPx(context: Context, `val`: Int): Float {
        val r = context.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            `val`.toFloat(),
            r.displayMetrics
        )
    }

    fun getMimeType(url: String): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }

        return type
    }

    fun isJPEGorPNG(url: String): Boolean {
        try {
            val type = getMimeType(url)
            val ext = type!!.substring(type.lastIndexOf("/") + 1)
            if (ext.equals("jpeg", ignoreCase = true) || ext.equals(
                    "jpg",
                    ignoreCase = true
                ) || ext.equals(
                    "png",
                    ignoreCase = true
                )
            ) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
        return false
    }

    fun getFileSize(url: String): Double {
        val file = File(url)
        // Get length of file in bytes
        val fileSizeInBytes = file.length().toDouble()
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        val fileSizeInKB = fileSizeInBytes / 1024
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        val fileSizeInMB = fileSizeInKB / 1024


        return fileSizeInMB
    }

    fun getAsteriskName(str: String): String {
        var str = str
        val n = 4
        str = nullSafe(str)
        val fStr = StringBuilder()
        if (!TextUtils.isEmpty(str)) {
            if (str.length > n) {
                fStr.append(str.substring(0, n))
                for (i in 0 until str.length - n) {
                    fStr.append("*")
                }

                return fStr.toString()
            } else {
                fStr.append(str.substring(0, str.length - 1))
            }
        }
        return str
    }


    internal lateinit var dialog: Dialog

    fun twoDecimal(rate: String): String {
        if (!StringUtils.isEmpty(rate)) {
            val df = DecimalFormat("0.00", DecimalFormatSymbols(Locale.ENGLISH))
            return "CHF " + df.format(java.lang.Float.parseFloat(rate).toDouble())
        }
        return ""
    }

    fun twoDecimalWithoutChf(rate: String): String {
        if (!StringUtils.isEmpty(rate)) {
            val df = DecimalFormat("0.00", DecimalFormatSymbols(Locale.ENGLISH))
            return df.format(java.lang.Float.parseFloat(rate).toDouble())
        }
        return ""
    }




    fun isToday(timeInMillis: Long): Boolean {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = dateFormat.format(timeInMillis)
        return date == dateFormat.format(System.currentTimeMillis())
    }


    fun isYesterDay(time: Long): Boolean {
        if (time >= (parseTime_(Date(), "yyyy-MM-dd") - (TimeUnit.DAYS.toMillis(1)))) {
            return true;
        }
        return false;

    }

    fun compareDate(date1: Date, date2: Date): Boolean {
        var date1 = date1
        var date2 = date2

        date1 = parseTime(date1, "yyyy-MM-dd")

        date2 = parseTime(date2, "yyyy-MM-dd")


        return date1.compareTo(date2) == 0
    }


    fun parseTime(time: Date, pattern: String): Date {
        val sdf = SimpleDateFormat(
            pattern,
            Locale.getDefault()
        )
        try {
            return sdf.parse(sdf.format(time))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return time
    }

    fun parseTime_(date: Date, pattern: String): Long {
        var sdf = SimpleDateFormat(
            pattern,
            Locale.getDefault()
        );
        try {
            var sdf1 = SimpleDateFormat(
                pattern,
                Locale.getDefault()
            );
            var format = sdf.format(date);

            return sdf1.parse(sdf.format(date)).getTime();
        } catch (e: ParseException) {
            e.printStackTrace();
        }

        return 0L;
    }

    private val suffixes = TreeMap<Long, String>()

    init {
        suffixes[1_000L] = "K";
        suffixes[1_000_000L] = "M";
        suffixes[1_000_000_000L] = "G";
        suffixes[1_000_000_000_000L] = "T";
        suffixes[1_000_000_000_000_000L] = "P";
        suffixes[1_000_000_000_000_000_000L] = "E";
    }

    fun format(value: Long): String {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == java.lang.Long.MIN_VALUE) return format(java.lang.Long.MIN_VALUE + 1)
        if (value < 0) return "-" + format(-value)
        if (value < 1000) return java.lang.Long.toString(value) //deal with easy case

        val e = suffixes.floorEntry(value)
        val divideBy = e.key
        val suffix = e.value

        val truncated = value / (divideBy!! / 10) //the number part of the output times 10
        val hasDecimal = truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()
        return if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
    }

    fun getAssetItems(
        mContext: Context,
        categoryName: String
    ): java.util.ArrayList<String> {
        val arrayList = ArrayList<String>()
        val imgPath: Array<String>
        val assetManager = mContext.assets
        try {
            imgPath = assetManager.list(categoryName)!!
            if (imgPath != null) {
                for (anImgPath in imgPath) {
                    arrayList.add("///android_asset/$categoryName/$anImgPath")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return arrayList
    }

    fun ReplaceSpacialCharacters(string: String): String {
        return string.replace(" ", "").replace("&", "").replace("-", "").replace("'", "")
    }

    fun secToString(sec: Int, pattern: String): String {
        var formatter = DecimalFormat("00")
        var p1 = sec % 60
        var p2: Int = sec / 60
        val p3 = p2 % 60
        p2 /= 60

        if(p2 > 0)
        {
            return  formatter.format(p2)+":"+formatter.format(p3)+":"+formatter.format(p1)

        }
        return formatter.format(p3)+":"+formatter.format(p1)
    }

    fun DownloadTTSEngine(context: Context) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://search?q=text to speech&c=apps")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/search?q=text to speech&c=apps")
                )
            )
        }
    }

    fun getDrawableId(name: String?, context: Context): Int {
        return context.resources.getIdentifier(
            name,
            "drawable",
            context.packageName
        )
    }

    fun getCalorieFromSec(second: Long): Double {

        return second * Constant.SEC_DURATION_CAL

    }

    fun startAlarm(mCount: Int, hourOfDay: Int, minute: Int, context: Context) {

        try {
            val alarmStartTime = Calendar.getInstance()
            alarmStartTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            alarmStartTime.set(Calendar.MINUTE, minute)
            alarmStartTime.set(Calendar.SECOND, 0)

            val hourDt12 = parseTime(alarmStartTime.timeInMillis, "yyyy-MM-dd hh:mm")

            val format = SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault())
            val alarmDt = format.parse(hourDt12)

            val intent = Intent(context, AlarmBroadcastReceiver::class.java)
            intent.putExtra(Constant.extraReminderId, "" + mCount)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                mCount,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                alarmDt!!.time,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun setComBackIn30Min(context: Context){
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.putExtra(Constant.extraReminderId, "" + SystemClock.elapsedRealtime())
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            Date().time.toInt(),
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val  futureInMillis = SystemClock.elapsedRealtime () + 18000
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent)
    }

    fun getShortDayName(dayNo: String): String {
        var dayName = ""
        when (dayNo) {
            "1" -> dayName = "Sun"
            "2" -> dayName = "Mon"
            "3" -> dayName = "Tue"
            "4" -> dayName = "Wed"
            "5" -> dayName = "Thu"
            "6" -> dayName = "Fri"
            "7" -> dayName = "Sat"

        }
        return dayName
    }

    fun lbToKg(weightValue: Double): Double {
        return weightValue / 2.2046226218488
    }

    fun kgToLb(weightValue: Double): Double {
        return weightValue * 2.2046226218488
    }

    fun getafterPointValue(value:Float):String{
        val format = DecimalFormat("#.##")
        return format.format(value)
    }

    fun cmToInch(heightValue: Double): Double {

        return heightValue / 2.54
    }

    fun inchToCm(heightValue: Double): Double {

        return heightValue * 2.54
    }

    fun ftInToInch(ft: Int, `in`: Double): Double {
        return (ft * 12).toDouble() + `in`
    }

    fun calcInchToFeet(inch: Double): Int {
        return (inch / 12.0).toInt()
    }

    fun calcInFromInch(inch: Double): Double {
        return BigDecimal(inch % 12.0).setScale(1, 6).toDouble()
    }

    fun getMeter(inch: Double): Double {
        return inch * 0.0254
    }

    fun getBmiCalculation(kg: Float, foot: Int, inch: Int): Double {
        val bmi = kg / getMeter(ftInToInch(foot, inch.toDouble())).pow(2.0)
        return bmi
    }

    fun calculationForBmiGraph(point: Float): Float {
        /*
           Calculation formula
           pos = ((point - view starting point) * view_Weight) / (view ending - view starting point)
           pos += before view current value
           pos -= 0.08 (For center margin remove from calculation)
        * */
        var pos = 0f
        try {

            if (point < 15) {
                return 0f
            } else if (point > 15 && point <= 16) {
                //pos = (0.25f * point) / 15.5f

                pos = ((point - 15f) * 0.5f) / 1f
                pos -= 0.05f

            } else if (point > 16 && point <= 18.5) {

                pos = ((point - 16f) * 1.5f) / 2.5f
                pos += 0.5f
                pos -= 0.05f

//            pos = (0.75f * point) / 15.5f
//            pos += 0.5f

            } else if (point > 18.5 && point <= 25) {

                pos = ((point - 18.5f) * 2f) / 6.5f
                pos += 0.5f + 1.5f
                pos -= 0.05f

//            pos = (1.0f * point) / 15.5f
//            pos += 0.5f + 1.5f

            } else if (point > 25 && point <= 30) {

                pos = ((point - 25f) * 1f) / 5f
                pos += 0.5f + 1.5f + 2f
                pos -= 0.05f

//            pos = (0.50f * point) / 15.5f
//            pos += 0.5f + 1.5f + 2f

            } else if (point > 30 && point <= 35) {

                pos = ((point - 30f) * 1f) / 5f
                pos += 0.5f + 1.5f + 2f + 1f
                pos -= 0.05f

//            pos = (0.50f * point) / 15.5f
//            pos += 0.5f + 1.5f + 2f + 1f

            } else if (point > 35 && point <= 40) {

                pos = ((point - 35f) * 1f) / 5f
                pos += 0.5f + 1.5f + 2f + 1f + 1f
                pos -= 0.05f

//            pos = (0.50f * point) / 15.5f
//            pos += 0.5f + 1.5f + 2f + 1f + 1f

            } else if (point > 40) {
                return 6.90f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return pos
    }

    fun bmiWeightString(point: Float): String {

        try {
            if (point < 15) {
                return "Severely underweight"
            } else if (point > 15 && point < 16) {
                return "Very underweight"
            } else if (point > 16 && point < 18.5) {
                return "Underweight"
            } else if (point > 18.5 && point < 25) {
                return "Healthy Weight"
            } else if (point > 25 && point < 30) {
                return "Overweight"
            } else if (point > 30 && point < 35) {
                return "Moderately obese"
            } else if (point > 35 && point < 40) {
                return "Very obese"
            } else if (point > 40) {
                return "Severely obese"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    fun bmiWeightTextColor(context: Context, point: Float): Int {

        try {
            if (point < 15) {
                return ContextCompat.getColor(context, R.color.black)
            } else if (point > 15 && point < 16) {
                return ContextCompat.getColor(context, R.color.colorFirst)
            } else if (point > 16 && point < 18.5) {
                return ContextCompat.getColor(context, R.color.colorSecond)
            } else if (point > 18.5 && point < 25) {
                return ContextCompat.getColor(context, R.color.colorThird)
            } else if (point > 25 && point < 30) {
                return ContextCompat.getColor(context, R.color.colorFour)
            } else if (point > 30 && point < 35) {
                return ContextCompat.getColor(context, R.color.colorFive)
            } else if (point > 35 && point < 40) {
                return ContextCompat.getColor(context, R.color.colorSix)
            } else if (point > 40) {
                return ContextCompat.getColor(context, R.color.black)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ContextCompat.getColor(context, R.color.black)
    }

    fun getCurrentWeek(): java.util.ArrayList<String> {
        val currentWeekArrayList = ArrayList<String>()
//        currentWeekArrayList.add("Temp Data")
        try {
            val format = SimpleDateFormat(Constant.CapDateFormatDisplay, Locale.ENGLISH)
            val calendar = Calendar.getInstance(Locale.ENGLISH)
            calendar.firstDayOfWeek = Calendar.SUNDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            for (i in 1..7) {
                try {
                    val data = format.format(calendar.time)
                    currentWeekArrayList.add(data)
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return currentWeekArrayList
    }

    fun getCurrentWeekByFirstDay(context: Context): java.util.ArrayList<String> {
        val currentWeekArrayList = ArrayList<String>()
//        currentWeekArrayList.add("Temp Data")
        try {
            val format = SimpleDateFormat(Constant.CapDateFormatDisplay, Locale.ENGLISH)
            val calendar = Calendar.getInstance(Locale.ENGLISH)

            when (getFirstWeekDayNameByDayNo(getPref( Constant.PREF_FIRST_DAY_OF_WEEK, 1))) {
                "Sunday" -> {
                    calendar.firstDayOfWeek = Calendar.SUNDAY
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                }
                "Monday" -> {
                    calendar.firstDayOfWeek = Calendar.MONDAY
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                }
                "Saturday" -> {
                    calendar.firstDayOfWeek = Calendar.SATURDAY
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                }
            }

            for (i in 1..7) {
                try {
                    val data = format.format(calendar.time)
                    currentWeekArrayList.add(data)
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return currentWeekArrayList
    }

    fun getFirstWeekDayNameByDayNo(dayNo: Int): String {
        var dayNumber = ""
        when (dayNo) {
            1 -> dayNumber = "Sunday"
            2 -> dayNumber = "Monday"
            3 -> dayNumber = "Saturday"
        }
        return dayNumber
    }

    fun getFirstWeekDayNoByDayName(dayName: String): Int {
        var dayNumber = 1
        when (dayName) {
            "Sunday" -> dayNumber = 1
            "Monday" -> dayNumber = 2
            "Saturday" -> dayNumber = 3
        }
        return dayNumber
    }


    fun rateUs(context: Context) {
        val appPackageName = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    fun contactUs(content: Context) {
        try {
            val sendIntentGmail = Intent(Intent.ACTION_SEND)
            sendIntentGmail.type = "plain/text"
            sendIntentGmail.setPackage("com.google.android.gm")
            sendIntentGmail.putExtra(Intent.EXTRA_EMAIL, arrayOf("developerjd60@gmail.com"))
            sendIntentGmail.putExtra(
                Intent.EXTRA_SUBJECT,
                content.resources.getString(R.string.app_name)+" - Android"
            )
            content.startActivity(sendIntentGmail)
        } catch (e: Exception) {
            val sendIntentIfGmailFail = Intent(Intent.ACTION_SEND)
            sendIntentIfGmailFail.type = "*/*"
            sendIntentIfGmailFail.putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf("developerjd60@gmail.com")
            )
            sendIntentIfGmailFail.putExtra(
                Intent.EXTRA_SUBJECT,
                content.resources.getString(R.string.app_name)+" - Android"
            )
            if (sendIntentIfGmailFail.resolveActivity(content.packageManager) != null) {
                content.startActivity(sendIntentIfGmailFail)
            }
        }
    }

    fun openUrl(content: Context, strUrl: String) {
        val appPackageName = content.getPackageName() // getPackageName() from Context or Activity object
        try {
            content.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strUrl)))
        } catch (e: ActivityNotFoundException) {
            content.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strUrl)))
        }
    }

    // Todo Set Day Progress in text and progress bar
    fun setDayProgressData(
        content: Context,
        catId: String,
        txtDayLeft: TextView,
        txtDayPer: TextView,
        pbDay: ProgressBar
    ) {
        try {
            val dbHelper = DataHelper(content)

            // Week day progress data
            val compDay = dbHelper.getCompleteDayCountByPlanId(catId)

            //val proPercentage = String.format("%.1f", (((compDay).toFloat()) * 100) / 28)
            val proPercentage = String.format(
                "%.2f",
                (((((compDay).toFloat()) * 100) / 14).toDouble())
            ).replace(",", ".")
            txtDayLeft.text = (14 - compDay).toString().plus(" Days left")
            txtDayPer.text = proPercentage.toDouble().roundToInt().toString().plus("%")

            pbDay.progress = proPercentage.toFloat().toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareStringLink(content: Context, strSubject: String, strText: String) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, strText)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, strSubject)
        shareIntent.type = "text/plain"
        content.startActivity(
            Intent.createChooser(
                shareIntent,
                content.resources.getString(R.string.app_name)
            )
        )
    }
}
