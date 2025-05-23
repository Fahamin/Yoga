package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.ReplaceExerciseAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Fun
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.core.interfaces.TopBarClickListener
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityReplaceExerciseBinding
import com.livetv.configurator.nexus.kodiapps.databinding.DialogChangeWorkoutTimeBinding
import com.livetv.configurator.nexus.kodiapps.model.HomeExTableClass

class ReplaceExerciseActivity : BaseActivity(), CallbackListener {

    var binding: ActivityReplaceExerciseBinding? = null
    var replaceExerciseAdapter: ReplaceExerciseAdapter? = null
    var homeExTableClass: HomeExTableClass? = null

    lateinit var pref: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_replace_exercise)


         Fun(this)
        val adContainerView = findViewById<FrameLayout>(R.id.ad_view_container)
        Fun.showBanner(this, adContainerView)
        pref = Prefs(this)


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
                if (intent.extras!!.containsKey("ExcData")) {
                    val str = intent.getStringExtra("ExcData")
                    homeExTableClass = Gson().fromJson(str, object :
                        TypeToken<HomeExTableClass>() {}.type)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        binding!!.topbar.isBackShow = true
        binding!!.topbar.tvTitleText.text = getString(R.string.replace_exercise)
        binding!!.topbar.topBarClickListener = TopClickListener()
        binding!!.handler = ClickHandler()

        replaceExerciseAdapter = ReplaceExerciseAdapter(this)
        binding!!.rvWorkOuts.layoutManager = LinearLayoutManager(this)
        binding!!.rvWorkOuts.setAdapter(replaceExerciseAdapter)

        replaceExerciseAdapter!!.setEventListener(object : ReplaceExerciseAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                showReplaceExcDialog(position)
            }

        })


        fillData()
        getExerciseData()
    }

    private fun getExerciseData() {

        val allExcList = dbHelper.getReplaceExList(homeExTableClass!!.exId!!)

        if(homeExTableClass!!.replaceExId.isNullOrEmpty().not())
        {
            val originalExID =  dbHelper.getOriginalPlanExID(homeExTableClass!!.dayExId!!.toInt())

            for (i in allExcList.indices)
            {
                val item = allExcList.get(i)
                if(allExcList.get(i).exId.equals(originalExID))
                {
                    allExcList.removeAt(i)
                    allExcList.add(0,item)
                }
            }
        }

        replaceExerciseAdapter!!.addAll(allExcList)


    }

    private fun fillData() {
        if (homeExTableClass != null) {

            binding!!.tvName.text = homeExTableClass!!.exName
            if (homeExTableClass!!.exUnit.equals(Constant.workout_type_step)) {
                binding!!.tvTime.text = "X ${homeExTableClass!!.exTime}"
            } else {
                binding!!.tvTime.text = pref.secToString(
                    homeExTableClass!!.exTime!!.toInt(),
                    Constant.WORKOUT_TIME_FORMAT
                )
            }

            binding!!.viewFlipper.removeAllViews()
            val listImg: ArrayList<String>? =
                pref.ReplaceSpacialCharacters(homeExTableClass!!.exPath!!)
                    ?.let { pref.getAssetItems(this, it) }

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
            binding!!.viewFlipper.setFlipInterval(resources.getInteger(R.integer.viewfliper_animation))
            binding!!.viewFlipper.startFlipping()
        }
    }

    lateinit var replaceExcDialog: Dialog
    fun showReplaceExcDialog(pos: Int) {
        val item = replaceExerciseAdapter!!.getItem(pos)

        var second = 0
        if (item.exTime.isNullOrEmpty().not()) {
            second = item.exTime!!.trim().toInt()
        }

        replaceExcDialog = Dialog(getActivity())
        replaceExcDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        var dialogChangeWorkoutTimeBinding =
            DataBindingUtil.inflate<DialogChangeWorkoutTimeBinding>(
                getLayoutInflater(),
                R.layout.dialog_change_workout_time, null, false
            )

        replaceExcDialog.setContentView(dialogChangeWorkoutTimeBinding.root)

        dialogChangeWorkoutTimeBinding.tvTitle.text = item.exName
        dialogChangeWorkoutTimeBinding.tvDes.text = item.exDescription
        dialogChangeWorkoutTimeBinding!!.tvSave.text = getString(R.string.replace)

        if (item.exUnit.equals(Constant.workout_type_step)) {
            dialogChangeWorkoutTimeBinding.tvTime.text = "X ${item.exTime}"
        } else {
            dialogChangeWorkoutTimeBinding.tvTime.text = pref.secToString(item.exTime!!.trim().toInt(), Constant.WORKOUT_TIME_FORMAT)
        }

        dialogChangeWorkoutTimeBinding.viewFlipper.removeAllViews()
        val listImg: java.util.ArrayList<String>? =
            pref.ReplaceSpacialCharacters(item.exPath!!)?.let { pref.getAssetItems(this, it) }

        if (listImg != null) {
            for (i in 0 until listImg.size) {
                val imgview = ImageView(this)
                //            Glide.with(mContext).load("//android_asset/burpee/".plus(i.toString()).plus(".png")).into(imgview)
                Glide.with(this).load(listImg.get(i)).into(imgview)
                imgview.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                dialogChangeWorkoutTimeBinding.viewFlipper.addView(imgview)
            }
        }

        dialogChangeWorkoutTimeBinding.viewFlipper.isAutoStart = true
        dialogChangeWorkoutTimeBinding.viewFlipper.setFlipInterval(resources.getInteger(R.integer.viewfliper_animation))
        dialogChangeWorkoutTimeBinding.viewFlipper.startFlipping()


        dialogChangeWorkoutTimeBinding!!.imgMinus.setOnClickListener {
            second--
            if (item.exUnit.equals(Constant.workout_type_step)) {
                dialogChangeWorkoutTimeBinding.tvTime.text = "X $second"
            } else {
                dialogChangeWorkoutTimeBinding.tvTime.text =
                    pref.secToString(second, Constant.WORKOUT_TIME_FORMAT)
            }
        }

        dialogChangeWorkoutTimeBinding!!.imgPlus.setOnClickListener {
            second++
            if (item.exUnit.equals(Constant.workout_type_step)) {
                dialogChangeWorkoutTimeBinding.tvTime.text = "X $second"
            } else {
                dialogChangeWorkoutTimeBinding.tvTime.text =
                    pref.secToString(second, Constant.WORKOUT_TIME_FORMAT)
            }
        }

        dialogChangeWorkoutTimeBinding!!.imgClose.setOnClickListener {

            replaceExcDialog.dismiss()
        }

        dialogChangeWorkoutTimeBinding!!.tvSave.setOnClickListener {
            dbHelper.replaceExercise(
                homeExTableClass!!.dayExId.toString(),
                item.exId!!,
                second.toString()
            )
            setResult(Activity.RESULT_OK)
            finish()
            replaceExcDialog.dismiss()
        }

        dialogChangeWorkoutTimeBinding!!.tvReset.setOnClickListener {
            second = item.exTime!!.trim().toInt()
            if (item.exUnit.equals(Constant.workout_type_step)) {
                dialogChangeWorkoutTimeBinding.tvTime.text = "X $second"
            } else {
                dialogChangeWorkoutTimeBinding.tvTime.text =
                    pref.secToString(second, Constant.WORKOUT_TIME_FORMAT)
            }
        }

        replaceExcDialog.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent);
        replaceExcDialog.show()
    }


    inner class ClickHandler {


    }

    inner class TopClickListener : TopBarClickListener {
        override fun onTopBarClickListener(view: View?, value: String?) {
            pref.hideKeyBoard(getActivity(), view!!)

            if (value.equals(getString(R.string.back))) {
                finish()
            }
        }
    }

    override fun onSuccess() {

    }

    override fun onCancel() {

    }

    override fun onRetry() {

    }


}