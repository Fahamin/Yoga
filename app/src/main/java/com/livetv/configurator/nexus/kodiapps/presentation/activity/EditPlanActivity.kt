package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.EditPlanAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Fun
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.core.interfaces.TopBarClickListener
import com.livetv.configurator.nexus.kodiapps.core.swr.OnDragListener
import com.livetv.configurator.nexus.kodiapps.core.swr.OnSwipeListener
import com.livetv.configurator.nexus.kodiapps.core.swr.RecyclerHelper
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityEditPlanBinding
import com.livetv.configurator.nexus.kodiapps.db.DataHelper
import com.livetv.configurator.nexus.kodiapps.model.HomeExTableClass
import com.livetv.configurator.nexus.kodiapps.model.HomePlanTableClass

class EditPlanActivity : BaseActivity(), CallbackListener {
    lateinit var pref: Prefs

    var binding: ActivityEditPlanBinding? = null
    var editPlanAdapter: EditPlanAdapter? = null
    var workoutPlanData: HomePlanTableClass? = null
    var isSaveDialogShow = false
    var touchHelper: RecyclerHelper<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_plan)


        pref = Prefs(this)


//        AdUtils.loadBannerAd(binding!!.adView,this)
//        AdUtils.loadBannerGoogleAd(this,binding!!.llAdView,Constant.BANNER_TYPE)

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
        binding!!.topbar.isBackShow = true
        binding!!.topbar.isMoreShow = true
        binding!!.topbar.tvTitleText.text = getString(R.string.edit_plan)
        binding!!.topbar.topBarClickListener = TopClickListener()
        binding!!.handler = ClickHandler()

        editPlanAdapter = EditPlanAdapter(this)
        binding!!.rvWorkOuts.layoutManager = LinearLayoutManager(this)
        binding!!.rvWorkOuts.setAdapter(editPlanAdapter)

        editPlanAdapter!!.setEventListener(object : EditPlanAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                showChangeTimeDialog(editPlanAdapter!!.getItem(position), object : DialogInterface {
                    override fun dismiss() {
                        getExerciseData()
                        setResult(Activity.RESULT_OK)
                    }

                    override fun cancel() {

                    }

                })
            }

            override fun onReplaceClick(position: Int, view: View) {
                val i = Intent(this@EditPlanActivity, ReplaceExerciseActivity::class.java)
                i.putExtra("ExcData", Gson().toJson(editPlanAdapter!!.getItem(position)))
                startActivityForResult(i, 8989)
            }

        })


        fillData()
        getExerciseData()
    }

    private fun getExerciseData() {
        if (workoutPlanData!!.planDays == Constant.PlanDaysYes) {
            val dayId = intent.getStringExtra(Constant.extra_day_id)
            if (dayId != null) {
                //arrDayExTableClass = DataHelper(this).getDayExList(dayId)
            }
        } else {
            editPlanAdapter!!.addAll(DataHelper(this).getHomeDetailExList(workoutPlanData!!.planId!!))
        }

        touchHelper = RecyclerHelper<HomeExTableClass>(
            editPlanAdapter!!.data!!,
            editPlanAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>
        )
        touchHelper!!.setRecyclerItemDragEnabled(true)
            .setOnDragItemListener(object : OnDragListener {
                override fun onDragItemListener(fromPosition: Int, toPosition: Int) {
                    editPlanAdapter!!.onChangePosition(fromPosition,toPosition)
                    isSaveDialogShow = true
                }
            })
        touchHelper!!.setRecyclerItemSwipeEnabled(true)
            .setOnSwipeItemListener(object : OnSwipeListener {
                override fun onSwipeItemListener() {

                }
            })
        val itemTouchHelper = ItemTouchHelper(touchHelper!!)
        itemTouchHelper.attachToRecyclerView(binding!!.rvWorkOuts)
    }

    private fun fillData() {
        if (workoutPlanData != null) {
            binding!!.tvWorkoutTime.text =
                workoutPlanData!!.planMinutes + " " + getString(R.string.mins)
            binding!!.tvTotalWorkout.text =
                workoutPlanData!!.planWorkouts + " " + getString(R.string.workouts)
        }
    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
    }


    inner class ClickHandler {

        fun onSaveClick() {
            saveExercise()
        }

    }

    inner class TopClickListener : TopBarClickListener {
        override fun onTopBarClickListener(view: View?, value: String?) {
            pref.hideKeyBoard(getActivity(), view!!)

            if (value.equals(getString(R.string.back))) {
                if (isSaveDialogShow)
                    showSaveConfirmationDialog()
                else
                    finish()
            }

            if (value.equals(getString(R.string.more))) {
                val menu = PopupMenu(this@EditPlanActivity, view)

                menu.menu.add(getString(R.string.reset_plan))

                menu.setOnMenuItemClickListener { item ->
                    if (item!!.title!! == getString(R.string.reset_plan)) {
                        dbHelper.resetPlanExc(editPlanAdapter!!.data)
                        getExerciseData()
                        setResult(Activity.RESULT_OK)
                    }
                    true
                }

                menu.gravity = Gravity.TOP
                menu.show()
            }

        }
    }



    private fun showSaveConfirmationDialog() {
        val builder = AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
        builder.setCancelable(false)
        builder.setMessage(getString(R.string.save_changes_que))
        builder.setPositiveButton(R.string.btn_ok) { dialog, which ->
            saveExercise()
        }
        builder.setNegativeButton(R.string.btn_cancel, { dialog, which -> finish() })
        builder.create().show()
    }

    private fun saveExercise() {
        for (i in 0..editPlanAdapter!!.data!!.size - 1) {
            val item = editPlanAdapter!!.data[i]
            item.planSort = (i + 1).toString()
            dbHelper.updatePlanEx(item)
        }
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 8989 && resultCode == Activity.RESULT_OK) {
            showToast("Exercise Replaced successfully")
            setResult(Activity.RESULT_OK)
            getExerciseData()
        }
    }

    override fun onSuccess() {

    }

    override fun onCancel() {

    }

    override fun onRetry() {

    }

}