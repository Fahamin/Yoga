package com.livetv.configurator.nexus.kodiapps.presentation.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.MiseUtils.isOnline
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.core.interfaces.DialogDismissListener
import com.livetv.configurator.nexus.kodiapps.databinding.BottomSheetSoundOptionBinding
import com.livetv.configurator.nexus.kodiapps.databinding.DialogDobBinding
import com.livetv.configurator.nexus.kodiapps.databinding.DialogGenderDobBinding
import com.livetv.configurator.nexus.kodiapps.db.DataHelper
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt

open class BaseFragment : Fragment() {

    internal lateinit var toast: Toast
    var dbHelper: DataHelper? = null

    lateinit var utils: Prefs
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toast = Toast.makeText(activity, "", Toast.LENGTH_LONG)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        utils = Prefs(requireContext())
        dbHelper = DataHelper(requireContext())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item != null) {
            when (item.itemId) {
                android.R.id.home -> {


                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showToast(text: String, duration: Int) {
        requireActivity().runOnUiThread {
            toast.setText(text)
            toast.duration = duration
            toast.show()
        }
    }

    fun showToast(text: String) {
        requireActivity().runOnUiThread {
            toast.setText(text)
            toast.duration = Toast.LENGTH_SHORT
            toast.show()
        }
    }


    fun showHeightWeightDialog(listner: DialogDismissListener) {
        val builder = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
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

        var editWeightStr: String = ""
        var editHeightCMStr: String = ""
        var editHeightFTStr: String = ""
        var editHeightInStr: String = ""


        /*For weight*/
        editWeight.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (editWeight.text.toString().equals("0.0 KG") || editWeight.text.toString()
                        .equals(
                            "0.0 LB"
                        )
                ) {
                    editWeight.setText("")
                } else {
                    editWeight.setText(editWeight.text.toString())
                }
                editWeight.setSelection(editWeight.text!!.length)
            } else {
                if (utils.getPref( Constant.PREF_LAST_INPUT_WEIGHT, 0f)
                        .toString() == "0.0"
                ) {
                    if (utils.getPref(

                            Constant.CHECK_LB_KG,
                            Constant.DEF_KG
                        ) == Constant.DEF_KG
                    ) {
                        if (editWeightStr.isEmpty()) {
                            editWeight.setText("0.0 KG")
                        } else {
                            editWeight.setText(editWeight.text.toString())
                        }
                    } else {
                        if (editWeightStr.isEmpty()) {
                            editWeight.setText("0.0 LB")
                        } else {
                            editWeight.setText(editWeight.text.toString())
                        }
                        editWeight.setSelection(editWeight.text!!.length)
                    }

                } else {
                    editWeight.setText(
                        utils.getPref( Constant.PREF_LAST_INPUT_WEIGHT, 0f)
                            .toString()
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
                } else {
                    editHeightCM.setText(editHeightCM.text.toString())
                }
//                editHeightCM.setSelection(editHeightCM.text!!.length)
            } else {
                if (utils.getPref( Constant.PREF_LAST_INPUT_FOOT, 0)
                        .toString() == "0"
                    && utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0f).toString()
                        .equals(
                            "0.0"
                        )
                ) {
                    if (editHeightCMStr.isEmpty()) {
                        editHeightCM.setText("0.0 CM")
                    } else {
                        editHeightCM.setText(editHeightCM.text.toString())
                    }
                    editHeightCM.setSelection(editHeightCM.text!!.length)
                } else {
                    val inch = utils.ftInToInch(
                        utils.getPref( Constant.PREF_LAST_INPUT_FOOT, 0),
                        utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0F)
                            .toDouble()
                    )
                    editHeightCM.setText(utils.inchToCm(inch).roundToInt().toDouble().toString())
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
                } else {
                    editHeightFT.setText(editHeightFT.text.toString())
                }

                editHeightFT.setSelection(editHeightFT.text!!.length)

            } else {
                if (utils.getPref( Constant.PREF_LAST_INPUT_FOOT, 0)
                        .toString() == "0"
                ) {

                    if (editHeightFTStr.isEmpty()) {
                        editHeightFT.setText("0.0 FT")
                    } else {
                        editHeightFT.setText(editHeightFT.text.toString())
                    }
                    editHeightFT.setSelection(editHeightFT.text!!.length)
                } else {
                    editHeightFT.setText(
                        utils.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString()
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
                } else {
                    editHeightIn.setText(editHeightIn.text.toString())
                }
                editHeightIn.setSelection(editHeightIn.text!!.length)
            } else {
                if (utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0f)
                        .toString() == "0.0"
                ) {

                    if (editHeightInStr.isEmpty()) {
                        editHeightIn.setText("0.0 IN")
                    } else {
                        editHeightIn.setText(editHeightIn.text.toString())
                    }
                    editHeightIn.setSelection(editHeightIn.text!!.length)
                } else {
                    editHeightIn.setText(
                        utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0f)
                            .toString()
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
            if (utils.getPref(
                    Constant.CHECK_LB_KG,
                    Constant.DEF_KG
                ) == Constant.DEF_KG
            ) {

                editWeight.setText(
                    utils.getPref( Constant.PREF_LAST_INPUT_WEIGHT, 0f).toString()
                )

                editWeight.setSelection(editWeight.text!!.length)

                tvKG.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvLB.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))

                tvKG.isSelected = true
                tvLB.isSelected = false
            } else {
                boolKg = false

                editWeight.setText(
                    utils.kgToLb(
                        utils.getPref(

                            Constant.PREF_LAST_INPUT_WEIGHT,
                            0f
                        ).toDouble()
                    ).toString()
                )

                editWeight.setSelection(editWeight.text!!.length)
                tvKG.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))
                tvLB.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                tvKG.isSelected = false
                tvLB.isSelected = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Log.e(
            "TAG", "showHeightWeightDialog:::Check LbKg:::::::: " + utils.getPref(

                Constant.CHECK_LB_KG,
                Constant.DEF_LB
            )
        )
        boolInch = true
        try {
            if (utils.getPref(

                    Constant.CHECK_LB_KG,
                    Constant.DEF_KG
                ) == Constant.DEF_LB
            ) {

                editHeightCM.visibility = View.GONE
                llInch.visibility = View.VISIBLE

                tvIN.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvCM.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))

                tvIN.isSelected = true
                tvCM.isSelected = false

                /*if (utils.getPref(Constant.PREF_LAST_INPUT_FOOT,0).toString().equals("0")){
                    editHeightFT.setText("0.0 FT")
                }else{
                    editHeightFT.setText(utils.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString())
                }

                if (utils.getPref(Constant.PREF_LAST_INPUT_INCH,0).toString().equals("0")){
                    editHeightIn.setText("0.0 IN")
                }else{
                    editHeightIn.setText(utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0F).toString())
                }*/
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)
            } else {
                boolInch = false

                editHeightCM.visibility = View.VISIBLE
                llInch.visibility = View.GONE

                tvIN.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))
                tvCM.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                tvIN.isSelected = false
                tvCM.isSelected = true

                /* if (utils.getPref(Constant.PREF_LAST_INPUT_FOOT,0).toString().equals("0")
                         && utils.getPref(Constant.PREF_LAST_INPUT_INCH,0).toString().equals("0")){

                 }else{

                 }*/
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)

                /*val inch = utils.ftInToInch(
                        utils.getPref( Constant.PREF_LAST_INPUT_FOOT, 0),
                        utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0F).toDouble()
                )

                editHeightCM.setText(utils.inchToCm(inch).roundToInt().toDouble().toString())*/
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        tvKG.setOnClickListener {
            try {

                utils.setPref( Constant.CHECK_LB_KG, Constant.DEF_KG)
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)
                if (boolInch) {
                    boolInch = false

                    editHeightCM.visibility = View.VISIBLE
                    llInch.visibility = View.GONE

                    tvIN.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))
                    tvCM.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                    tvIN.isSelected = false
                    tvCM.isSelected = true

                    var inch = 0.0
                    if (editHeightFT.text.toString() != "" && editHeightFT.text.toString() != "") {
                        inch = utils.ftInToInch(
                            editHeightFT.text.toString().toInt(),
                            editHeightIn.text.toString().toDouble()
                        )
                    } else if (editHeightFT.text.toString() != "" && editHeightIn.text.toString() == "") {
                        inch = utils.ftInToInch(editHeightFT.text.toString().toInt(), 0.0)
                    } else if (editHeightFT.text.toString() == "" && editHeightIn.text.toString() != "") {
                        inch = utils.ftInToInch(1, editHeightIn.text.toString().toDouble())
                    }

                    editHeightCM.setText(utils.inchToCm(inch).roundToInt().toDouble().toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                if (!boolKg) {
                    boolKg = true

                    tvKG.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    tvLB.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))

                    tvKG.isSelected = true
                    tvLB.isSelected = false

//                    editWeight.hint = Constant.DEF_KG

                    if (editWeight.text.toString() != "") {
                        editWeight.setText(
                            utils.lbToKg(editWeight.text.toString().toDouble()).toString()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        tvLB.setOnClickListener {
            try {
                utils.setPref(Constant.CHECK_LB_KG, Constant.DEF_LB)
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)
                if (boolKg) {
                    boolKg = false


                    tvKG.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))
                    tvLB.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                    tvKG.isSelected = false
                    tvLB.isSelected = true

//                    editWeight.hint = Constant.DEF_LB

                    if (editWeight.text.toString() != "") {
                        editWeight.setText(
                            utils.kgToLb(editWeight.text.toString().toDouble()).toString()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                editHeightCM.visibility = View.GONE
                llInch.visibility = View.VISIBLE

                tvIN.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvCM.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))

                tvIN.isSelected = true
                tvCM.isSelected = false

                try {
                    if (!boolInch) {
                        boolInch = true

                        if (editHeightCM.text.toString() != "") {
                            val inch = utils.cmToInch(editHeightCM.text.toString().toDouble())
                            editHeightFT.setText(utils.calcInchToFeet(inch).toString())
                            editHeightIn.setText(utils.calcInFromInch(inch).toString())
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
                utils.setPref( Constant.CHECK_LB_KG, Constant.DEF_KG)
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)
                editWeight.setSelection(editWeight.text!!.length)
                if (boolInch) {
                    boolInch = false

                    editHeightCM.visibility = View.VISIBLE
                    llInch.visibility = View.GONE

                    tvIN.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))
                    tvCM.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                    tvIN.isSelected = false
                    tvCM.isSelected = true

                    if (editHeightIn.text.toString()
                            .equals("0.0 IN") || editHeightFT.text.toString().equals(
                            "0.0 FT"
                        )
                    ) {
                        editHeightCM.setText("0.0 CM")
                    } else {
                        var inch = 0.0
                        if (editHeightFT.text.toString() != "" && editHeightIn.text.toString() != "") {
                            inch = utils.ftInToInch(
                                editHeightFT.text.toString().toInt(),
                                editHeightIn.text.toString().toDouble()
                            )
                        } else if (editHeightFT.text.toString() != "" && editHeightIn.text.toString() == "") {
                            inch = utils.ftInToInch(editHeightFT.text.toString().toInt(), 0.0)
                        } else if (editHeightFT.text.toString() == "" && editHeightIn.text.toString() != "") {
                            inch = utils.ftInToInch(1, editHeightIn.text.toString().toDouble())
                        }

                        editHeightCM.setText(
                            utils.inchToCm(inch).roundToInt().toDouble().toString()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                if (!boolKg) {
                    boolKg = true

                    tvKG.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    tvLB.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))

                    tvKG.isSelected = true
                    tvLB.isSelected = false

//                    editWeight.hint = Constant.DEF_KG

                    if (editWeight.text.toString() != "") {
                        editWeight.setText(
                            utils.lbToKg(editWeight.text.toString().toDouble()).toString()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        tvIN.setOnClickListener {
            try {
                utils.setPref( Constant.CHECK_LB_KG, Constant.DEF_LB)
                checkLbKg(editWeight, editHeightCM, editHeightFT, editHeightIn)
                if (boolKg) {
                    boolKg = false


                    tvKG.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))
                    tvLB.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                    tvKG.isSelected = false
                    tvLB.isSelected = true

//                    editWeight.hint = Constant.DEF_LB

                    if (editWeight.text.toString() != "") {
                        editWeight.setText(
                            utils.kgToLb(editWeight.text.toString().toDouble()).toString()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                editHeightCM.visibility = View.GONE
                llInch.visibility = View.VISIBLE

                tvIN.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvCM.setTextColor(ContextCompat.getColor(requireContext(), R.color.col_666))

                tvIN.isSelected = true
                tvCM.isSelected = false

                try {
                    if (!boolInch) {
                        boolInch = true

                        if (editHeightCM.text.toString() != "") {
                            val inch = utils.cmToInch(editHeightCM.text.toString().toDouble())
                            editHeightFT.setText(utils.calcInchToFeet(inch).toString())
                            editHeightIn.setText(utils.calcInFromInch(inch).toString())
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

        val dob = utils.getPref( Constant.PREF_DOB, "")
        var posBtnText = ""
        if (dob.isNullOrEmpty()) {
            posBtnText = getString(R.string.next)
        } else {
            posBtnText = getString(R.string.set)
        }

        editWeight.setOnClickListener {
            if (editWeight.text.toString().equals("0.0 KG") || editWeight.text.toString()
                    .equals("0.0 LB")
            ) {
                editWeight.setText("")
            }
        }
        builder.setPositiveButton(posBtnText) { dialog, which ->

            try {
                if (boolInch) {
                    utils.setPref(
                        Constant.PREF_LAST_INPUT_FOOT,
                        editHeightFT.text.toString().toInt()
                    )
                    utils.setPref(
                        Constant.PREF_LAST_INPUT_INCH,
                        editHeightIn.text.toString().toFloat()
                    )
                    utils.setPref( Constant.PREF_HEIGHT_UNIT, Constant.DEF_IN)

                } else {
                    val inch = utils.cmToInch(editHeightCM.text.toString().toDouble())
                    utils.setPref(
                        Constant.PREF_LAST_INPUT_FOOT,
                        utils.calcInchToFeet(inch)
                    )
                    utils.setPref(
                        Constant.PREF_LAST_INPUT_INCH,
                        utils.calcInFromInch(inch).toFloat()
                    )
                    utils.setPref( Constant.PREF_HEIGHT_UNIT, Constant.DEF_CM)

                }


                val strKG: Float
                if (boolKg) {
                    strKG = editWeight.text.toString().toFloat()
                    utils.setPref( Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG)
                    utils.setPref( Constant.PREF_LAST_INPUT_WEIGHT, strKG)
                } else {
                    strKG =
                        utils.lbToKg(editWeight.text.toString().toDouble()).roundToInt().toFloat()
                    utils.setPref( Constant.PREF_WEIGHT_UNIT, Constant.DEF_LB)
                    utils.setPref( Constant.PREF_LAST_INPUT_WEIGHT, strKG)
                }

                val currentDate = utils.parseTime(Date().time, Constant.WEIGHT_TABLE_DATE_FORMAT)

                if (dbHelper!!.weightExistOrNot(currentDate)) {
                    dbHelper!!.updateWeight(currentDate, strKG.toString(), "")
                } else {
                    dbHelper!!.addUserWeight(strKG.toString(), currentDate, "")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (dob.isNullOrEmpty()) {
                showGenderDOBDialog(requireContext(), listner)
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


    fun showGenderDOBDialog(
        mContext: Context,
        listner: DialogDismissListener
    ) {
        val dialogbinding: DialogGenderDobBinding = DataBindingUtil.inflate(
            (mContext as Activity).layoutInflater,
            R.layout.dialog_gender_dob,
            null,
            false
        )

        val builder = AlertDialog.Builder(mContext)
            .setView(dialogbinding.root)

        if (utils.getPref( Constant.PREF_GENDER, "").isNullOrEmpty().not()) {
            if (utils.getPref( Constant.PREF_GENDER, "")
                    .equals(Constant.FEMALE)
            ) {
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

                val dob = utils.parseTime("$day-$month-$year", "dd-mm-yyyy")

                if (dob < Date()) {

                    utils.setPref( Constant.PREF_DOB, "$day-$month-$year")
                    if (dialogbinding.tvMale.isSelected)
                        utils.setPref( Constant.PREF_GENDER, Constant.MALE)

                    if (dialogbinding.tvFemale.isSelected)
                        utils.setPref( Constant.PREF_GENDER, Constant.FEMALE)

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


    open fun setSelectedDate() {
        var today = Calendar.getInstance()
        if (utils.getPref( Constant.PREF_DOB, "").isNullOrEmpty().not()) {
            val date = utils.parseTime(
                utils.getPref( Constant.PREF_DOB, "")!!,
                "dd-mm-yyyy"
            )
            today.time = date
        }
        yearPos = today[Calendar.YEAR] - minYear
        monthPos = today[Calendar.MONTH]
        dayPos = today[Calendar.DAY_OF_MONTH] - 1
    }


    private fun checkLbKg(
        editWeight: EditText,
        editHeightCM: EditText,
        editHeightFT: EditText,
        editHeightIn: EditText
    ) {

        /*for weight*/

        if (utils.getPref( Constant.PREF_LAST_INPUT_WEIGHT, 0f).toString()
                .equals("0.0")
            && utils.getPref( Constant.CHECK_LB_KG, Constant.DEF_KG)
                .equals(Constant.DEF_KG)
        ) {
            editWeight.setText("0.0 KG")
        } else if (utils.getPref( Constant.PREF_LAST_INPUT_WEIGHT, 0f).toString()
                .equals("0.0")
            && utils.getPref( Constant.CHECK_LB_KG, Constant.DEF_LB)
                .equals(Constant.DEF_LB)
        ) {
            editWeight.setText("0.0 LB")
        }
        editWeight.setSelection(editWeight.text!!.length)

        /*for height cm*/
        Log.e(
            "TAG",
            "checkLbKg:::::FIFIIF " + utils.getPref(

                Constant.PREF_LAST_INPUT_FOOT,
                0
            ) + " " +
                    utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0f)
        )

        if (utils.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString().equals("0")
            && utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0f).toString()
                .equals("0.0")
        ) {
            editHeightCM.setText("0.0 CM")
        } else {
            val inch = utils.ftInToInch(
                utils.getPref( Constant.PREF_LAST_INPUT_FOOT, 0),
                utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0F).toDouble()
            )
            Log.e(
                "TAG", "checkLbKg:::::FIFIIF " + utils.getPref(

                    Constant.PREF_LAST_INPUT_FOOT,
                    0
                ) + " " +
                        utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0f)
            )
            editHeightCM.setText(utils.inchToCm(inch).roundToInt().toDouble().toString())
        }


        /*for height ft*/
        if (utils.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString()
                .equals("0")
        ) {
            editHeightFT.setText("0.0 FT")
        } else {
            editHeightFT.setText(
                utils.getPref( Constant.PREF_LAST_INPUT_FOOT, 0).toString()
            )
        }


        /*for height in*/
        if (utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0f).toString()
                .equals("0.0")
        ) {
            editHeightIn.setText("0.0 IN")
        } else {
            editHeightIn.setText(
                utils.getPref( Constant.PREF_LAST_INPUT_INCH, 0f).toString()
            )
        }

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
            utils.getPref( Constant.PREF_IS_SOUND_MUTE, false)
        dialogSoundOptionBinding!!.switchCoachTips.isChecked =
            utils.getPref( Constant.PREF_IS_COACH_SOUND_ON, true)
        dialogSoundOptionBinding!!.switchVoiceGuide.isChecked =
            utils.getPref( Constant.PREF_IS_INSTRUCTION_SOUND_ON, true)

        dialogSoundOptionBinding!!.switchMute.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dialogSoundOptionBinding!!.switchCoachTips.isChecked = false
                dialogSoundOptionBinding!!.switchVoiceGuide.isChecked = false
                utils.setPref( Constant.PREF_IS_SOUND_MUTE, true)
            } else {
                utils.setPref( Constant.PREF_IS_SOUND_MUTE, false)
            }
        }

        dialogSoundOptionBinding!!.switchCoachTips.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dialogSoundOptionBinding!!.switchMute.isChecked = false
                utils.setPref( Constant.PREF_IS_COACH_SOUND_ON, true)
            } else {
                utils.setPref( Constant.PREF_IS_COACH_SOUND_ON, false)
            }
        }

        dialogSoundOptionBinding!!.switchVoiceGuide.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                dialogSoundOptionBinding!!.switchMute.isChecked = false
                utils.setPref( Constant.PREF_IS_INSTRUCTION_SOUND_ON, true)
            } else {
                utils.setPref( Constant.PREF_IS_INSTRUCTION_SOUND_ON, false)
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
    fun openInternetDialog(callbackListener: CallbackListener) {
        if (!isOnline(requireContext())) {
            val builder = AlertDialog.Builder(requireContext())
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

            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }
}
