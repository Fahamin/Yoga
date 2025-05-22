package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Fun
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.databinding.ActivitySubPlanBinding
import com.livetv.configurator.nexus.kodiapps.model.HomePlanTableClass

class SubPlanActivity : BaseActivity(), CallbackListener {

    var binding: ActivitySubPlanBinding? = null
    var workoutPlanData: HomePlanTableClass? = null
    var subPlans:ArrayList<HomePlanTableClass>? = null
    lateinit var pref: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sub_plan)
        pref = Prefs(this)

        initIntentParam()
        init()

         Fun(this)
        val adContainerView = findViewById<FrameLayout>(R.id.ad_view_container)
        Fun.showBanner(this, adContainerView)



    }

    private fun initIntentParam() {
        try {
            if (intent.extras != null) {
                if (intent.extras!!.containsKey("workoutPlanData")) {
                    val str = intent.getStringExtra("workoutPlanData")
                    workoutPlanData = Gson().fromJson(str, object :
                        TypeToken<HomePlanTableClass>() {}.type)!!
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        binding!!.handler = ClickHandler()

        fillData()

    }


    private fun fillData() {
        if (workoutPlanData != null) {
            binding!!.tvTitle.text = workoutPlanData!!.planName
            binding!!.tvIntroductionDes.text = workoutPlanData!!.introduction
            binding!!.imgCover.setImageResource(
                pref.getDrawableId(
                    workoutPlanData!!.planImage,
                    this
                )
            )

            subPlans = dbHelper.getHomeSubPlanList(workoutPlanData!!.planId!!)

            for (item in subPlans!!)
            {
                if (item.planLvl!!.equals(Constant.PlanLvlBeginner)) {
                    binding!!.tvBeginnerTime.text = item.planMinutes + " mins"
                } else if (item.planLvl!!.equals(Constant.PlanLvlIntermediate)) {
                    binding!!.tvInterMediateTime.text = item.planMinutes + " mins"
                } else if (item.planLvl!!.equals(Constant.PlanLvlAdvanced)) {
                    binding!!.tvAdvancedTime.text = item.planMinutes + " mins"
                }

            }
        }
    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
    }


    inner class ClickHandler {

        fun onBackClick() {
            finish()
        }

        fun onBeginnerClick() {
            val i = Intent(this@SubPlanActivity,ExercisesListActivity::class.java)

            for (item in subPlans!!)
            {
                if (item.planLvl!!.equals(Constant.PlanLvlBeginner)) {
                    i.putExtra("workoutPlanData", Gson().toJson(item))
                    i.putExtra(Constant.IS_PURCHASE,true)
                }
            }

            startActivity(i)
        }

        fun onIntermediateClick() {
            val i = Intent(this@SubPlanActivity,ExercisesListActivity::class.java)

            for (item in subPlans!!)
            {
                if (item.planLvl!!.equals(Constant.PlanLvlIntermediate)) {
                    i.putExtra("workoutPlanData", Gson().toJson(item))
                    i.putExtra(Constant.IS_PURCHASE,true)
                }
            }

            startActivity(i)
        }

        fun onAdvanceClick() {
            val i = Intent(this@SubPlanActivity,ExercisesListActivity::class.java)
            for (item in subPlans!!)
            {
                if (item.planLvl!!.equals(Constant.PlanLvlAdvanced)) {
                    i.putExtra("workoutPlanData", Gson().toJson(item))
                    i.putExtra(Constant.IS_PURCHASE,true)
                }
            }
            startActivity(i)
        }
    }

    override fun onSuccess() {

    }

    override fun onCancel() {

    }

    override fun onRetry() {

    }


}