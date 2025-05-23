package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.WeekPlanAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Fun
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityDaysPlanDetailBinding
import com.livetv.configurator.nexus.kodiapps.model.HomePlanTableClass
import com.livetv.configurator.nexus.kodiapps.model.PWeeklyDayData

class DaysPlanDetailActivity : BaseActivity(), CallbackListener {

    var binding: ActivityDaysPlanDetailBinding? = null
    var weekPlanAdapter: WeekPlanAdapter? = null
    var workoutPlanData: HomePlanTableClass? = null
    lateinit var weeklyDayStatusList: ArrayList<PWeeklyDayData>

    lateinit var pref: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_days_plan_detail)

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
                        TypeToken<HomePlanTableClass>() {}.type)!!
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        binding!!.handler = ClickHandler()

        weekPlanAdapter = WeekPlanAdapter(this, workoutPlanData!!)
        binding!!.rvWeeks.layoutManager = LinearLayoutManager(this)
        binding!!.rvWeeks.setAdapter(weekPlanAdapter)

        weekPlanAdapter!!.setEventListener(object : WeekPlanAdapter.EventListener {
            override fun onItemClick(
                position: Int,
                child: Int,
                view: View
            ) {
                startWorkOut(position, child)
            }

        })


        fillData()

    }

    private fun startWorkOut(position: Int, child: Int) {
        val item = weekPlanAdapter!!.getItem(position).arrWeekDayData.get(child)

        workoutPlanData!!.planMinutes = item.Minutes
        workoutPlanData!!.planWorkouts = item.Workouts
        val intent = Intent(this@DaysPlanDetailActivity, ExercisesListActivity::class.java)
        intent.putExtra("workoutPlanData", Gson().toJson(workoutPlanData))
        intent.putExtra(Constant.extra_day_id, item.Day_id)
        intent.putExtra("day_name", item.Day_id)
        intent.putExtra("Week_name", weekPlanAdapter!!.getItem(position).Week_name)
        intent.putExtra(Constant.IS_PURCHASE, true)
        startActivity(intent)
    }


    private fun fillData() {
        if (workoutPlanData != null) {
            binding!!.tvTitleText.text = workoutPlanData!!.planName
            binding!!.tvIntroductionDes.text = workoutPlanData!!.introduction

            weeklyDayStatusList = dbHelper.getWorkoutWeeklyData(workoutPlanData!!.planName!!)
            weekPlanAdapter!!.addAll(weeklyDayStatusList)

            pref.setDayProgressData(
                this,
                workoutPlanData!!.planId!!,
                binding!!.tvDayLeft,
                binding!!.tvPer,
                binding!!.pbDay
            )
        }
    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
        fillData()
    }


    inner class ClickHandler {

        fun onStartClick() {
            try {
                if (weekPlanAdapter!!.continueDataChildPos != null && weekPlanAdapter!!.continueDataParentPos != null) {
                    startWorkOut(
                        weekPlanAdapter!!.continueDataParentPos!!,
                        weekPlanAdapter!!.continueDataChildPos!!
                    )
                } else {
                    startWorkOut(0, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun onIntroductionClick() {
            if (binding!!.llIntroductionDes.visibility == View.VISIBLE) {
                binding!!.llIntroductionDes.visibility = View.GONE
            } else {
                binding!!.llIntroductionDes.visibility = View.VISIBLE
            }
        }

        fun onBackClick() {
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