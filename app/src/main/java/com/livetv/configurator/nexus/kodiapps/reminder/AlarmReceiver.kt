package com.livetv.configurator.nexus.kodiapps.reminder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.presentation.activity.MainActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    var mAlarmManager: AlarmManager? = null
    var mPendingIntent: PendingIntent? = null
    lateinit var dataBaseHelper: ReminderDatabase
    lateinit var reminderClass: Reminder

    override fun onReceive(context: Context, intent: Intent) {
        dataBaseHelper = ReminderDatabase(context)
        val id = intent.getStringExtra(Constant.EXTRA_REMINDER_ID)
        Log.e("TAG", "onReceive:Start ServiceL:::: $id")
        try {
            reminderClass = dataBaseHelper.getReminder(id!!.toInt())!!
            if (reminderClass.active == "true") {

                var arrOfDays = ArrayList<String>()
                if (reminderClass.days!!.contains(",")) {
                    arrOfDays = (reminderClass.days!!.split(",")) as ArrayList<String>
                } else {
                    arrOfDays.add(reminderClass.days!!)
                }

                for (i in 0 until arrOfDays.size) {
                    arrOfDays[i] = arrOfDays[i].replace("'", "")
                    Log.e("TAG", "onReceive:Arraydays:::::  " + arrOfDays[i])
                }

                val dayNumber = getDayNumber(getCurrentDayName().uppercase(Locale.getDefault()))
                Log.e("TAG", "onReceive::::Day Number::::: $dayNumber")
                if (arrOfDays.contains(dayNumber)) {
                    fireNotification(context, reminderClass)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun setAlarm(context: Context, calendar: Calendar, ID: Int) {
        mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        intent.putExtra(Constant.EXTRA_REMINDER_ID, ID.toString())

        // Fixed: Use FLAG_IMMUTABLE unless you specifically need mutability
        mPendingIntent = PendingIntent.getBroadcast(
            context,
            ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val c = Calendar.getInstance()
        val currentTime = c.timeInMillis
        val diffTime = calendar.timeInMillis - currentTime
        mAlarmManager!!.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, mPendingIntent!!)

        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun setRepeatAlarm(context: Context, calendar: Calendar, ID: Int, RepeatTime: Long) {
        mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent_id = System.currentTimeMillis().toInt()
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

        intent.putExtra(Constant.EXTRA_REMINDER_ID, Integer.toString(ID))
        mPendingIntent = PendingIntent.getBroadcast(context, intent_id, intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val c = Calendar.getInstance()
        val currentTime = c.timeInMillis
        val diffTime = calendar.timeInMillis - currentTime

        mAlarmManager!!.setRepeating(
            AlarmManager.RTC_WAKEUP,
            SystemClock.elapsedRealtime() + diffTime,
            RepeatTime, mPendingIntent!!
        )
        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun cancelAlarm(context: Context, ID: Int) {
        mAlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        mPendingIntent = PendingIntent.getBroadcast(context, ID, Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE)
        mAlarmManager!!.cancel(mPendingIntent!!)

        val receiver = ComponentName(context, BootReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }


    private fun fireNotification(context: Context, reminderClass: Reminder) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = reminderClass.iD
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channelName = context.resources.getString(R.string.app_name)
        val channelDescription = "Application_name Alert"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(channelId.toString(), channelName, importance)
            mChannel.description = channelDescription
            mChannel.enableVibration(true)
            notificationManager.createNotificationChannel(mChannel)
        }

        val builder = NotificationCompat.Builder(context, channelId.toString())

        builder.setSmallIcon(R.drawable.ic_alert_sound)
        builder.color = ContextCompat.getColor(context,R.color.colorAccent)

        builder.setStyle(NotificationCompat.BigTextStyle().bigText("Your body needs energy! You haven't exercised in ${getCurrentFullDayName()}!"))
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
        builder.setContentTitle(context.resources.getString(R.string.app_name))
        builder.setAutoCancel(true)
        builder.setOngoing(false)

        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val intent = PendingIntent.getActivity(context, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(intent)

        notificationManager.notify(reminderClass.iD.toInt(), builder.build())

    }

    @SuppressLint("SimpleDateFormat")
    private fun getDate(date: String): Date {
        val simpleDateFormat = SimpleDateFormat("dd MMM, yyyy")
        val resultDate = simpleDateFormat.parse(date);
        return resultDate
    }


    private fun getCurrentFullDayName(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(cal.time)
    }

    private fun getCurrentDayName(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        return sdf.format(cal.time)
    }

    private fun getCurrentDate(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        return sdf.format(cal.time)
    }

    private fun getEndDate(date: Date): String {
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    private fun getDayNumber(dayName: String): String {
        var dayNumber = ""
        when (dayName) {
            "MON" -> dayNumber = "1"
            "TUE" -> dayNumber = "2"
            "WED" -> dayNumber = "3"
            "THU" -> dayNumber = "4"
            "FRI" -> dayNumber = "5"
            "SAT" -> dayNumber = "6"
            "SUN" -> dayNumber = "7"
        }
        return dayNumber
    }
}