package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.AdUtils
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityAddExerciseDetailBinding
import com.livetv.configurator.nexus.kodiapps.model.MyTrainingCatExTableClass
import com.livetv.configurator.nexus.kodiapps.model.MyTrainingCategoryTableClass

class AddExerciseDetailActivity : BaseActivity(), CallbackListener {

    var binding: ActivityAddExerciseDetailBinding? = null
    var workoutPlanData: MyTrainingCatExTableClass? = null
    var categoryData: MyTrainingCategoryTableClass? = null
    var defaultSecond = 20
    var second = 0
    var pos: Int? = null
    var isEditTime = false

    var pref: Prefs? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_exercise_detail)

        pref = Prefs(this)
//        AdUtils.loadBannerAd(binding!!.adView,this)

//        AdUtils.loadBannerGoogleAd(this,binding!!.llAdView,Constant.BANNER_TYPE)

        if (Constant.AD_TYPE_FB_GOOGLE == Constant.AD_GOOGLE) {
            AdUtils.loadGoogleBannerAd(this, binding!!.llAdView, Constant.BANNER_TYPE)
            binding!!.llAdViewFacebook.visibility=View.GONE
        }else if (Constant.AD_TYPE_FB_GOOGLE == Constant.AD_FACEBOOK) {
            AdUtils.loadFacebookBannerAd(this,binding!!.llAdViewFacebook)
        }else{
            binding!!.llAdViewFacebook.visibility=View.GONE
        }


        initIntentParam()
        init()
    }

    private fun initIntentParam() {
        try {
            if (intent.extras != null) {
                if (intent.extras!!.containsKey("ExDetail")) {
                    val str = intent.getStringExtra("ExDetail")
                    workoutPlanData = Gson().fromJson(
                        str,
                        object : TypeToken<MyTrainingCatExTableClass>() {}.type
                    )!!
                }

                if (intent.extras!!.containsKey("categoryDetail")) {
                    val str = intent.getStringExtra("categoryDetail")
                    categoryData = Gson().fromJson(
                        str,
                        object : TypeToken<MyTrainingCategoryTableClass>() {}.type
                    )!!
                }
                if (intent.extras!!.containsKey("pos")) {
                    pos = intent.getIntExtra("pos", -1)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        binding!!.handler = ClickHandler()

        if (pos != null && pos!! >= 0) {
            binding!!.btnAdd.text = getString(R.string.save)
        }

        fillData()

    }


    private fun fillData() {
        if (workoutPlanData != null) {

            binding!!.viewFlipper.removeAllViews()
            val listImg: ArrayList<String>? =
                pref!!.ReplaceSpacialCharacters(workoutPlanData!!.exPath!!)
                    ?.let { pref!!.getAssetItems(this, it) }

            if (listImg != null) {
                for (i in 0 until listImg.size) {
                    val imgview = ImageView(this)

                    Glide.with(this).load(listImg.get(i)).into(imgview)
                    imgview.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    binding!!.viewFlipper.addView(imgview)
                }
            }

            binding!!.viewFlipper.isAutoStart = true
            binding!!.viewFlipper.flipInterval = resources.getInteger(R.integer.viewfliper_animation)
            binding!!.viewFlipper.startFlipping()


            binding!!.tvTitle.text = workoutPlanData!!.exName
            binding!!.tvDes.text = workoutPlanData!!.exDescription
            if(workoutPlanData!!.exVideo.isNullOrEmpty())
            {
                binding!!.llVideo.visibility = View.GONE
            }else{
                binding!!.llVideo.visibility = View.VISIBLE
            }


            if (workoutPlanData!!.exTime.isNullOrEmpty()) {
                binding!!.llTime.visibility = View.GONE
            } else {
                binding!!.llTime.visibility = View.VISIBLE
                if (workoutPlanData!!.exReplaceTime.isNullOrEmpty()) {
                    second = workoutPlanData!!.exTime!!.toInt()
                } else {
                    second = workoutPlanData!!.exReplaceTime!!.toInt()
                    binding!!.tvReset.visibility = View.VISIBLE
                }
                defaultSecond = workoutPlanData!!.exTime!!.toInt()

                if (workoutPlanData!!.exUnit.equals(Constant.workout_type_step)) {
                    binding!!.tvTime.text = "$second"
                } else {
                    binding!!.tvTime.text = pref!!.secToString(
                        second,
                        Constant.WORKOUT_TIME_FORMAT
                    )
                }
            }
        }
    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
    }


    inner class ClickHandler {

        fun onCancelClick() {
            finish()
        }

        fun onResetClick() {
            if (workoutPlanData!!.exUnit.equals(Constant.workout_type_step)) {
                binding!!.tvTime.text = "$defaultSecond"
            } else {
                binding!!.tvTime.text =
                    pref!!.secToString(defaultSecond, Constant.WORKOUT_TIME_FORMAT)
            }
            binding!!.tvReset.visibility = View.GONE
            workoutPlanData!!.exReplaceTime = ""
            second = defaultSecond
            isEditTime = false
        }

        fun onMinusTimeClick() {
            if (second > 1)
                second--
            binding!!.tvTime.text = pref!!.secToString(second, "mm:ss")
            binding!!.tvReset.visibility = View.VISIBLE
            isEditTime = true
        }

        fun onAddTimeClick() {
            second++
            binding!!.tvTime.text = pref!!.secToString(second, "mm:ss")
            binding!!.tvReset.visibility = View.VISIBLE
            isEditTime = true
        }

        fun onVideoClick() {
            val i = Intent(getActivity(), ExerciseVideoActivity::class.java)
            i.putExtra("workoutPlanData", Gson().toJson(workoutPlanData))
            startActivity(i)
        }

        fun onAddClick() {

            val i = Intent()
            if (isEditTime)
                workoutPlanData!!.exReplaceTime = second.toString()
            i.putExtra("workoutPlanData", Gson().toJson(workoutPlanData))
            i.putExtra("categoryDetail", Gson().toJson(categoryData))
            if (pos != null && pos!! >= 0) {
                i.putExtra("pos", pos!!)
            }
            setResult(Activity.RESULT_OK, i)
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