package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.WorkoutProgressIndicatorAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.CountDownTimerWithPause
import com.livetv.configurator.nexus.kodiapps.core.Fun
import com.livetv.configurator.nexus.kodiapps.core.Fun.addShow
import com.livetv.configurator.nexus.kodiapps.core.MyApplication
import com.livetv.configurator.nexus.kodiapps.core.MySoundUtil
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.AdsCallback
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.core.interfaces.DialogDismissListener
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityPerformWorkOutBinding
import com.livetv.configurator.nexus.kodiapps.databinding.BottomSheetSoundOptionBinding
import com.livetv.configurator.nexus.kodiapps.databinding.DialogQuiteWorkoutBinding
import com.livetv.configurator.nexus.kodiapps.db.DataHelper
import com.livetv.configurator.nexus.kodiapps.model.HomeExTableClass
import com.livetv.configurator.nexus.kodiapps.model.HomePlanTableClass
import com.livetv.configurator.nexus.kodiapps.model.Music

class PerformWorkOutActivity : BaseActivity(), CallbackListener {

    val TAG = PerformWorkOutActivity::class.java.name + Constant.ARROW
    var binding: ActivityPerformWorkOutBinding? = null
    var workoutProgressIndicatorAdapter: WorkoutProgressIndicatorAdapter? = null
    var workoutPlanData: HomePlanTableClass? = null
    var exercisesList: java.util.ArrayList<HomeExTableClass>? = null
    private lateinit var mySoundUtil: MySoundUtil

    var currentPos = 0
    var currentExe: HomeExTableClass? = null
    var totalExTime = 0L

    private var exStartTime: Long = 0
    private var running = false
    private var currentTime: Long = 0
    private var timeCountDown = 0
    var currMusic: Music? = null
    var isPaused = false
    lateinit var pref: Prefs

    var timer: CountDownTimerWithPause? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerformWorkOutBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        pref = Prefs(this)
         Fun(this)
        val adContainerView = findViewById<FrameLayout>(R.id.ad_view_container)
        Fun.showBanner(this, adContainerView)
        initIntentParam()
        init()
        initReadyToGo()

    }

    private fun initIntentParam() {
        try {
            if (intent.extras != null) {
                workoutPlanData = intent.getStringExtra("workoutPlanData")?.let {
                    Gson().fromJson(it, object : TypeToken<HomePlanTableClass>() {}.type)
                }

                exercisesList = intent.getStringExtra("ExcList")?.let {
                    Gson().fromJson(it, object : TypeToken<ArrayList<HomeExTableClass>>() {}.type)
                }

                currentPos = intent.getIntExtra("currentPos", 0)

                // Ensure we have valid exercises list and position
                if (!exercisesList.isNullOrEmpty()) {
                    currentExe = exercisesList!!.getOrNull(currentPos) ?: exercisesList!!.first()
                } else {
                    // Handle empty exercise list case
                    finishWithError("No exercises provided")
                    return
                }
            } else {
                // Handle no extras case
                finishWithError("No workout data provided")
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
            finishWithError("Invalid workout data")
        }
    }

    private fun finishWithError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    private fun init() {
        binding!!.handler = ClickHandler()

        mySoundUtil = MySoundUtil(this)

        workoutProgressIndicatorAdapter = WorkoutProgressIndicatorAdapter(this)
        binding!!.rcyWorkoutStatus.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding!!.rcyWorkoutStatus.adapter = workoutProgressIndicatorAdapter
        workoutProgressIndicatorAdapter!!.setTotalExercise(exercisesList!!.size)

        initMusic(true)

        if (pref!!.getPref( Constant.PREF_IS_SOUND_MUTE, false)){
            binding!!.imgSound.setImageResource(R.drawable.ic_mute_sound)
        }else{
            binding!!.imgSound.setImageResource(R.drawable.ic_sound_round)
        }



    }

    private fun initMusic(isPlayMusic: Boolean) {

        if (pref!!.getPref( Constant.PREF_IS_MUSIC_SELECTED, false)) {
            val str = pref!!.getPref( Constant.PREF_MUSIC, "")
            if (str.isNullOrEmpty().not()) {
                currMusic = Gson().fromJson<Music>(str, object : TypeToken<Music>() {}.type)
                if (pref!!.getPref( Constant.PREF_IS_MUSIC_MUTE, false)
                        .not()
                ) {
                    if (isPlayMusic)
                        playMusic()
                }
            }
        }

        setPlayPauseView()
    }

    private fun loadWorkoutImage() {
        binding?.viewFlipper?.removeAllViews()

        currentExe?.let { ex ->
            ex.exPath?.let { path ->
                val listImg = pref.ReplaceSpacialCharacters(path)?.let {
                    pref.getAssetItems(this, it)
                }

                listImg?.takeIf { it.isNotEmpty() }?.forEach { imgPath ->
                    val imgview = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        Glide.with(this@PerformWorkOutActivity)
                            .load(imgPath)
                            .into(this)
                    }
                    binding?.viewFlipper?.addView(imgview)
                }

                binding?.viewFlipper?.apply {
                    isAutoStart = true
                    setFlipInterval(resources.getInteger(R.integer.viewfliper_animation))
                    startFlipping()
                }
            } ?: run {
                // Handle case where exPath is null or empty
                loadDefaultImage()
            }
        } ?: run {
            // Handle case where currentExe is null
            loadDefaultImage()
        }
    }

    private fun loadDefaultImage() {
        // Load a placeholder image when no exercise images are available
        val imgview = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            Glide.with(this@PerformWorkOutActivity)
                .load(R.drawable.ic_music_default_cover)
                .into(this)
        }
        binding?.viewFlipper?.addView(imgview)
    }

    private fun initReadyToGo() {

        loadWorkoutImage()
        binding!!.llReadyToGo.visibility = View.VISIBLE
        binding!!.llAfterStartWithTime.visibility = View.GONE
        binding!!.llAfterStartWithSteps.visibility = View.GONE
        binding!!.tvExcNameReadyToGo.text = currentExe!!.exName

        countDownReadyToGo()

        val readyToGoText = "Ready to go start with ${currentExe!!.exName}"

        MyApplication.speechText(this, readyToGoText)
    }

    private fun countDownReadyToGo() {

        var timeCountDown = 0

        val readyToGoTime = pref!!.getPref(this,Constant.PREF_READY_TO_GO_TIME, Constant.DEFAULT_READY_TO_GO_TIME)
        binding!!.progressBarReadyToGo.max = readyToGoTime.toInt()
        binding!!.progressBarReadyToGo.secondaryProgress = readyToGoTime.toInt()

        timer = object : CountDownTimerWithPause(readyToGoTime * 1000L, 1000, true) {
            override fun onFinish() {
                binding!!.tvCountDownReadyToGO.text = "0"
                binding!!.progressBarReadyToGo.progress = readyToGoTime.toInt()
                exStartTime = System.currentTimeMillis()
                startPerformExercise(false)
            }

            override fun onTick(millisUntilFinished: Long) {
                timeCountDown++
                if (readyToGoTime - timeCountDown >= 0) {
                    binding!!.tvCountDownReadyToGO.text = (readyToGoTime - timeCountDown).toString()
                    binding!!.progressBarReadyToGo.progress = timeCountDown

                    if (timeCountDown == readyToGoTime.toInt() / 2) {

                        val readyToGoText = "Please do that on a mat"

                        MyApplication.speechText(this@PerformWorkOutActivity, readyToGoText)

                    } else if ((readyToGoTime - timeCountDown) < 4) {
                        MyApplication.speechText(
                            this@PerformWorkOutActivity,
                            (readyToGoTime - timeCountDown).toString()
                        )
                    }
                } else {
                    timer!!.onFinish()
                    timer!!.cancel()
                }
            }

        }

        Handler().postDelayed(Runnable {
            timer!!.start()
        }, 1000)

    }

    fun startPerformExercise(isNeedDelay: Boolean) {

        workoutProgressIndicatorAdapter!!.setCompletedExercise(currentPos)
        if (currentPos > 0) {
            binding!!.imgPrevWorkout.visibility = View.VISIBLE
        } else {
            binding!!.imgPrevWorkout.visibility = View.GONE
        }

        if (currentExe!!.exUnit.equals(Constant.workout_type_step)) {

            binding!!.tvExcName.text = currentExe!!.exName
            binding!!.tvExcNameStep.text = currentExe!!.exName
            binding!!.tvTotalStep.text = currentExe!!.exTime!!
            binding!!.llReadyToGo.visibility = View.GONE
            binding!!.llAfterStartWithTime.visibility = View.GONE
            binding!!.llAfterStartWithSteps.visibility = View.VISIBLE

        } else {
            binding!!.tvExcName.text = currentExe!!.exName
            binding!!.llReadyToGo.visibility = View.GONE
            binding!!.llAfterStartWithTime.visibility = View.VISIBLE
            binding!!.llAfterStartWithSteps.visibility = View.GONE

            binding!!.progressBarWorkOut.max = currentExe!!.exTime!!.toInt()
            binding!!.progressBarWorkOut.progress = 0
            binding!!.tvCompletedSec.text = "${currentExe!!.exTime!!}"
            binding!!.tvTotalSec.text = " / ${currentExe!!.exTime!!}"
        }

        if (isNeedDelay) {
            val scaleAnimation: Animation = ScaleAnimation(
                1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            )
            scaleAnimation!!.setDuration(1000)
            scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    binding!!.tvAnimation.visibility = View.GONE
                }

                override fun onAnimationStart(animation: Animation?) {

                }

            })
            timer = object : CountDownTimerWithPause(4000, 1000, true) {
                override fun onFinish() {
                    timer?.cancel()
                    timer = null
                    binding!!.tvAnimation.visibility = View.GONE
                    if (currentExe!!.exUnit.equals(Constant.workout_type_step)) {
                        startExerciseWithStep()
                    } else {
                        startExerciseWithTime()
                    }
                    this@PerformWorkOutActivity.start()
                }

                override fun onTick(millisUntilFinished: Long) {
                    if ((millisUntilFinished / 1000) > 0) {
                        MyApplication.speechText(
                            this@PerformWorkOutActivity,
                            (millisUntilFinished / 1000).toString()
                        )
                        binding!!.tvAnimation!!.setText((millisUntilFinished / 1000).toString())
                        binding!!.tvAnimation.visibility = View.VISIBLE
                        binding!!.tvAnimation!!.startAnimation(scaleAnimation)
                    }
                }

            }
            timer!!.start()
        } else {
            start()
            if (currentExe!!.exUnit.equals(Constant.workout_type_step)) {
                startExerciseWithStep()
            } else {
                startExerciseWithTime()
            }
        }


    }

    private fun start() {
        /*val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 1000)*/
        exStartTime = System.currentTimeMillis()
        running = true
    }

    private fun startExerciseWithStep() {
        if (timer != null) {
            timer!!.cancel()
        }
        mySoundUtil.playSound(0)

    }

    private fun startExerciseWithTime() {
        if (timer != null) {
            timer!!.cancel()
        }
        mySoundUtil.playSound(0)

        try {
            countExercise()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val excTime = currentExe!!.exTime!!.toInt()
        val readyToGoText = "Start $excTime seconds ${currentExe!!.exName}"

        MyApplication.speechText(this, readyToGoText)
    }

    private fun countExercise() {

        var timeCountDown = 0

        val exerciseTime = currentExe!!.exTime!!.toInt()
        val halfTime = exerciseTime / 2

        timer =
            object : CountDownTimerWithPause(currentExe!!.exTime!!.toInt() * 1000L, 1000, true) {
                override fun onFinish() {
                    binding!!.tvCompletedSec.text = "0"
                    binding!!.progressBarWorkOut.progress = exerciseTime
                    onWorkoutTimeOver()
                }

                override fun onTick(millisUntilFinished: Long) {
                    timeCountDown++
                    if ((exerciseTime - timeCountDown) >= 0) {
                        binding!!.tvCompletedSec.text = (exerciseTime - timeCountDown).toString()
                        binding!!.progressBarWorkOut.progress = timeCountDown

                        if (timeCountDown == halfTime) {
                            MyApplication.speechText(this@PerformWorkOutActivity, "Half time")
                        } else if ((exerciseTime - timeCountDown) < 4) {
                            MyApplication.speechText(
                                this@PerformWorkOutActivity,
                                (exerciseTime - timeCountDown).toString()
                            )
                        }
                    } else {
                        timer!!.onFinish()
                        timer!!.cancel()
                    }
                }

            }

        Handler().postDelayed(Runnable {
            if (timer != null)
                timer!!.start()
        }, 1000)


    }

    private fun onWorkoutTimeOver() {
        stopTimer()

        DataHelper(this).updateCompleteHomeExByDayExId(currentExe!!.exId!!)
        if (currentPos == exercisesList!!.lastIndex) {
            // Go to Complete Screen
            MyApplication.speechText(this, "Congratulation")
            goToCompleteScreen()
        } else {
            goToRestScreen()
        }
    }

    private fun goToRestScreen() {
        mySoundUtil.playSound(mySoundUtil.SOUND_DING)
        val i = Intent(this, RestActivity::class.java)
        i.putExtra("nextEx", Gson().toJson(exercisesList!!.get(currentPos + 1)))
        i.putExtra("nextPos", currentPos + 2)
        i.putExtra("totalEx", exercisesList!!.size)
        startActivityForResult(i, 7029)
    }

    private fun goToCompleteScreen() {
        addShow()
            startCompleteActivity()



    }

    fun startCompleteActivity() {
        val i = Intent(this@PerformWorkOutActivity, CompletedActivity::class.java)
        i.putExtra("workoutPlanData", Gson().toJson(workoutPlanData))
        i.putExtra("ExcList", Gson().toJson(exercisesList))
        i.putExtra("duration", totalExTime)
        startActivity(i)
        finish()
    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
        initMusic(false)
        resumeTimer()
    }


    inner class ClickHandler {

        fun onWorkOutInfoClick() {
            //pauseTimer()
            val i = Intent(this@PerformWorkOutActivity, ExerciseVideoActivity::class.java)
            i.putExtra("workoutPlanData", Gson().toJson(currentExe))
            startActivity(i)
        }

        fun onVideoClick() {
            val i = Intent(this@PerformWorkOutActivity, ExerciseVideoActivity::class.java)
            i.putExtra("workoutPlanData", Gson().toJson(currentExe))
            startActivity(i)
        }

        fun onSoundClick() {
            pauseTimer()
            showSoundOptionDialogPerForm(this@PerformWorkOutActivity, object :
                DialogDismissListener {
                override fun onDialogDismiss() {
                    resumeTimer()
                }
            })
        }

        fun onReadyToGoClick() {

              val i = Intent(this@PerformWorkOutActivity, PauseBeforeStartActivity::class.java)
              i.putExtra("workoutPlanData", Gson().toJson(currentExe))
              i.putExtra("nextPos", currentPos + 1)
              i.putExtra("totalEx", exercisesList!!.size)
              startActivity(i)
        }

        fun onMusicClick() {
            if (binding!!.llMusic.visibility == View.VISIBLE) {
                binding!!.llMusic.visibility = View.INVISIBLE
            } else {
                binding!!.llMusic.visibility = View.VISIBLE
            }
        }

        fun onPauseMusicClick() {
            if (currMusic != null)
                playMusic()
            else {
                val i = Intent(this@PerformWorkOutActivity, MusicListActivity::class.java)
                startActivity(i)
            }
        }


        fun onPrevMusicClick() {
         
                MyApplication.prevMusic()
            
        }

        fun onNextMusicClick() {
          
                MyApplication.nextMusic()
            
        }

        fun onSelectMusicClick() {
            pauseTimer()
            val i = Intent(this@PerformWorkOutActivity, MusicListActivity::class.java)
            startActivity(i)
        }

        fun onSkipReadyToGoClick() {
            startPerformExercise(false)
        }

        fun onBackClick() {
            pauseTimer()
            showQuitDialog()
        }

        fun onNextExerciseClick() {
            stopTimer()
            onWorkoutTimeOver()
        }

        fun onPrevExerciseClick() {
            stopTimer()
            if (currentPos >= 1) {
                val i = Intent(this@PerformWorkOutActivity, RestActivity::class.java)
                i.putExtra("nextEx", Gson().toJson(exercisesList!!.get(currentPos - 1)))
                i.putExtra("nextPos", currentPos - 1)
                i.putExtra("totalEx", exercisesList!!.size)

                currentPos -= 2

                startActivityForResult(i, 7029)
            } else {
                totalExTime = 0
                currentPos = 0
                exStartTime = System.currentTimeMillis()
                currentTime = 0
                running = false
                initReadyToGo()
            }
        }

    }


    var dialogSoundOptionBindingPerForm: BottomSheetSoundOptionBinding? = null
    lateinit var dialogSoundOptionPerForm: BottomSheetDialog

    fun showSoundOptionDialogPerForm(mContext: Context, listner: DialogDismissListener) {
        val v: View = (mContext as Activity).getLayoutInflater()
            .inflate(R.layout.bottom_sheet_sound_option, null)
        dialogSoundOptionBindingPerForm = DataBindingUtil.bind(v)
        dialogSoundOptionPerForm = BottomSheetDialog(mContext)
        dialogSoundOptionPerForm.setContentView(v)

        dialogSoundOptionBindingPerForm!!.switchMute.isChecked =
            pref!!.getPref( Constant.PREF_IS_SOUND_MUTE, false)
        dialogSoundOptionBindingPerForm!!.switchCoachTips.isChecked =
            pref!!.getPref( Constant.PREF_IS_COACH_SOUND_ON, true)
        dialogSoundOptionBindingPerForm!!.switchVoiceGuide.isChecked =
            pref!!.getPref( Constant.PREF_IS_INSTRUCTION_SOUND_ON, true)

        dialogSoundOptionBindingPerForm!!.switchMute.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding!!.imgSound.setImageResource(R.drawable.ic_mute_sound)
                dialogSoundOptionBindingPerForm!!.switchCoachTips.isChecked = false
                dialogSoundOptionBindingPerForm!!.switchVoiceGuide.isChecked = false
                pref!!.setPref( Constant.PREF_IS_SOUND_MUTE, true)
            } else {
                binding!!.imgSound.setImageResource(R.drawable.ic_sound_round)
                pref!!.setPref( Constant.PREF_IS_SOUND_MUTE, false)
            }
        }

        dialogSoundOptionBindingPerForm!!.switchCoachTips.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dialogSoundOptionBindingPerForm!!.switchMute.isChecked = false
                pref!!.setPref( Constant.PREF_IS_COACH_SOUND_ON, true)
            } else {
                pref!!.setPref( Constant.PREF_IS_COACH_SOUND_ON, false)
            }
        }

        dialogSoundOptionBindingPerForm!!.switchVoiceGuide.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dialogSoundOptionBindingPerForm!!.switchMute.isChecked = false
                pref!!.setPref( Constant.PREF_IS_INSTRUCTION_SOUND_ON, true)
            } else {
                pref!!.setPref( Constant.PREF_IS_INSTRUCTION_SOUND_ON, false)
            }
        }

        dialogSoundOptionBindingPerForm!!.llDone.setOnClickListener {
            dialogSoundOptionPerForm.dismiss()
        }

        dialogSoundOptionPerForm.setOnDismissListener {
            listner.onDialogDismiss()
        }

        dialogSoundOptionPerForm.show()
    }

    private fun playMusic() {
        if (currMusic != null) {
            if (MyApplication.musicUtil == null || MyApplication.musicUtil!!.isPlaying.not()) {
                binding!!.imgPlayMusic.visibility = View.GONE
                binding!!.imgPauseMusic.visibility = View.VISIBLE
                binding!!.imgMusic.setImageResource(R.drawable.ic_music)
                MyApplication.playMusic(currMusic!!, this@PerformWorkOutActivity)
                pref!!.setPref( Constant.PREF_IS_MUSIC_MUTE, false)
            } else {
                binding!!.imgPlayMusic.visibility = View.VISIBLE
                binding!!.imgPauseMusic.visibility = View.GONE
                binding!!.imgMusic.setImageResource(R.drawable.ic_music_off)
                pref!!.setPref( Constant.PREF_IS_MUSIC_MUTE, true)
                MyApplication.stopMusic()
            }
        }
    }

    fun setPlayPauseView() {
        if (MyApplication.musicUtil == null || MyApplication.musicUtil!!.isPlaying.not()) {
            binding!!.imgPlayMusic.visibility = View.VISIBLE
            binding!!.imgPauseMusic.visibility = View.GONE
            binding!!.imgMusic.setImageResource(R.drawable.ic_music_off)
        } else {
            binding!!.imgPlayMusic.visibility = View.GONE
            binding!!.imgPauseMusic.visibility = View.VISIBLE
            binding!!.imgMusic.setImageResource(R.drawable.ic_music)
        }
    }

    override fun onPause() {
        super.onPause()
        pauseTimer()
    }



    override fun onDestroy() {
        super.onDestroy()
        MyApplication.stopMusic()
        stopTimer()
    }

    fun stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    fun resumeTimer() {
        if (timer != null && timer!!.isPaused) {
            timer!!.resume()
        }
        if (running.not()) {
            running = true
            exStartTime = System.currentTimeMillis()
        }

    }

    fun pauseTimer() {
        if (running) {
            running = false
            currentTime = System.currentTimeMillis() - exStartTime
            totalExTime += currentTime / 1000

        }

        if (timer != null && timer!!.isRunning) {
            timer!!.pause()

        }
    }

    lateinit var quiteDialog: Dialog
    fun showQuitDialog() {
        MyApplication.speechText(this, getString(R.string.quite_exercise_msg))
        quiteDialog = Dialog(getActivity())
        quiteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogQuiteWorkoutBinding =
            DataBindingUtil.inflate<DialogQuiteWorkoutBinding>(
                layoutInflater,
                R.layout.dialog_quite_workout, null, false
            )

        quiteDialog.setContentView(dialogQuiteWorkoutBinding.root)

        dialogQuiteWorkoutBinding!!.imgClose.setOnClickListener {
            resumeTimer()
            quiteDialog.dismiss()
        }

        dialogQuiteWorkoutBinding.btnContinue.setOnClickListener {
            resumeTimer()
            quiteDialog.dismiss()
        }

        dialogQuiteWorkoutBinding.btnQuit.setOnClickListener {
            quiteDialog.dismiss()
            saveData()
        }

        dialogQuiteWorkoutBinding.tvComeback.setOnClickListener {
            pref!!.setComBackIn30Min(this)
            quiteDialog.dismiss()
            finish()
        }

        quiteDialog.show()
        quiteDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    }


    private fun saveData() {



        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 7029 && resultCode == Activity.RESULT_OK) {
            if (data!!.hasExtra("restTime")) {
                totalExTime += data.getLongExtra("restTime", 0)
            }

            currentPos++
//            binding!!.progressBarTop.progress = currentPos
            currentExe = exercisesList!!.get(currentPos)
            loadWorkoutImage()
            if (data!!.hasExtra("isRestSkip")) {
                startPerformExercise(data!!.getBooleanExtra("isRestSkip", false))
            } else {
                startPerformExercise(false)
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