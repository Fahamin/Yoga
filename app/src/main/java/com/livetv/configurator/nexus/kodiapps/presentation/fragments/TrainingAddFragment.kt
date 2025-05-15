package com.livetv.configurator.nexus.kodiapps.presentation.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.MyTrainingAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.databinding.FragmentTrainingAddBinding
import com.livetv.configurator.nexus.kodiapps.model.HomePlanTableClass
import com.livetv.configurator.nexus.kodiapps.presentation.activity.AddExerciseActivity
import com.livetv.configurator.nexus.kodiapps.presentation.activity.MyTrainingExcListActivity

class TrainingAddFragment : BaseFragment() , CallbackListener {
    lateinit var binding: FragmentTrainingAddBinding
    lateinit var mContext: Context
    var myTrainingAdapter: MyTrainingAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_training_add,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        this.mContext = binding.root.context
        binding!!.handler = ClickHandler()
        binding!!.topbar.tvTitleText.text = getString(R.string.menu_my_training)

        myTrainingAdapter = MyTrainingAdapter(mContext)
        binding!!.rvMyTraining.layoutManager = LinearLayoutManager(mContext)
        binding!!.rvMyTraining.setAdapter(myTrainingAdapter)

        myTrainingAdapter!!.setEventListener(object : MyTrainingAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                val item = myTrainingAdapter!!.getItem(position)
                val i = Intent(mContext, MyTrainingExcListActivity::class.java)
                i.putExtra("workoutPlanData", Gson().toJson(item))
                startActivity(i)

            }

            override fun onMoreClick(position: Int, view: View) {
                val item = myTrainingAdapter!!.getItem(position)
                showPopupMenu(view, item)
            }

        })

        fillData()
    }

    @SuppressLint("RestrictedApi")
    fun fillData() {
        myTrainingAdapter!!.addAll(dbHelper!!.getHomePlanList(Constant.PlanTypeMyTraining))

        if (myTrainingAdapter!!.itemCount > 0) {
            binding.llPlaceHolder.visibility = View.GONE
            binding.imgAddNew.visibility = View.VISIBLE
        } else {
            binding.llPlaceHolder.visibility = View.VISIBLE
            binding.imgAddNew.visibility = View.GONE
        }
    }

    private fun showPopupMenu(
        view: View,
        planDetail: HomePlanTableClass
    ) {
        val menu = PopupMenu(mContext, view)

        menu.menu.add(getString(R.string.rename))
        val s = SpannableString(getString(R.string.delete))
        s.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.setting_color_red)),
            0,
            s.length,
            0
        )
        menu.menu.add(s)

        menu.setOnMenuItemClickListener { item ->
            if (item!!.title is SpannableString) {
                showDeleteConfirmationDialog(planDetail)
            } else {
                showTrainingPlanNameDialog(planDetail)
            }
            true
        }

        menu.show()
    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
        fillData()
    }

    private fun showDeleteConfirmationDialog(planDetail: HomePlanTableClass) {
        val builder = AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
        builder.setCancelable(false)
        builder.setMessage(getString(R.string.delete_confirmation_msg))
        builder.setPositiveButton(R.string.delete) { _, _ ->
            dbHelper!!.deleteMyTrainingPlan(planDetail.planId!!)
            fillData()
        }
        builder.setNegativeButton(R.string.btn_cancel) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showTrainingPlanNameDialog(item: HomePlanTableClass) {
        val builder = AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
        builder.setCancelable(false)
        builder.setTitle(R.string.please_name_your_plan)

        val dialogLayout = layoutInflater.inflate(R.layout.dialog_edit_text, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editText)

        editText.setText(item!!.planName)

        builder.setView(dialogLayout)

        builder.setPositiveButton(R.string.btn_ok) { dialog, which ->
            dbHelper!!.updateMyTrainingPlanName(item.planId!!, editText.text.toString())
            fillData()
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.btn_cancel, { dialog, which -> dialog.dismiss() })
        builder.create().show()
    }

    inner class ClickHandler {

        fun onAddNewClick() {
            val i = Intent(requireContext(), AddExerciseActivity::class.java)
            i.putExtra("from_new_training", false)
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