package com.livetv.configurator.nexus.kodiapps.presentation.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.MyApplication
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.core.interfaces.DialogDismissListener
import com.livetv.configurator.nexus.kodiapps.databinding.DialogSelectTtsEngineBinding
import com.livetv.configurator.nexus.kodiapps.databinding.DialogSetDurationBinding
import com.livetv.configurator.nexus.kodiapps.databinding.DialogTestVoiceFailBinding
import com.livetv.configurator.nexus.kodiapps.databinding.FragmentSettingBinding
import com.livetv.configurator.nexus.kodiapps.presentation.activity.MetricImperialUnitsActivity
import com.livetv.configurator.nexus.kodiapps.presentation.activity.ReminderActivity

class SettingFragment : BaseFragment(), CallbackListener {
    lateinit var binding: FragmentSettingBinding
    lateinit var mContext: Context
    var pref: Prefs? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_setting,container,false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pref = Prefs(requireContext())
        
        init()
    }


    private fun init() {
        this.mContext = binding.root.context
        binding.handler = ClickHandler()
        binding.topbar.tvTitleText.text = getString(R.string.menu_setting)
        binding.SwitchKeepTheScreenOn.setOnCheckedChangeListener { buttonView, isChecked ->
            pref!!.setPref( Constant.PREF_IS_KEEP_SCREEN_ON,isChecked)
        }

        fillData()

    }


    private fun fillData() {
        binding.tvRestTime.text = pref!!.getPref(mContext, Constant.PREF_REST_TIME, Constant.DEFAULT_REST_TIME).toString().plus(" secs")
        binding.tvCountDownTime.text = pref!!.getPref(mContext,
            Constant.PREF_READY_TO_GO_TIME,
            Constant.DEFAULT_READY_TO_GO_TIME).toString().plus(" secs")

        binding.SwitchKeepTheScreenOn.isChecked =  pref!!.getPref(
            Constant.PREF_IS_KEEP_SCREEN_ON,false)


    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
    }


    inner class ClickHandler {

        fun onSetDurationClick() {
            showTrainingRestDialog()
        }

        fun onCountDownClick() {
            showSetCountDownTimeDialog()
        }

        fun onSoundOptionClick() {
            showSoundOptionDialog(mContext,object : DialogDismissListener {
                override fun onDialogDismiss() {

                }

            })
        }

        fun onTestVoiceClick(){
            MyApplication.speechText(mContext, getString(R.string.did_you_hear_test_voice))
            Thread.sleep(1000)
            showVoiceConfirmationDialog()
        }

        fun onSelectTTsEngineClick(){
            showSelectTTSEngineDialog()
        }



        fun onDeviceTTSSettingClick(){
            val intent = Intent("com.android.settings.TTS_SETTINGS")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        fun onRemindMeTomorrow(){
            val intent = Intent(mContext, ReminderActivity::class.java)
            intent.putExtra("from", Constant.FROM_SETTING)
            startActivity(intent)
        }



        fun onMetricImperialsClick(){
            val intent = Intent(mContext, MetricImperialUnitsActivity::class.java)
            startActivity(intent)
        }


        fun onRatUsClick(){
            pref!!.rateUs(mContext)
        }

        fun onFeedBackClick(){
            pref!!.contactUs(mContext)
        }

        fun onPrivacyPolicyClick()
        {
            pref!!.openUrl(mContext, getString(R.string.privacy_policy_link))
        }

        fun onShareWithFriendClick()
        {

            val link = "https://play.google.com/store/apps/details?id="
            val strSubject = ""
            val strText = "I'm stretching with ${getString(R.string.app_name)}, it's so useful! All kinds of " +
                    "stretching and warm-up routines here for workout, running, " +
                    "pain relief, etc. It must have one you need, try it for free!" +
                    "\n\n" +
                    "Download the app:$link"

            pref!!.shareStringLink(mContext, strSubject, strText)
        }
    }

    private fun showVoiceConfirmationDialog() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
        val dialog : Dialog
        builder.setMessage(R.string.did_you_hear_test_voice)
        builder.setPositiveButton(R.string.yes) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.dismiss()
            showTestVoiceFailDialog()
        }
        dialog =  builder.create()
        dialog.show()
    }

    private fun showTestVoiceFailDialog() {

        val builder = AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
        var dialog : Dialog? = null
        val v: View = (mContext as Activity).layoutInflater
            .inflate(R.layout.dialog_test_voice_fail, null)
        val dialogBinding: DialogTestVoiceFailBinding? = DataBindingUtil.bind(v)
        builder.setView(v)

        dialogBinding!!.tvDownloadTTSEngine.setOnClickListener {
            pref!!.DownloadTTSEngine(mContext)
            dialog!!.dismiss()
        }

        dialogBinding.tvSelectTTSEngine.setOnClickListener {
            showSelectTTSEngineDialog()
            dialog!!.dismiss()
        }

        builder.setView(v)
        dialog =  builder.create()
        dialog.show()
    }

    private fun showSelectTTSEngineDialog() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
        var dialog : Dialog? = null
        val v: View = (mContext as Activity).layoutInflater
            .inflate(R.layout.dialog_select_tts_engine, null)
        builder.setView(v)
        builder.setTitle(getString(R.string.please_choose_guide_voice_engine))
        val dialogBinding: DialogSelectTtsEngineBinding? = DataBindingUtil.bind(v)

        dialogBinding!!.llContainer.setOnClickListener {
            dialog!!.dismiss()
        }

        builder.setView(v)
        dialog =  builder.create()
        dialog.show()

    }

    private fun showTrainingRestDialog() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
        builder.setCancelable(false)
        builder.setTitle("Set Duration (5 ~ 180 secs)")
        val v: View = (mContext as Activity).getLayoutInflater()
            .inflate(R.layout.dialog_set_duration, null)
        val dialogBinding: DialogSetDurationBinding? = DataBindingUtil.bind(v)
        builder.setView(v)

        dialogBinding!!.tvSeconds.text = pref!!.getPref(mContext, Constant.PREF_REST_TIME, Constant.DEFAULT_REST_TIME).toString()

        dialogBinding.imgNext.setOnClickListener {
            val sec = dialogBinding.tvSeconds.text.toString().toInt()
            if (sec < 180)
                dialogBinding.tvSeconds.text = (sec+1).toString()
        }

        dialogBinding.imgPrev.setOnClickListener {
            val sec = dialogBinding.tvSeconds.text.toString().toInt()
            if (sec > 5)
                dialogBinding.tvSeconds.text = (sec-1).toString()
        }

        builder.setPositiveButton(R.string.save) { dialog, _ ->
            pref!!.setPref( Constant.PREF_REST_TIME,dialogBinding.tvSeconds.text.toString().toLong())
            fillData()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.btn_cancel) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }


    private fun showSetCountDownTimeDialog() {
        val builder = AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
        builder.setCancelable(false)
        builder.setTitle("Set Duration (10 ~ 15 secs)")
        val v: View = (mContext as Activity).getLayoutInflater()
            .inflate(R.layout.dialog_set_duration, null)
        val dialogBinding: DialogSetDurationBinding? = DataBindingUtil.bind(v)
        builder.setView(v)

        dialogBinding!!.tvSeconds.text = pref!!.getPref(mContext,
            Constant.PREF_READY_TO_GO_TIME,
            Constant.DEFAULT_READY_TO_GO_TIME).toString()

        dialogBinding.imgNext.setOnClickListener {
            val sec = dialogBinding.tvSeconds.text.toString().toInt()
            if (sec < 15)
                dialogBinding.tvSeconds.text = (sec+1).toString()
        }

        dialogBinding.imgPrev.setOnClickListener {
            val sec = dialogBinding.tvSeconds.text.toString().toInt()
            if (sec > 10)
                dialogBinding.tvSeconds.text = (sec-1).toString()
        }

        builder.setPositiveButton(R.string.save) { dialog, _ ->
            pref!!.setPref(
                Constant.PREF_READY_TO_GO_TIME,dialogBinding.tvSeconds.text.toString().toLong())
            fillData()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.btn_cancel) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }



    override fun onSuccess() {

    }

    override fun onCancel() {

    }

    override fun onRetry() {

    }

}