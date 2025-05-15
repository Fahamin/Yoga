package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.facebook.ads.BuildConfig
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.livetv.configurator.nexus.kodiapps.MainActivity
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.SideMenuAdapter
import com.livetv.configurator.nexus.kodiapps.core.AlertDialogHelper
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.ManagePermissionsImp
import com.livetv.configurator.nexus.kodiapps.core.MiseUtils.isOnline
import com.livetv.configurator.nexus.kodiapps.core.PermissionHelper
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.core.interfaces.DateEventListener
import com.livetv.configurator.nexus.kodiapps.core.interfaces.DialogDismissListener
import com.livetv.configurator.nexus.kodiapps.databinding.BottomSheetSoundOptionBinding
import com.livetv.configurator.nexus.kodiapps.databinding.DialogChangeWorkoutTimeBinding
import com.livetv.configurator.nexus.kodiapps.databinding.DialogDobBinding
import com.livetv.configurator.nexus.kodiapps.databinding.DialogGenderDobBinding
import com.livetv.configurator.nexus.kodiapps.databinding.TopbarBinding
import com.livetv.configurator.nexus.kodiapps.db.DataHelper
import com.livetv.configurator.nexus.kodiapps.model.HomeExTableClass
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt

open class BaseActivity() : AppCompatActivity(){

    internal lateinit var commonReciever: MyEventServiceReciever
    var topBarBinding: TopbarBinding? = null
    lateinit var dbHelper: DataHelper
//    private var mRewardedVideoAd: RewardedVideoAd? = null
lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toast = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG)
        val intentFilter = IntentFilter()
        prefs = Prefs(this)
        
        intentFilter.addAction(Constant.FINISH_ACTIVITY)
        commonReciever = MyEventServiceReciever()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            commonReciever, intentFilter
        )
        val isKeepOn = prefs!!.getPref( Constant.PREF_IS_KEEP_SCREEN_ON, false)
        if (isKeepOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        dbHelper = DataHelper(this)


    }

    fun initTopBar(topbar: TopbarBinding) {
        topBarBinding = topbar
    }


    open fun getActivity(): BaseActivity {
        return this
    }


    /* fun initBack() {
         imgBack_!!.visibility = View.VISIBLE
         imgBack_!!.setOnClickListener {
             finishActivity()
         }
     }*/


    internal lateinit var toast: Toast

    fun showToast(text: String, duration: Int) {
        runOnUiThread {
            toast.setText(text)
            toast.duration = duration
            toast.show()
        }
    }

    fun showToast(text: String) {
        runOnUiThread {
            toast.setText(text)
            toast.duration = Toast.LENGTH_SHORT
            toast.show()
        }
    }





    var menuAdapter: SideMenuAdapter? = null

    open fun changeSelection(index: Int) {
        if ( menuAdapter != null) {
            menuAdapter!!.changeSelection(index)
        }
    }



    fun finishActivity() {
        if (getActivity() is MainActivity) {
        } else {
            getActivity().finish()
        }
    }



    internal inner class MyEventServiceReciever : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (intent.action!!.equals(Constant.FINISH_ACTIVITY, ignoreCase = true)) {
                    finishActivity()
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    var rotation: Animation? = null

    private fun loadAnimations() {
        AnimationUtils()
        rotation = AnimationUtils.loadAnimation(this, R.anim.rotate)

        try {
            if (topBarBinding != null) {
                topBarBinding!!.isInterNetAvailable = true
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
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

    var dialogPermission: AlertDialog? = null
    private fun showAlert() {
        val builder = AlertDialog.Builder(getActivity())
        builder.setTitle(getString(R.string.need_permission_title))
        builder.setCancelable(false)
        builder.setMessage(getString(R.string.err_need_permission_msg))
        builder.setPositiveButton(R.string.btn_ok) { dialog, which ->
            startActivity(
                Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
            )
            finish()
        }
        builder.setNeutralButton(R.string.btn_cancel, { dialog, which -> finish() })
        dialogPermission = builder.create()
        dialogPermission!!.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun isInternetAvailable(context: Context, callbackListener: CallbackListener) {
        if (prefs!!.isInternetConnected(context)) {
            callbackListener.onSuccess()
            return
        }
        AlertDialogHelper.showNoInternetDialog(context, callbackListener)
    }

    fun isInternetAvailable_(context: Context, callbackListener: CallbackListener) {
        if (prefs!!.isInternetConnected(context)) {
            callbackListener.onSuccess()
            return
        }
    }



    private var logoutPopupWindow: PopupWindow? = null



    lateinit var setWorkOutTimeDialog: Dialog
    fun showChangeTimeDialog(
        item: HomeExTableClass,
        listener: DialogInterface
    ) {
        var second = item.exTime!!.toInt()

        setWorkOutTimeDialog = Dialog(getActivity())
        setWorkOutTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        var dialogChangeWorkoutTimeBinding =
            DataBindingUtil.inflate<DialogChangeWorkoutTimeBinding>(
                getLayoutInflater(),
                R.layout.dialog_change_workout_time, null, false
            )

        setWorkOutTimeDialog.setContentView(dialogChangeWorkoutTimeBinding.root)

        dialogChangeWorkoutTimeBinding.tvTitle.text = item.exName
        dialogChangeWorkoutTimeBinding.tvDes.text = item.exDescription

        if (item.exUnit.equals(Constant.workout_type_step)) {
            dialogChangeWorkoutTimeBinding.tvTime.text = "X ${item.exTime}"
        } else {
            dialogChangeWorkoutTimeBinding.tvTime.text =
                prefs!!.secToString(item.exTime!!.toInt(), Constant.WORKOUT_TIME_FORMAT)
        }

        if (item.exVideo.isNullOrEmpty()) {
            dialogChangeWorkoutTimeBinding!!.imgVideo.visibility = View.GONE
        } else {
            dialogChangeWorkoutTimeBinding!!.imgVideo.visibility = View.VISIBLE
        }

        dialogChangeWorkoutTimeBinding.viewFlipper.removeAllViews()
        val listImg: java.util.ArrayList<String>? =
            prefs!!.ReplaceSpacialCharacters(item.exPath!!)?.let { prefs!!.getAssetItems(this, it) }

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
                    prefs!!.secToString(second, Constant.WORKOUT_TIME_FORMAT)
            }
        }

        dialogChangeWorkoutTimeBinding!!.imgPlus.setOnClickListener {
            second++
            if (item.exUnit.equals(Constant.workout_type_step)) {
                dialogChangeWorkoutTimeBinding.tvTime.text = "X $second"
            } else {
                dialogChangeWorkoutTimeBinding.tvTime.text =
                    prefs!!.secToString(second, Constant.WORKOUT_TIME_FORMAT)
            }
        }

        dialogChangeWorkoutTimeBinding!!.imgClose.setOnClickListener {
            listener.cancel()
            setWorkOutTimeDialog.dismiss()
        }

        dialogChangeWorkoutTimeBinding!!.tvSave.setOnClickListener {
            dbHelper.updatePlanExTime(item.dayExId!!.toInt(), second.toString())
            listener.dismiss()
            setWorkOutTimeDialog.dismiss()
        }

        dialogChangeWorkoutTimeBinding!!.tvReset.setOnClickListener {
            second = dbHelper.getOriginalPlanExTime(item.dayExId!!.toInt())!!.toInt()
            if (item.exUnit.equals(Constant.workout_type_step)) {
                dialogChangeWorkoutTimeBinding.tvTime.text = "X $second"
            } else {
                dialogChangeWorkoutTimeBinding.tvTime.text =
                    prefs!!.secToString(second!!.toInt(), Constant.WORKOUT_TIME_FORMAT)
            }
            listener.dismiss()
        }

        dialogChangeWorkoutTimeBinding!!.imgVideo.setOnClickListener {
            val i = Intent(getActivity(), ExerciseVideoActivity::class.java)
            i.putExtra("workoutPlanData", Gson().toJson(item))
            startActivity(i)
        }

        setWorkOutTimeDialog.getWindow()!!
            .setBackgroundDrawableResource(android.R.color.transparent);
        setWorkOutTimeDialog.show()
    }

    fun showExcDetailDialog(
        excList: MutableList<HomeExTableClass>,
        position: Int
    ) {
        var currPos = position
        val excDetailDialog = Dialog(getActivity())
        excDetailDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        var dialogWorkoutDetailBinding =
            DataBindingUtil.inflate<DialogChangeWorkoutTimeBinding>(
                getLayoutInflater(),
                R.layout.dialog_change_workout_time, null, false
            )

        excDetailDialog.setContentView(dialogWorkoutDetailBinding.root)

        dialogWorkoutDetailBinding.llEdit.visibility = View.GONE
        dialogWorkoutDetailBinding.llPlusMinus.visibility = View.GONE
        dialogWorkoutDetailBinding.llPrevNext.visibility = View.VISIBLE

        fun showExcDetail(pos: Int) {
            val item = excList.get(pos)
            dialogWorkoutDetailBinding.tvTitle.text = item.exName
            dialogWorkoutDetailBinding.tvDes.text = item.exDescription
            dialogWorkoutDetailBinding.tvPos.text = "${pos + 1}/${excList.size}"

            if (item.exUnit.equals(Constant.workout_type_step)) {
                dialogWorkoutDetailBinding.tvTime.text = "X ${item.exTime}"
            } else {
                dialogWorkoutDetailBinding.tvTime.text =
                    prefs!!.secToString(item.exTime!!.toInt(), Constant.WORKOUT_TIME_FORMAT)
            }

            if (item.exVideo.isNullOrEmpty()) {
                dialogWorkoutDetailBinding!!.imgVideo.visibility = View.GONE
            } else {
                dialogWorkoutDetailBinding!!.imgVideo.visibility = View.VISIBLE
            }

            dialogWorkoutDetailBinding.viewFlipper.removeAllViews()
            val listImg: ArrayList<String>? =
                prefs!!.ReplaceSpacialCharacters(item.exPath!!)?.let { prefs!!.getAssetItems(
                    this,
                    it
                ) }

            if (listImg != null) {
                for (i in 0 until listImg.size) {
                    val imgview = ImageView(this)
                    //            Glide.with(mContext).load("//android_asset/burpee/".plus(i.toString()).plus(".png")).into(imgview)
                    Glide.with(this).load(listImg.get(i)).into(imgview)
                    imgview.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    dialogWorkoutDetailBinding.viewFlipper.addView(imgview)
                }
            }

            dialogWorkoutDetailBinding.viewFlipper.isAutoStart = true
            dialogWorkoutDetailBinding.viewFlipper.setFlipInterval(resources.getInteger(R.integer.viewfliper_animation))
            dialogWorkoutDetailBinding.viewFlipper.startFlipping()

            dialogWorkoutDetailBinding!!.imgVideo.setOnClickListener {
                val i = Intent(getActivity(), ExerciseVideoActivity::class.java)
                i.putExtra("workoutPlanData", Gson().toJson(item))
                startActivity(i)
            }
        }

        showExcDetail(currPos)
        dialogWorkoutDetailBinding.imgNext.setOnClickListener {
            if (currPos < excList.size - 1) {
                currPos++
                showExcDetail(currPos)
            }
        }

        dialogWorkoutDetailBinding.imgPrev.setOnClickListener {
            if (currPos > 0) {
                currPos--
                showExcDetail(currPos)
            }
        }

        dialogWorkoutDetailBinding.imgClose.setOnClickListener {
            excDetailDialog.dismiss()
        }

        excDetailDialog.getWindow()!!
            .setBackgroundDrawableResource(android.R.color.transparent);
        excDetailDialog.show()

    }


    var dialogSoundOptionBinding: BottomSheetSoundOptionBinding? = null
    lateinit var dialogSoundOption: BottomSheetDialog

    fun showSoundOptionDialog(mContext: Context, listner: DialogDismissListener) {
        val v: View = (mContext as Activity).getLayoutInflater()
            .inflate(R.layout.bottom_sheet_sound_option, null)
        dialogSoundOptionBinding = DataBindingUtil.bind(v)
        dialogSoundOption = BottomSheetDialog(mContext)
        dialogSoundOption.setContentView(v)

        dialogSoundOptionBinding!!.switchMute.isChecked =
            prefs!!.getPref( Constant.PREF_IS_SOUND_MUTE, false)
        dialogSoundOptionBinding!!.switchCoachTips.isChecked =
            prefs!!.getPref( Constant.PREF_IS_COACH_SOUND_ON, true)
        dialogSoundOptionBinding!!.switchVoiceGuide.isChecked =
            prefs!!.getPref( Constant.PREF_IS_INSTRUCTION_SOUND_ON, true)

        dialogSoundOptionBinding!!.switchMute.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dialogSoundOptionBinding!!.switchCoachTips.isChecked = false
                dialogSoundOptionBinding!!.switchVoiceGuide.isChecked = false
                prefs!!.setPref( Constant.PREF_IS_SOUND_MUTE, true)
            } else {
                prefs!!.setPref( Constant.PREF_IS_SOUND_MUTE, false)
            }
        }

        dialogSoundOptionBinding!!.switchCoachTips.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dialogSoundOptionBinding!!.switchMute.isChecked = false
                prefs!!.setPref( Constant.PREF_IS_COACH_SOUND_ON, true)
            } else {
                prefs!!.setPref( Constant.PREF_IS_COACH_SOUND_ON, false)
            }
        }

        dialogSoundOptionBinding!!.switchVoiceGuide.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dialogSoundOptionBinding!!.switchMute.isChecked = false
                prefs!!.setPref( Constant.PREF_IS_INSTRUCTION_SOUND_ON, true)
            } else {
                prefs!!.setPref( Constant.PREF_IS_INSTRUCTION_SOUND_ON, false)
            }
        }

        dialogSoundOptionBinding!!.llDone.setOnClickListener {
            dialogSoundOption.dismiss()
        }

        dialogSoundOption.setOnDismissListener {
            listner.onDialogDismiss()
        }

        dialogSoundOption.show()
    }

    fun showHeightWeightDialog(listner: DialogDismissListener) {
        val builder = AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
        builder.setCancelable(false)

        val dialogLayout = layoutInflater.inflate(R.layout.dialog_height_weight, null)

        var boolKg: Boolean
        var boolInch: Boolean

        val editWeight = dialogLayout.findViewById<EditText>(R.id.editWeight)
        val tvKG = dialogLayout.findViewById<TextView>(R.id.tvKG)
        val tvLB = dialogLayout.findViewById<TextView>(R.id.tvLB)
        val editHeightCM = dialogLayout.findViewById<EditText>(R.id.editHeightCM)
        val tvCM = dialogLayout.findViewById<TextView>(R.id.tvCM)
        val tvIN = dialogLayout.findViewById<TextView>(R.id.tvIN)
        val editHeightFT = dialogLayout.findViewById<EditText>(R.id.editHeightFT)
        val editHeightIn = dialogLayout.findViewById<EditText>(R.id.editHeightIn)
        val llInch = dialogLayout.findViewById<LinearLayout>(R.id.llInch)

        var editWeightStr =""
        var editHeightCMStr  =""
        var editHeightFTStr  =""
        var editHeightInStr  =""


        /*For weight*/
        editWeight.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (editWeight.text.toString().equals("0.0 KG") || editWeight.text.toString().equals(
                        "0.0 LB"
                    ) ) {
                    editWeight.setText("")
                }else{
                    editWeight.setText(editWeight.text.toString())
                }
                editWeight.setSelection(editWeight.text!!.length)
            } else {
                if (prefs!!.getPref( Constant.PREF_LAST_INPUT_WEIGHT, 0f).toString() == "0.0") {
                    if (prefs!!.getPref( Constant.CHECK_LB_KG, Constant.DEF_KG) == Constant.DEF_KG) {
                        if (editWeightStr.isEmpty()) {
                            editWeight.setText("0.0 KG")
                        }else{
                            editWeight.setText(editWeight.text.toString())
                        }
                    } else {
                        if (editWeightStr.isEmpty()) {
                            editWeight.setText("0.0 LB")
                        }else{
                            editWeight.setText(editWeight.text.toString())
                        }
                        editWeight.setSelection(editWeight.text!!.length)
                    }

                } else {
                    editWeight.setText(
                        prefs!!.getPref( Constant.PREF_LAST_INPUT_WEIGHT, 0f).toString()
                    )
                    editWeight.setSelection(editWeight.text!!.length)
                }
            }
        }
        editWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().equals("0.0 KG").not() || s.toString().equals("0.0 LB").not()) {
                    editWeightStr = s.toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        /*for height cm*/
        editHeightCM.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (editHeightCM.text.toString().equals("0.0 CM")) {
                    editHeightCM.setText("")
                }else{
                    editHeightCM.setText(editHeightCM.text.toString())
                }
//                editHeightCM.setSelection(editHeightCM.text!!.length)
            } else {
                if (prefs!!.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString() == "0"
                    && prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0f).toString().equals(
                        "0.0"
                    )) {
                    if (editHeightCMStr.isEmpty()) {
                        editHeightCM.setText("0.0 CM")
                    }else{
                        editHeightCM.setText(editHeightCM.text.toString())
                    }
                    editHeightCM.setSelection(editHeightCM.text!!.length)
                } else {
                    val inch = prefs!!.ftInToInch(
                        prefs!!.getPref( Constant.PREF_LAST_INPUT_FOOT, 0),
                        prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0F).toDouble()
                    )
                    editHeightCM.setText(prefs!!.inchToCm(inch).roundToInt().toDouble().toString())
                    editHeightCM.setSelection(editHeightCM.text!!.length)
                }

            }
        }
        editHeightCM.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().equals("0.0 CM").not()) {
                    editHeightCMStr = s.toString()
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        /*for height ft*/
        editHeightFT.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {

                if (editHeightFT.text.toString().equals("0.0 FT")) {
                    editHeightFT.setText("")
                }else{
                    editHeightFT.setText(editHeightFT.text.toString())
                }

                editHeightFT.setSelection(editHeightFT.text!!.length)

            } else {
                if (prefs!!.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString() == "0") {

                    if (editHeightFTStr.isEmpty()) {
                        editHeightFT.setText("0.0 FT")
                    }else{
                        editHeightFT.setText(editHeightFT.text.toString())
                    }
                    editHeightFT.setSelection(editHeightFT.text!!.length)
                } else {
                    editHeightFT.setText(
                        prefs!!.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString()
                    )
                    editHeightFT.setSelection(editHeightFT.text!!.length)
                }

            }
        }
        editHeightFT.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                if (s.toString().equals("0.0 FT").not()) {
                    editHeightFTStr = s.toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        /*for height in*/
        editHeightIn.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (editHeightIn.text.toString().equals("0.0 IN")) {
                    editHeightIn.setText("")
                }else{
                    editHeightIn.setText(editHeightIn.text.toString())
                }
                editHeightIn.setSelection(editHeightIn.text!!.length)
            } else {
                if (prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0f).toString() == "0.0") {

                    if (editHeightInStr.isEmpty()) {
                        editHeightIn.setText("0.0 IN")
                    }else{
                        editHeightIn.setText(editHeightIn.text.toString())
                    }
                    editHeightIn.setSelection(editHeightIn.text!!.length)
                } else {
                    editHeightIn.setText(
                        prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0f).toString()
                    )
                    editHeightIn.setSelection(editHeightIn.text!!.length)
                }

            }
        }
        editHeightIn.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().equals("0.0 IN").not()) {
                    editHeightInStr = s.toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        boolKg = true
        try {
            if (prefs!!.getPref( Constant.CHECK_LB_KG, Constant.DEF_KG) == Constant.DEF_KG) {

                editWeight.setText(
                    prefs!!.getPref( Constant.PREF_LAST_INPUT_WEIGHT, 0f).toString()
                )

                editWeight.setSelection(editWeight.text!!.length)

                tvKG.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvLB.setTextColor(ContextCompat.getColor(this, R.color.col_666))

                tvKG.isSelected = true
                tvLB.isSelected = false
            } else {
                boolKg = false

                editWeight.setText(
                    prefs!!.kgToLb(
                        prefs!!.getPref(

                            Constant.PREF_LAST_INPUT_WEIGHT,
                            0f
                        ).toDouble()
                    ).toString()
                )

                editWeight.setSelection(editWeight.text!!.length)
                tvKG.setTextColor(ContextCompat.getColor(this, R.color.col_666))
                tvLB.setTextColor(ContextCompat.getColor(this, R.color.white))

                tvKG.isSelected = false
                tvLB.isSelected = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Log.e(
            "TAG", "showHeightWeightDialog:::Check LbKg:::::::: " + prefs!!.getPref(
                Constant.CHECK_LB_KG,
                Constant.DEF_LB
            )
        )
        boolInch = true
        try {
            if (prefs!!.getPref( Constant.CHECK_LB_KG, Constant.DEF_KG) == Constant.DEF_LB) {

                editHeightCM.visibility = View.GONE
                llInch.visibility = View.VISIBLE

                tvIN.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvCM.setTextColor(ContextCompat.getColor(this, R.color.col_666))

                tvIN.isSelected = true
                tvCM.isSelected = false

                /*if (prefs!!.getPref(Constant.PREF_LAST_INPUT_FOOT,0).toString().equals("0")){
                    editHeightFT.setText("0.0 FT")
                }else{
                    editHeightFT.setText(prefs!!.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString())
                }

                if (prefs!!.getPref(Constant.PREF_LAST_INPUT_INCH,0).toString().equals("0")){
                    editHeightIn.setText("0.0 IN")
                }else{
                    editHeightIn.setText(prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0F).toString())
                }*/
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)
            } else {
                boolInch = false

                editHeightCM.visibility = View.VISIBLE
                llInch.visibility = View.GONE

                tvIN.setTextColor(ContextCompat.getColor(this, R.color.col_666))
                tvCM.setTextColor(ContextCompat.getColor(this, R.color.white))

                tvIN.isSelected = false
                tvCM.isSelected = true

                /* if (prefs!!.getPref(Constant.PREF_LAST_INPUT_FOOT,0).toString().equals("0")
                         && prefs!!.getPref(Constant.PREF_LAST_INPUT_INCH,0).toString().equals("0")){
 
                 }else{
 
                 }*/
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)

                /*val inch = prefs!!.ftInToInch(
                        prefs!!.getPref( Constant.PREF_LAST_INPUT_FOOT, 0),
                        prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0F).toDouble()
                )

                editHeightCM.setText(prefs!!.inchToCm(inch).roundToInt().toDouble().toString())*/
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        tvKG.setOnClickListener {
            try {

                prefs!!.setPref( Constant.CHECK_LB_KG, Constant.DEF_KG)
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)
                if (boolInch) {
                    boolInch = false

                    editHeightCM.visibility = View.VISIBLE
                    llInch.visibility = View.GONE

                    tvIN.setTextColor(ContextCompat.getColor(this, R.color.col_666))
                    tvCM.setTextColor(ContextCompat.getColor(this, R.color.white))

                    tvIN.isSelected = false
                    tvCM.isSelected = true

                    var inch = 0.0
                    if (editHeightFT.text.toString() != "" && editHeightFT.text.toString() != "") {
                        inch = prefs!!.ftInToInch(
                            editHeightFT.text.toString().toInt(),
                            editHeightIn.text.toString().toDouble()
                        )
                    } else if (editHeightFT.text.toString() != "" && editHeightIn.text.toString() == "") {
                        inch = prefs!!.ftInToInch(editHeightFT.text.toString().toInt(), 0.0)
                    } else if (editHeightFT.text.toString() == "" && editHeightIn.text.toString() != "") {
                        inch = prefs!!.ftInToInch(1, editHeightIn.text.toString().toDouble())
                    }

                    editHeightCM.setText(prefs!!.inchToCm(inch).roundToInt().toDouble().toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                if (!boolKg) {
                    boolKg = true

                    tvKG.setTextColor(ContextCompat.getColor(this, R.color.white))
                    tvLB.setTextColor(ContextCompat.getColor(this, R.color.col_666))

                    tvKG.isSelected = true
                    tvLB.isSelected = false

//                    editWeight.hint = Constant.DEF_KG

                    if (editWeight.text.toString() != "") {
                        editWeight.setText(
                            prefs!!.lbToKg(editWeight.text.toString().toDouble()).toString()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        tvLB.setOnClickListener {
            try {
                prefs!!.setPref( Constant.CHECK_LB_KG, Constant.DEF_LB)
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)
                if (boolKg) {
                    boolKg = false


                    tvKG.setTextColor(ContextCompat.getColor(this, R.color.col_666))
                    tvLB.setTextColor(ContextCompat.getColor(this, R.color.white))

                    tvKG.isSelected = false
                    tvLB.isSelected = true

//                    editWeight.hint = Constant.DEF_LB

                    if (editWeight.text.toString() != "") {
                        editWeight.setText(
                            prefs!!.kgToLb(editWeight.text.toString().toDouble()).toString()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                editHeightCM.visibility = View.GONE
                llInch.visibility = View.VISIBLE

                tvIN.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvCM.setTextColor(ContextCompat.getColor(this, R.color.col_666))

                tvIN.isSelected = true
                tvCM.isSelected = false

                try {
                    if (!boolInch) {
                        boolInch = true

                        if (editHeightCM.text.toString() != "") {
                            val inch = prefs!!.cmToInch(editHeightCM.text.toString().toDouble())
                            editHeightFT.setText(prefs!!.calcInchToFeet(inch).toString())
                            editHeightIn.setText(prefs!!.calcInFromInch(inch).toString())
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        tvCM.setOnClickListener {
            try {
                prefs!!.setPref( Constant.CHECK_LB_KG, Constant.DEF_KG)
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)
                editWeight.setSelection(editWeight.text!!.length)
                if (boolInch) {
                    boolInch = false

                    editHeightCM.visibility = View.VISIBLE
                    llInch.visibility = View.GONE

                    tvIN.setTextColor(ContextCompat.getColor(this, R.color.col_666))
                    tvCM.setTextColor(ContextCompat.getColor(this, R.color.white))

                    tvIN.isSelected = false
                    tvCM.isSelected = true

                    if (editHeightIn.text.toString().equals("0.0 IN") || editHeightFT.text.toString().equals(
                            "0.0 FT"
                        )){
                        editHeightCM.setText("0.0 CM")
                    }else {
                        var inch = 0.0
                        if (editHeightFT.text.toString() != "" && editHeightIn.text.toString() != "") {
                            inch = prefs!!.ftInToInch(
                                editHeightFT.text.toString().toInt(),
                                editHeightIn.text.toString().toDouble()
                            )
                        } else if (editHeightFT.text.toString() != "" && editHeightIn.text.toString() == "") {
                            inch = prefs!!.ftInToInch(editHeightFT.text.toString().toInt(), 0.0)
                        } else if (editHeightFT.text.toString() == "" && editHeightIn.text.toString() != "") {
                            inch = prefs!!.ftInToInch(1, editHeightIn.text.toString().toDouble())
                        }

                        editHeightCM.setText(
                            prefs!!.inchToCm(inch).roundToInt().toDouble().toString()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                if (!boolKg) {
                    boolKg = true

                    tvKG.setTextColor(ContextCompat.getColor(this, R.color.white))
                    tvLB.setTextColor(ContextCompat.getColor(this, R.color.col_666))

                    tvKG.isSelected = true
                    tvLB.isSelected = false

//                    editWeight.hint = Constant.DEF_KG

                    if (editWeight.text.toString() != "") {
                        editWeight.setText(
                            prefs!!.lbToKg(editWeight.text.toString().toDouble()).toString()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        tvIN.setOnClickListener {
            try {
                prefs!!.setPref( Constant.CHECK_LB_KG, Constant.DEF_LB)
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)
                if (boolKg) {
                    boolKg = false


                    tvKG.setTextColor(ContextCompat.getColor(this, R.color.col_666))
                    tvLB.setTextColor(ContextCompat.getColor(this, R.color.white))

                    tvKG.isSelected = false
                    tvLB.isSelected = true

//                    editWeight.hint = Constant.DEF_LB

                    if (editWeight.text.toString() != "") {
                        editWeight.setText(
                            prefs!!.kgToLb(editWeight.text.toString().toDouble()).toString()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                editHeightCM.visibility = View.GONE
                llInch.visibility = View.VISIBLE

                tvIN.setTextColor(ContextCompat.getColor(this, R.color.white))
                tvCM.setTextColor(ContextCompat.getColor(this, R.color.col_666))

                tvIN.isSelected = true
                tvCM.isSelected = false

                try {
                    if (!boolInch) {
                        boolInch = true

                        if (editHeightCM.text.toString() != "") {
                            val inch = prefs!!.cmToInch(editHeightCM.text.toString().toDouble())
                            editHeightFT.setText(prefs!!.calcInchToFeet(inch).toString())
                            editHeightIn.setText(prefs!!.calcInFromInch(inch).toString())
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        builder.setView(dialogLayout)

        val dob = prefs!!.getPref( Constant.PREF_DOB, "")
        var posBtnText = ""
        if (dob.isNullOrEmpty()) {
            posBtnText = getString(R.string.next)
        } else {
            posBtnText = getString(R.string.set)
        }

        editWeight.setOnClickListener {
            if (editWeight.text.toString().equals("0.0 KG") || editWeight.text.toString().equals("0.0 LB")){
                editWeight.setText("")
            }
        }
        builder.setPositiveButton(posBtnText) { dialog, which ->

            try {
                if (boolInch) {
                    prefs!!.setPref(
                        Constant.PREF_LAST_INPUT_FOOT,
                        editHeightFT.text.toString().toInt()
                    )
                    prefs!!.setPref(
                        Constant.PREF_LAST_INPUT_INCH,
                        editHeightIn.text.toString().toFloat()
                    )
                    prefs!!.setPref( Constant.PREF_HEIGHT_UNIT, Constant.DEF_IN)

                } else {
                    val inch = prefs!!.cmToInch(editHeightCM.text.toString().toDouble())
                    prefs!!.setPref( Constant.PREF_LAST_INPUT_FOOT, prefs!!.calcInchToFeet(inch))
                    prefs!!.setPref(
                        Constant.PREF_LAST_INPUT_INCH,
                        prefs!!.calcInFromInch(inch).toFloat()
                    )
                    prefs!!.setPref( Constant.PREF_HEIGHT_UNIT, Constant.DEF_CM)

                }


                val strKG: Float
                if (boolKg) {
                    strKG = editWeight.text.toString().toFloat()
                    prefs!!.setPref( Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG)
                    prefs!!.setPref( Constant.PREF_LAST_INPUT_WEIGHT, strKG)
                } else {
                    strKG =
                        prefs!!.lbToKg(editWeight.text.toString().toDouble()).roundToInt().toFloat()
                    prefs!!.setPref( Constant.PREF_WEIGHT_UNIT, Constant.DEF_LB)
                    prefs!!.setPref( Constant.PREF_LAST_INPUT_WEIGHT, strKG)
                }

                val currentDate = prefs!!.parseTime(Date().time, Constant.WEIGHT_TABLE_DATE_FORMAT)

                if (dbHelper.weightExistOrNot(currentDate)) {
                    dbHelper.updateWeight(currentDate, strKG.toString(), "")
                } else {
                    dbHelper.addUserWeight(strKG.toString(), currentDate, "")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (dob.isNullOrEmpty()) {
                showGenderDOBDialog(this, listner)
            } else {
                listner.onDialogDismiss()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.btn_cancel) { dialog, which ->
            dialog.dismiss()
            listner.onDialogDismiss()
        }
        builder.create().show()


    }

    private fun checkLbKg(
        editWeight: EditText,
        editHeightCM: EditText,
        editHeightFT: EditText,
        editHeightIn: EditText
    ) {

        /*for weight*/

        if (prefs!!.getPref( Constant.PREF_LAST_INPUT_WEIGHT, 0f).toString().equals("0.0")
            && prefs!!.getPref( Constant.CHECK_LB_KG, Constant.DEF_KG).equals(Constant.DEF_KG)) {
            editWeight.setText("0.0 KG")
        } else if (prefs!!.getPref( Constant.PREF_LAST_INPUT_WEIGHT, 0f).toString().equals("0.0")
            && prefs!!.getPref( Constant.CHECK_LB_KG, Constant.DEF_LB).equals(Constant.DEF_LB)) {
            editWeight.setText("0.0 LB")
        }
        editWeight.setSelection(editWeight.text!!.length)

        /*for height cm*/
        Log.e(
            "TAG",
            "checkLbKg:::::FIFIIF " + prefs!!.getPref( Constant.PREF_LAST_INPUT_FOOT, 0) + " " +
                    prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0f)
        )

        if (prefs!!.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString().equals("0")
            && prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0f).toString().equals("0.0")) {
            editHeightCM.setText("0.0 CM")
        } else {
            val inch = prefs!!.ftInToInch(
                prefs!!.getPref( Constant.PREF_LAST_INPUT_FOOT, 0),
                prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0F).toDouble()
            )
            Log.e(
                "TAG", "checkLbKg:::::FIFIIF " + prefs!!.getPref(

                    Constant.PREF_LAST_INPUT_FOOT,
                    0
                ) + " " +
                        prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0f)
            )
            editHeightCM.setText(prefs!!.inchToCm(inch).roundToInt().toDouble().toString())
        }


        /*for height ft*/
        if (prefs!!.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString().equals("0")) {
            editHeightFT.setText("0.0 FT")
        } else {
            editHeightFT.setText(prefs!!.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString())
        }


        /*for height in*/
        if (prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0f).toString().equals("0.0")) {
            editHeightIn.setText("0.0 IN")
        } else {
            editHeightIn.setText(prefs!!.getPref( Constant.PREF_LAST_INPUT_INCH, 0f).toString())
        }

    }

    private val DEFAULT_MIN_YEAR = 1960
    private var yearPos = 0
    private var monthPos = 0
    private var dayPos = 0

    var yearList = arrayListOf<String?>()
    var monthList = arrayListOf<String?>()
    var dayList = arrayListOf<String?>()

    private var minYear = 0
    private var maxYear = 0
    private var viewTextSize = 0

    fun showGenderDOBDialog(
        mContext: Context,
        listner: DialogDismissListener
    ) {
        val builder = AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
        builder.setCancelable(false)
        val v: View = (mContext as Activity).getLayoutInflater()
            .inflate(R.layout.dialog_gender_dob, null)
        val dialogbinding: DialogGenderDobBinding? = DataBindingUtil.bind(v)

        builder.setView(dialogbinding!!.root)

        if (prefs!!.getPref( Constant.PREF_GENDER, "").isNullOrEmpty().not()) {
            if (prefs!!.getPref( Constant.PREF_GENDER, "").equals(Constant.FEMALE)) {
                dialogbinding.tvFemale.isSelected = true
                dialogbinding.tvMale.isSelected = false
            } else {
                dialogbinding.tvMale.isSelected = true
                dialogbinding.tvFemale.isSelected = false
            }
        }

        dialogbinding.tvMale.setOnClickListener {
            dialogbinding.tvMale.isSelected = true
            dialogbinding.tvFemale.isSelected = false
        }

        dialogbinding.tvFemale.setOnClickListener {
            dialogbinding.tvFemale.isSelected = true
            dialogbinding.tvMale.isSelected = false
        }

        minYear = DEFAULT_MIN_YEAR
        viewTextSize = 25
        maxYear = Calendar.getInstance().get(Calendar.YEAR) - 10

        setSelectedDate()
        dialogbinding.tvMale.isSelected = true
        initPickerViews(dialogbinding.dobPicker)
        initDayPickerView(dialogbinding.dobPicker)

        builder.setPositiveButton(R.string.save) { dialog, which ->

            if (dialogbinding.tvMale.isSelected || dialogbinding.tvFemale.isSelected) {

                val day = dayList[dialogbinding.dobPicker.npDay.value]
                val month = monthList[dialogbinding.dobPicker.npMonth.value]
                val year = yearList[dialogbinding.dobPicker.npYear.value]

                val dob = prefs!!.parseTime("$day-$month-$year", "dd-mm-yyyy")

                if (dob < Date()) {

                    prefs!!.setPref( Constant.PREF_DOB, "$day-$month-$year")
                    if (dialogbinding.tvMale.isSelected)
                        prefs!!.setPref( Constant.PREF_GENDER, Constant.MALE)

                    if (dialogbinding.tvFemale.isSelected)
                        prefs!!.setPref( Constant.PREF_GENDER, Constant.FEMALE)

                    dialog.dismiss()
                    listner.onDialogDismiss()
                } else {
                    showToast("Date of birth must less then current date")
                }
            }
        }
        builder.setNegativeButton(R.string.btn_cancel) { dialog, which ->
            dialog.dismiss()
            listner.onDialogDismiss()
        }
        builder.setNeutralButton(R.string.previous) { dialog, which ->
            dialog.dismiss()
            showHeightWeightDialog(listner)
        }
        builder.create().show()
    }

    fun showYearOfBirthDialog(mContext: Context, listner: DialogDismissListener) {
        val builder = AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
        builder.setCancelable(false)
        val v: View = (mContext as Activity).getLayoutInflater()
            .inflate(R.layout.dialog_dob, null)
        val dialogbinding: DialogDobBinding? = DataBindingUtil.bind(v)

        builder.setView(dialogbinding!!.root)

        minYear = DEFAULT_MIN_YEAR;
        viewTextSize = 25
        maxYear = Calendar.getInstance().get(Calendar.YEAR) - 10

        setSelectedDate()

        initPickerViews(dialogbinding)
        initDayPickerView(dialogbinding)

        builder.setPositiveButton(R.string.set) { dialog, which ->
            val day = dayList[dialogbinding.npDay.value]
            val month = monthList[dialogbinding.npMonth.value]
            val year = yearList[dialogbinding.npYear.value]

            prefs!!.setPref( Constant.PREF_DOB, "$day-$month-$year")
            dialog.dismiss()
            listner.onDialogDismiss()
        }
        builder.setNegativeButton(R.string.btn_cancel, { dialog, which -> dialog.dismiss() })
        builder.create().show()
    }

    open fun setSelectedDate() {
        var today = Calendar.getInstance()
        if (prefs!!.getPref( Constant.PREF_DOB, "").isNullOrEmpty().not()) {
            val date = prefs!!.parseTime(prefs!!.getPref( Constant.PREF_DOB, "")!!, "dd-mm-yyyy")
            today.time = date
        }
        yearPos = today[Calendar.YEAR] - minYear
        monthPos = today[Calendar.MONTH]
        dayPos = today[Calendar.DAY_OF_MONTH] - 1
    }


    fun initPickerViews(dialogbinding: DialogDobBinding) {
        val yearCount: Int = maxYear - minYear
        for (i in 0 until yearCount) {
            yearList.add(format2LenStr(minYear + i))
        }
        for (j in 0..11) {
            monthList.add(format2LenStr(j + 1))
        }
        dialogbinding.npYear.setDisplayedValues(yearList.toArray(arrayOf()))
        dialogbinding.npYear.setMinValue(0)
        dialogbinding.npYear.setMaxValue(yearList.size - 1)
        dialogbinding.npYear.value = yearPos

        dialogbinding.npMonth.setDisplayedValues(monthList.toArray(arrayOf()))
        dialogbinding.npMonth.setMinValue(0)
        dialogbinding.npMonth.setMaxValue(monthList.size - 1)
        dialogbinding.npMonth.value = monthPos

    }

    /**
     * Init day item
     */
    fun initDayPickerView(dialogbinding: DialogDobBinding) {
        val dayMaxInMonth: Int
        val calendar = Calendar.getInstance()
        dayList = arrayListOf()
        calendar[Calendar.YEAR] = minYear + yearPos
        calendar[Calendar.MONTH] = monthPos

        //get max day in month
        dayMaxInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 0 until dayMaxInMonth) {
            dayList.add(format2LenStr(i + 1))
        }

        dialogbinding.npDay.setDisplayedValues(dayList.toArray(arrayOf()))
        dialogbinding.npDay.setMinValue(0)
        dialogbinding.npDay.setMaxValue(dayList.size - 1)
        dialogbinding.npDay.value = dayPos
    }

    open fun format2LenStr(num: Int): String? {
        return if (num < 10) "0$num" else num.toString()
    }

    open fun isLeapYear(year: Int): Boolean {
        val cal = Calendar.getInstance()
        cal[Calendar.YEAR] = year
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365
    }

    fun showTimePickerDialog(
        context: Context,
        date: Date,
        eventListener: DateEventListener?,
        hour: Int,
        minute: Int
    ) {
        val c = Calendar.getInstance()
        c.time = date
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            context,
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->


                //Date date = new Date(selectedyear, selectedmonth, selectedday, hourOfDay, minute, 0);
                val date = prefs!!.parseTime(
                    c.get(Calendar.DAY_OF_MONTH)
                        .toString() + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(
                        Calendar.YEAR
                    ) + " " + hourOfDay + ":" + minute + ":00",
                    "dd/MM/yyyy HH:mm:ss"
                )
                eventListener?.onDateSelected(date, hourOfDay, minute)
            }, hour, minute, false
        )
        timePicker.show()
    }



    fun initVideoAd() {
        /*if (mRewardedVideoAd == null) {
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        }*/

        MobileAds.initialize(this) {
            // Initialization code here, if needed
        }
    }

    /*    fun loadVideoAd(adLoadCallback: RewardedAdLoadCallback) {
            if (mRewardedVideoAd!!.isLoaded.not())
                mRewardedVideoAd!!.loadAd(
                    Constant.GOOGLE_REWARDED_VIDEO_ID,
                    AdRequest.Builder().build()
                )
        }
    
        fun showVideoAd(listner: CallbackListener) {
    
            if (Debug.DEBUG_IS_HIDE_AD) {
                listner.onSuccess()
                return
            }
    
            val adLoadCallback = object : RewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
    
                }
    
            }
    
            mRewardedVideoAd!!.rewardedVideoAdListener = object : RewardedVideoAdListener {
                override fun onRewardedVideoAdClosed() {
    
                }
    
                override fun onRewardedVideoAdLeftApplication() {
                    listner.onCancel()
                }
    
                override fun onRewardedVideoAdLoaded() {
                    dismissDialog()
                    mRewardedVideoAd!!.show()
                }
    
                override fun onRewardedVideoAdOpened() {
    
                }
    
                override fun onRewardedVideoCompleted() {
                    listner.onSuccess()
                }
    
                override fun onRewarded(p0: RewardItem?) {
                    listner.onSuccess()
                }
    
                override fun onRewardedVideoStarted() {
                }
    
                override fun onRewardedVideoAdFailedToLoad(p0: Int) {
                    Debug.e("onRewardedVideoAdFailedToLoad ==>", p0.toString())
                    dismissDialog()
                    listner.onSuccess()
                }
    
            }
    
            if (mRewardedVideoAd!!.isLoaded)
                mRewardedVideoAd!!.show()
            else {
                showDialog("")
                loadVideoAd(adLoadCallback)
            }
    
        }*/

    private var rewardedAd: RewardedAd? = null





    fun openInternetDialog(callbackListener: CallbackListener) {
        if (!isOnline(this)) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("No internet Connection")
            builder.setCancelable(false)
            builder.setMessage("Please turn on internet connection to continue")
            builder.setNegativeButton("Retry") { dialog, _ ->
                dialog!!.dismiss()
                openInternetDialog(callbackListener)
                callbackListener.onRetry()

            }
            builder.setPositiveButton("Close") { dialog, _ ->
                dialog!!.dismiss()
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(homeIntent)
                finishAffinity()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }


}