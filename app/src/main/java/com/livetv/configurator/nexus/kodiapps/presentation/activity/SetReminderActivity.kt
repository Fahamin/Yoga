package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.livetv.configurator.nexus.kodiapps.presentation.activity.MainActivity
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.WhatsYourGoalAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Fun
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.databinding.ActivitySetReminderBinding
import com.livetv.configurator.nexus.kodiapps.db.DataHelper
import com.livetv.configurator.nexus.kodiapps.model.ReminderTableClass

class SetReminderActivity : AppCompatActivity() {

    var binding: ActivitySetReminderBinding? = null
    var whatsYourGoalAdapter: WhatsYourGoalAdapter? = null
    lateinit var utils: Prefs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_reminder)
        utils = Prefs(this)
         Fun(this)
        val adContainerView = findViewById<FrameLayout>(R.id.ad_view_container)
        Fun.showBanner(this, adContainerView)
        init()
    }


    private fun init() {

        /*binding!!.npHour.setFadingEdgeEnabled(true)
        binding!!.npMinute.setFadingEdgeEnabled(true)*/

// Set scroller enabled
        binding!!.npHour.setScrollerEnabled(true)
        binding!!.npMinute.setScrollerEnabled(true)

// Set wrap selector wheel
        binding!!.npHour.setWrapSelectorWheel(true)
        binding!!.npMinute.setWrapSelectorWheel(true)

        binding!!.npHour.setTypeface(
            Typeface.createFromAsset(
                assets,
                "roboto-black.ttf"
            ))
        binding!!.npMinute.setTypeface(Typeface.createFromAsset(
            assets,
            "roboto-black.ttf"
        ))

        binding!!.btnFinished.setOnClickListener {

            showDaySelectionDialog(binding!!.npHour.value,binding!!.npMinute.value)
        }

    }

    private fun showDaySelectionDialog(hourOfDay: Int, minute: Int) {

        val daysList = arrayOf<CharSequence>("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val booleanArray = booleanArrayOf(true,true,true,true,true,true,true)
        val arrayList = arrayListOf<String>("1", "2", "3", "4", "5", "6", "7")

        val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
        builder.setCancelable(false)
        builder.setTitle(R.string.repeat)
        builder.setMultiChoiceItems(daysList, booleanArray,object : DialogInterface.OnMultiChoiceClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int, isChecked: Boolean) {
                if(isChecked && arrayList.contains((which+1).toString()).not()){
                    arrayList.add((which+1).toString())
                }else{
                    arrayList.remove((which+1).toString())
                }
            }
        })

        builder.setPositiveButton(R.string.btn_ok) { dialog, which ->
            val reminderClass = ReminderTableClass()
            reminderClass.remindTime = String.format("%02d:%02d", hourOfDay, minute)

            reminderClass.days = arrayList.joinToString(",")
            reminderClass.isActive = "true"

            val mCount = DataHelper(this).addReminder(reminderClass)

            utils.startAlarm(mCount,hourOfDay,minute,this)
            dialog.dismiss()
            utils.setPref( Constant.PREF_IS_FIRST_TIME, false)
            utils.setPref( Constant.PREF_IS_REMINDER_SET,true)
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }
        builder.setNegativeButton(R.string.btn_cancel, { dialog, which -> dialog.dismiss() })
        builder.create().show()

    }



}
