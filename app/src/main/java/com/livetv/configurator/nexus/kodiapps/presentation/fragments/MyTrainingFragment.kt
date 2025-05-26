package com.livetv.configurator.nexus.kodiapps.presentation.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.HomePlansAdapter
import com.livetv.configurator.nexus.kodiapps.adapter.HomeWeekGoalAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Fun
import com.livetv.configurator.nexus.kodiapps.core.Fun.addShow
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.core.interfaces.TopBarClickListener
import com.livetv.configurator.nexus.kodiapps.databinding.FragmentMyTrainingBinding
import com.livetv.configurator.nexus.kodiapps.model.HistoryDetailsClass
import com.livetv.configurator.nexus.kodiapps.model.HomePlanTableClass
import com.livetv.configurator.nexus.kodiapps.presentation.activity.DaysPlanDetailActivity
import com.livetv.configurator.nexus.kodiapps.presentation.activity.DiscoverActivity
import com.livetv.configurator.nexus.kodiapps.presentation.activity.ExercisesListActivity
import com.livetv.configurator.nexus.kodiapps.presentation.activity.MainActivity
import com.livetv.configurator.nexus.kodiapps.presentation.activity.MyTrainingExcListActivity
import com.livetv.configurator.nexus.kodiapps.presentation.activity.RecentActivity
import com.livetv.configurator.nexus.kodiapps.presentation.activity.SetYourWeeklyGoalActivity
import com.livetv.configurator.nexus.kodiapps.presentation.activity.SubPlanActivity
import kotlin.jvm.java
import kotlin.math.roundToInt

class MyTrainingFragment : BaseFragment(), CallbackListener {

    lateinit var binding: FragmentMyTrainingBinding
    lateinit var mContext: Context


    var homeWeekGoalAdapter: HomeWeekGoalAdapter? = null
    var homePlansAdapter: HomePlansAdapter? = null
    var recentPlan: HomePlanTableClass? = null
    var lastWorkout: HistoryDetailsClass? = null
    var onClickAd = 1
    var pref: Prefs? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_training, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pref = Prefs(requireActivity())
        Fun(requireActivity())

        val adContainerView = view.findViewById<FrameLayout>(R.id.ad_view_container)
        Fun.showBanner(requireActivity(), adContainerView)
        initview()
    }

    private fun initview() {
        this.mContext = binding.root.context
        binding.handler = ClickHandler()
        init()

    }

    private fun init() {
//        binding!!.topbar.isMenuShow = true
//        binding!!.topbar.isDiscoverShow = true
//        binding!!.topbar.topBarClickListener = TopClickListener()
        binding!!.topbar.tvTitleText.text = getString(R.string.menu_training_plans)
//        binding!!.handler = ClickHandler()

        setupWeekTopData()

        homePlansAdapter = HomePlansAdapter(mContext)
        binding!!.rvPlans.layoutManager = LinearLayoutManager(mContext)
        binding!!.rvPlans.adapter = homePlansAdapter
        homePlansAdapter!!.setEventListener(object : HomePlansAdapter.EventListener {

            override fun onItemClick(position: Int, view: View) {
                addShow()
                startNextScreenMove(position)


            }
        })

        homePlansAdapter!!.addAll(dbHelper!!.getHomePlanList(Constant.PlanTypeWorkout))


    }

    fun startNextScreenMove(position: Int) {
        val item = homePlansAdapter!!.getItem(position)
        if (item.hasSubPlan) {
            val i = Intent(mContext, SubPlanActivity::class.java)
            i.putExtra("workoutPlanData", Gson().toJson(item))
            startActivity(i)
        } else if (item.planDays.equals("YES")) {
            val i = Intent(mContext, DaysPlanDetailActivity::class.java)
            i.putExtra("workoutPlanData", Gson().toJson(item))
            startActivity(i)
        } else {
            val i = Intent(mContext, ExercisesListActivity::class.java)
            i.putExtra("workoutPlanData", Gson().toJson(item))
            if (item.isPro) {
                i.putExtra(Constant.IS_PURCHASE, true)
            } else {
                i.putExtra(Constant.IS_PURCHASE, false)
            }
            startActivity(i)
        }
    }

    private fun setupWeekTopData() {

        try {
            if (pref!!.getPref(Constant.PREF_IS_WEEK_GOAL_DAYS_SET, false)) {


                val arrCurrentWeek = pref!!.getCurrentWeek()
                var completedWeekDay = 0
                for (pos in 0 until arrCurrentWeek.size) {
                    if (dbHelper!!.isHistoryAvailable(
                            pref!!.parseTime(
                                arrCurrentWeek[pos],
                                Constant.DATE_TIME_24_FORMAT,
                                Constant.DATE_FORMAT
                            )
                        )
                    ) {
                        completedWeekDay++
                    }
                }

                val weekDayGoal = pref!!.getPref(Constant.PREF_WEEK_GOAL_DAYS, 7)

                homeWeekGoalAdapter = HomeWeekGoalAdapter(mContext)



            }


            ((dbHelper!!.getHistoryTotalMinutes() / 60).toDouble()).roundToInt().toString()

            lastWorkout = dbHelper!!.getRecentHistory()
            if (lastWorkout != null) {
                recentPlan = dbHelper!!.getPlanByPlanId(lastWorkout!!.PlanId.toInt())

                if (recentPlan!!.planDays == Constant.PlanDaysYes) {

                    val item = dbHelper!!.getDaysPlanData(lastWorkout!!.DayId)
                    recentPlan!!.planMinutes = item!!.Minutes
                    recentPlan!!.planWorkouts = item!!.Workouts
                }


            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
        setupWeekTopData()
        homePlansAdapter?.notifyDataSetChanged()
        Log.e("TAG", "MainActivity:::::::::onResume:::Main Activity:::::: ")
    }


    inner class TopClickListener : TopBarClickListener {
        override fun onTopBarClickListener(view: View?, value: String?) {
            pref!!.hideKeyBoard(requireActivity(), view!!)

            if (value.equals(getString(R.string.menu_discover))) {
                val i = Intent(mContext, DiscoverActivity::class.java)
                startActivity(i)
            }

        }
    }

    inner class ClickHandler {

        fun onSetGoalClick() {
            val i = Intent(mContext, SetYourWeeklyGoalActivity::class.java)
            startActivityForResult(i, 8019)
        }

        fun onEditWeekGoalClick() {
            val i = Intent(mContext, SetYourWeeklyGoalActivity::class.java)
            startActivity(i)
        }

        fun onBackToTopClick() {
            binding!!.nestedScrollView.isSmoothScrollingEnabled = true
            binding!!.nestedScrollView.fullScroll(View.FOCUS_UP)
        }

        fun onMyTrainingClick() {
//            val i = Intent(mContext, MyTrainingActivity::class.java)
            val i = Intent(mContext, MainActivity::class.java)
            i.putExtra(Constant.FROMMYTRAININGFRAGMENT, 2)
            startActivity(i)
        }

        fun onRecentViewAllClick() {
            val i = Intent(mContext, RecentActivity::class.java)
            startActivity(i)
        }

        fun onRecentViewClick() {

            if (recentPlan!!.planType.equals(Constant.PlanTypeMyTraining)) {
                val i = Intent(mContext, MyTrainingExcListActivity::class.java)
                i.putExtra("workoutPlanData", Gson().toJson(recentPlan))
                startActivity(i)
            } else if (recentPlan!!.hasSubPlan) {
                val i = Intent(mContext, SubPlanActivity::class.java)
                i.putExtra("workoutPlanData", Gson().toJson(recentPlan))
                startActivity(i)
            } else if (recentPlan!!.planDays.equals("YES")) {
                val i = Intent(mContext, DaysPlanDetailActivity::class.java)
                i.putExtra("workoutPlanData", Gson().toJson(recentPlan))
                startActivity(i)
            } else {
                val i = Intent(mContext, ExercisesListActivity::class.java)
                i.putExtra("workoutPlanData", Gson().toJson(recentPlan))
                i.putExtra(Constant.IS_PURCHASE, false)
                startActivity(i)
            }

            /*val i = Intent(mContext, ExercisesListActivity::class.java)
            i.putExtra("workoutPlanData", Gson().toJson(recentPlan))
            i.putExtra(Constant.extra_day_id, lastWorkout!!.DayId)
            i.putExtra("day_name", lastWorkout!!.DayName)
            startActivity(i)*/
        }
    }


    override fun onSuccess() {

    }

    override fun onCancel() {

    }

    override fun onRetry() {

    }

}