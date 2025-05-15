package com.livetv.configurator.nexus.kodiapps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.databinding.ItemRecentBinding
import com.livetv.configurator.nexus.kodiapps.db.DataHelper
import com.livetv.configurator.nexus.kodiapps.model.HistoryDetailsClass


class RecentAdapter(internal var context: Context) :
    RecyclerView.Adapter<RecentAdapter.MyViewHolder>(){

    private val data = mutableListOf<HistoryDetailsClass>()
    internal var mEventListener: EventListener? = null
    private val dbHelper = DataHelper(context)

    fun getItem(pos: Int): HistoryDetailsClass {
        return data[pos]
    }
    internal var utils: Prefs? = null

    init {
        //        this.data.addAll(data);
        utils = Prefs(context)
    }
    fun addAll(mData: ArrayList<HistoryDetailsClass>) {
        try {
            data.clear()
            data.addAll(mData)

        } catch (e: Exception) {
            
        }
        notifyDataSetChanged()
    }

    fun add(item: HistoryDetailsClass) {

        try {
            this.data.add(item)

        } catch (e: Exception) {
            
        }

        notifyDataSetChanged()
    }


    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val rowSideMenuBinding = DataBindingUtil.inflate<ItemRecentBinding>(
            inflater,
            R.layout.item_recent, parent, false)
        return MyViewHolder(rowSideMenuBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val item = getItem(position)

        holder.rowSideMenuBinding.tvRecentWorkOutName.text = item.PlanName
        holder.rowSideMenuBinding.imgRecentWorkout.setImageResource(utils!!.getDrawableId(item.planDetail!!.planThumbnail,context))


        if (item.planDetail!!.planDays.equals(Constant.PlanDaysYes))
        {
            val plandays = dbHelper.getDaysPlanData(item.DayId)
            holder.rowSideMenuBinding.tvTime.text = plandays!!.Minutes.plus(" Mins")

        }else{
            holder.rowSideMenuBinding.tvTime.text = item.planDetail!!.planMinutes.plus(" Mins")
        }

        if(item.planDetail!!.planType.equals(Constant.PlanTypeMyTraining)){
            holder.rowSideMenuBinding.tvTime.visibility = View.GONE
        }else {
            holder.rowSideMenuBinding.tvTime.visibility = View.VISIBLE
        }

        holder.rowSideMenuBinding.container.setOnClickListener {
            if (mEventListener != null) {
                mEventListener!!.onItemClick(position, holder.rowSideMenuBinding.root)
            }
        }

    }


    override fun getItemCount(): Int {
        return data.size
    }

    inner class MyViewHolder(internal var rowSideMenuBinding: ItemRecentBinding) :
        RecyclerView.ViewHolder(rowSideMenuBinding.root)

    interface EventListener {
        fun onItemClick(position: Int, view: View)
    }

    fun setEventListener(eventListener: EventListener) {
        this.mEventListener = eventListener
    }

}
