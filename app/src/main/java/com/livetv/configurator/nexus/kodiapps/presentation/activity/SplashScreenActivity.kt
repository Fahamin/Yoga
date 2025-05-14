package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.livetv.configurator.nexus.kodiapps.MainActivity
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.ManagePermissionsImp
import com.livetv.configurator.nexus.kodiapps.core.PermissionHelper
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.db.DataHelper

class SplashScreenActivity : AppCompatActivity() {
    internal var handler = Handler()

    lateinit var utils: Prefs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        DataHelper(this).checkDBExist()
        utils = Prefs(this)
        checkPermissions(this)

    }

    fun startapp(sleepTime: Long) {
        handler.postDelayed(startApp, sleepTime)
    }


    var startApp: Runnable = object : Runnable {
        override fun run() {
            handler.removeCallbacks(this)
            startNextActivity()

        }
    }

    fun checkPermissions(activity: AppCompatActivity) {
        val list = listOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )

        PermissionHelper.checkPermissions(
            activity,
            list,
            object : ManagePermissionsImp.IPermission {
                override fun onPermissionDenied() {
//                        showAlert()
                    if (activity is SplashScreenActivity) {
                        activity.startapp(1000)
                    }
                }

                override fun onPermissionGranted() {
                    if (activity is SplashScreenActivity) {
                        activity.startapp(1000)
                        Log.e("TAG", "onPermissionGranted:::: ")
                    }
                }
            })
    }


    fun startNextActivity() {
        if (utils.getPref(Constant.PREF_IS_FIRST_TIME, true) &&
            utils.getPref(Constant.PREF_WHATS_YOUR_GOAL, "").isNullOrEmpty()
        ) {
            val i = Intent(this, WhatsYourGoalActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
            finish()
        } else {
            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
            finish()
        }
    }


}
