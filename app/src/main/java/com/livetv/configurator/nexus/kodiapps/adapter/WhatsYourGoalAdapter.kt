package com.livetv.configurator.nexus.kodiapps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.databinding.ItemWhatsYourGoalBinding
import com.livetv.configurator.nexus.kodiapps.presentation.activity.WhatsYourGoalActivity


class WhatsYourGoalAdapter(internal var context: Context) :
    RecyclerView.Adapter<WhatsYourGoalAdapter.MyViewHolder>(){

    private val data = mutableListOf<WhatsYourGoalActivity.Goals>()
    internal var mEventListener: EventListener? = null

    fun getItem(pos: Int): WhatsYourGoalActivity.Goals {
        return data[pos]
    }

    fun addAll(mData: ArrayList<WhatsYourGoalActivity.Goals>) {
        try {
            data.clear()
            data.addAll(mData)

        } catch (e: Exception) {
        }
        notifyDataSetChanged()
    }

    fun add(item: WhatsYourGoalActivity.Goals) {

        try {
            this.data.add(item)

        } catch (e: Exception) {
        }

        notifyDataSetChanged()
    }

    fun changeSelection(pos:Int)
    {
        for (i in data.indices)
        {
            data[i].isSelected = pos==i
        }

        notifyDataSetChanged()
    }

    fun getSelectedItem(): WhatsYourGoalActivity.Goals? {
        for (i in data.indices)
        {
            if(data[i].isSelected)
            {
                return data[i]
            }
        }

        return null
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val rowSideMenuBinding = DataBindingUtil.inflate<ItemWhatsYourGoalBinding>(
            inflater,
            R.layout.item_whats_your_goal, parent, false
        )
        return MyViewHolder(rowSideMenuBinding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)

        holder.rowSideMenuBinding.tvTitle.text = item.name
        holder.rowSideMenuBinding.ivIcon.setImageResource(item.img)

        if (item.isSelected) {
//            holder.rowSideMenuBinding.container.setBackgroundColor(ContextCompat.getColor(context,R.color.box_select))
            holder.rowSideMenuBinding.container.background = ContextCompat.getDrawable(context,R.drawable.bg_whats_goal_select)
        }
        else {
//            holder.rowSideMenuBinding.container.setBackgroundColor(ContextCompat.getColor(context,R.color.colorSettingbg))
            holder.rowSideMenuBinding.container.background = ContextCompat.getDrawable(context,R.drawable.bg_whats_goal_unselect)
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

    inner class MyViewHolder(internal var rowSideMenuBinding: ItemWhatsYourGoalBinding) :
        RecyclerView.ViewHolder(rowSideMenuBinding.root)

    interface EventListener {
        fun onItemClick(position: Int, view: View)
    }

    fun setEventListener(eventListener: EventListener) {
        this.mEventListener = eventListener
    }

}
