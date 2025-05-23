package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Fun
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityPauseBeforeStartBinding
import com.livetv.configurator.nexus.kodiapps.model.HomeExTableClass

class PauseBeforeStartActivity : BaseActivity(), CallbackListener {

    var binding: ActivityPauseBeforeStartBinding? = null
    var nextExercise: HomeExTableClass? = null
    var nextPos = 0
    var totalEx = 0
    lateinit var pref: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pause_before_start)
        pref = Prefs(this)
         Fun(this)
        val adContainerView = findViewById<FrameLayout>(R.id.ad_view_container)
        Fun.showBanner(this, adContainerView)
        initIntentParam()
        init()
    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
    }
    private fun initIntentParam() {
        try {
            if (intent.extras != null) {
                if (intent.extras!!.containsKey("workoutPlanData")) {
                    val str = intent.getStringExtra("workoutPlanData")
                    nextExercise = Gson().fromJson(str, object : TypeToken<HomeExTableClass>() {}.type)!!
                }
                if (intent.extras!!.containsKey("nextPos")) {
                    nextPos = intent.getIntExtra("nextPos",2)
                }
                if (intent.extras!!.containsKey("totalEx")) {
                    totalEx = intent.getIntExtra("totalEx",0)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        binding!!.handler = ClickHandler()

        binding!!.tvName.text = nextExercise!!.exName
        binding!!.tvTotalEx.text = "$nextPos / $totalEx"
        if (nextExercise!!.exUnit.equals(Constant.workout_type_step)) {
            binding!!.tvTime.text = "X ${nextExercise!!.exTime}"
        } else {
            binding!!.tvTime.text = pref.secToString(nextExercise!!.exTime!!.toInt(), Constant.WORKOUT_TIME_FORMAT)
        }

        binding!!.viewFlipper.removeAllViews()
        val listImg: ArrayList<String>? = pref.ReplaceSpacialCharacters(nextExercise!!.exPath!!)?.let { pref.getAssetItems(this, it) }

        if (listImg != null) {
            for (i in 0 until listImg.size) {
                val imgview = ImageView(this)
                //            Glide.with(mContext).load("//android_asset/burpee/".plus(i.toString()).plus(".png")).into(imgview)
                Glide.with(this).load(listImg.get(i)).into(imgview)
                imgview.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                binding!!.viewFlipper.addView(imgview)
            }
        }

        binding!!.viewFlipper.isAutoStart = true
        binding!!.viewFlipper.setFlipInterval(resources.getInteger(R.integer.viewfliper_animation))
        binding!!.viewFlipper.startFlipping()


    }


    inner class ClickHandler {

        fun onStartClick() {
            finish()
        }

        fun onExerciseInfoClick(){
            val i = Intent(this@PauseBeforeStartActivity, ExerciseVideoActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onSuccess() {
    }

    override fun onCancel() {
    }

    override fun onRetry() {
    }


}