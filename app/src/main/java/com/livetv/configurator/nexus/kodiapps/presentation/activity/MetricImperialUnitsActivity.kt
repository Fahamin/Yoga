package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.AdUtils
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.core.interfaces.TopBarClickListener
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityMetricImperialUnitsBinding

class MetricImperialUnitsActivity : BaseActivity(), CallbackListener {

    var binding: ActivityMetricImperialUnitsBinding? = null
    lateinit var pref: Prefs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_metric_imperial_units)
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


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        binding!!.topbar.isBackShow = true
        binding!!.topbar.tvTitleText.text = getString(R.string.metric_and_imperial_units)
        binding!!.topbar.topBarClickListener = TopClickListener()
        binding!!.handler = ClickHandler()

        binding!!.tvHeightUnit.text = pref.getPref( Constant.PREF_HEIGHT_UNIT, Constant.DEF_CM)
        binding!!.tvWeightUnit.text = pref.getPref( Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG)

    }




    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
    }


    inner class ClickHandler {

        fun onWeightUnitClick(){
            showWeightUnitDialog()
        }

        fun onHeightUnitClick(){
            showHeightUnitDialog()
        }
    }

    private fun showWeightUnitDialog() {

        val weightUnits = arrayOf<CharSequence>("Kg", "Lbs")

        var checkedItem = 0
        if ( pref.getPref( Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG).equals(Constant.DEF_LB)) {
            checkedItem = 1
        }

        val builder = AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
        builder.setTitle(R.string.weight_unit)
        builder.setSingleChoiceItems(weightUnits,checkedItem,object :DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {

                if (which == 0) {
                    pref.setPref( Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG)
                } else {
                    pref.setPref(

                        Constant.PREF_WEIGHT_UNIT,
                        Constant.DEF_LB
                    )
                }

                binding!!.tvWeightUnit.text = pref.getPref(Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG)
                if (dialog != null) {
                    dialog.dismiss()
                }
            }

        })

        builder.create().show()
    }

    private fun showHeightUnitDialog() {

        val heightUnits = arrayOf<CharSequence>("Cm", "In")

        var checkedItem = 0
        if ( pref.getPref( Constant.PREF_HEIGHT_UNIT, Constant.DEF_CM).equals(Constant.DEF_IN)) {
            checkedItem = 1
        }

        val builder = AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle)
        builder.setTitle(R.string.height_unit)
        builder.setSingleChoiceItems(heightUnits,checkedItem,object :DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                if (which == 0) {
                    pref.setPref( Constant.PREF_HEIGHT_UNIT, Constant.DEF_CM)
                } else {
                    pref.setPref( Constant.PREF_HEIGHT_UNIT, Constant.DEF_IN)
                }
                binding!!.tvHeightUnit.text = pref.getPref( Constant.PREF_HEIGHT_UNIT, Constant.DEF_CM)
                if (dialog != null) {
                    dialog.dismiss()
                }
            }

        })

        builder.create().show()
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