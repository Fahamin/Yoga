package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Fun
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityExerciseVideoBinding
import com.livetv.configurator.nexus.kodiapps.model.HomeExTableClass

class ExerciseVideoActivity : AppCompatActivity() {

    var binding: ActivityExerciseVideoBinding? = null
    var workoutPlanData: HomeExTableClass? = null
    lateinit var pref: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_exercise_video)
        pref = Prefs(this)

         Fun(this)
        val adContainerView = findViewById<FrameLayout>(R.id.ad_view_container)
        Fun.showBanner(this, adContainerView)

        initIntentParam()
        init()
    }

    private fun initIntentParam() {
        try {
            if (intent.extras != null) {
                if (intent.extras!!.containsKey("workoutPlanData")) {
                    val str = intent.getStringExtra("workoutPlanData")
                    workoutPlanData = Gson().fromJson(str, object :
                        TypeToken<HomeExTableClass>() {}.type)!!
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        binding!!.handler = ClickHandler()

        try {
            fillData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun fillData() {

        binding!!.viewFlipper.removeAllViews()
        val listImg: ArrayList<String>? =
            pref.ReplaceSpacialCharacters(workoutPlanData!!.exPath!!)
                ?.let { pref.getAssetItems(this, it) }

        if (listImg != null) {
            for (i in 0 until listImg.size) {
                val imgview = ImageView(this)
                //            Glide.with(mContext).load("//android_asset/burpee/".plus(i.toString()).plus(".png")).into(imgview)
                Glide.with(this).load(listImg.get(i)).into(imgview)
                imgview.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                binding!!.viewFlipper.addView(imgview)
            }
        }

        binding!!.viewFlipper.isAutoStart = true
        binding!!.viewFlipper.setFlipInterval(resources.getInteger(R.integer.viewfliper_animation))
        binding!!.viewFlipper.startFlipping()


        binding!!.tvTitle.text = workoutPlanData!!.exName
        binding!!.tvDes.text = workoutPlanData!!.exDescription

    }

    @SuppressLint("WrongConstant")
    inner class ClickHandler {

        fun onBackClick() {
            finish()
        }



        fun onAnimationClick() {


            binding!!.txtAnimation.setTypeface(Typeface.createFromAsset(assets, "lato_black.ttf"),0)
            binding!!.viewAnimation.visibility = View.VISIBLE
            binding!!.viewFlipper.visibility = View.VISIBLE

        }
    }




}