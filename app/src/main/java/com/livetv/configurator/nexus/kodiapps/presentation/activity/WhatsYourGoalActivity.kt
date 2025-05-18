package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.livetv.configurator.nexus.kodiapps.presentation.activity.MainActivity
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.WhatsYourGoalAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityWhatsYourGoalBinding

class WhatsYourGoalActivity : AppCompatActivity() {
    lateinit var utils: Prefs

    var binding: ActivityWhatsYourGoalBinding? = null
    var whatsYourGoalAdapter: WhatsYourGoalAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_whats_your_goal)
        utils = Prefs(this)

        init()
    }


    private fun init() {

        binding!!.mRecyclerView!!.setLayoutManager(LinearLayoutManager(this))
        whatsYourGoalAdapter = WhatsYourGoalAdapter(this)
        binding!!.mRecyclerView!!.setAdapter(whatsYourGoalAdapter)
        whatsYourGoalAdapter!!.setEventListener(object : WhatsYourGoalAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                whatsYourGoalAdapter!!.changeSelection(position)
                binding!!.btnNext.visibility = View.VISIBLE
            }

        })

        binding!!.btnNext.setOnClickListener {

            if (whatsYourGoalAdapter!!.getSelectedItem() != null) {
                utils.setPref(

                    Constant.PREF_WHATS_YOUR_GOAL,
                    whatsYourGoalAdapter!!.getSelectedItem()!!.name!!
                )
            }

            if (utils.getPref( Constant.PREF_IS_REMINDER_SET, false)) {

//                val i = Intent(this, HomeActivity::class.java)
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)

            } else {
                val i = Intent(this, SetReminderActivity::class.java)
                startActivity(i)
            }
        }

        addData()
    }

    private fun addData() {
        var list = arrayListOf<Goals>()

        list.add(Goals(getString(R.string.goal_warm_up), R.drawable.icon_warm_ups))
        list.add(Goals(getString(R.string.goal_cool_down), R.drawable.icon_cool_downs))
        list.add(Goals(getString(R.string.goal_pain_relief), R.drawable.icon_pain_relief))
        list.add(Goals(getString(R.string.goal_body_relax), R.drawable.icon_body_relax))
        list.add(
            Goals(
                getString(R.string.goal_posture_correction),
                R.drawable.icon_posture_correction
            )
        )

        whatsYourGoalAdapter!!.addAll(list)
    }



    class Goals {
        constructor(name: String, img: Int) {
            this.name = name
            this.img = img
        }

        var name: String? = null
        var img: Int = 0
        var isSelected = false
    }

}