package com.livetv.configurator.nexus.kodiapps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.databinding.ItemReplaceExerciesBinding
import com.livetv.configurator.nexus.kodiapps.model.ExTableClass


class ReplaceExerciseAdapter(internal var context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val data = mutableListOf<ExTableClass>()
    internal var mEventListener: EventListener? = null


    private val utils : Prefs

    init {
        utils = Prefs(context)

    }


    fun getItem(pos: Int): ExTableClass {
        return data[pos]
    }

    fun addAll(mData: ArrayList<ExTableClass>) {
        try {
            data.clear()
            data.addAll(mData)

        } catch (e: Exception) {
        }
        notifyDataSetChanged()
    }

    fun add(item: ExTableClass) {

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
        val rowSideMenuBinding = DataBindingUtil.inflate<ItemReplaceExerciesBinding>(
            inflater,
            R.layout.item_replace_exercies, parent, false
        )

        return MyViewHolder(rowSideMenuBinding)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        val holder = viewHolder as MyViewHolder
        val item = getItem(position)

        holder.rowSideMenuBinding.tvName.text = item.exName

        if (item.exUnit.equals(Constant.workout_type_step)) {
            if (item.exReplaceTime.isNullOrEmpty())
                holder.rowSideMenuBinding.tvTime.text = "X ${item.exTime}"
            else
                holder.rowSideMenuBinding.tvTime.text = "X ${item.exReplaceTime}"
        } else {
            if (item.exReplaceTime.isNullOrEmpty() && item.exTime.isNullOrEmpty().not())
                holder.rowSideMenuBinding.tvTime.text = utils.secToString(
                    item.exTime!!.trim().toInt(),
                    Constant.WORKOUT_TIME_FORMAT
                )
            else if(item.exReplaceTime.isNullOrEmpty().not())
                holder.rowSideMenuBinding.tvTime.text = utils.secToString(
                    item.exReplaceTime!!.trim().toInt(),
                    Constant.WORKOUT_TIME_FORMAT
                )
        }

        holder.rowSideMenuBinding.container.setOnClickListener {
            if (mEventListener != null) {
                mEventListener!!.onItemClick(position, holder.rowSideMenuBinding.root)
            }
        }

        holder.rowSideMenuBinding.viewFlipper.removeAllViews()
        val listImg: ArrayList<String>? =
            utils.ReplaceSpacialCharacters(item.exPath!!)?.let { utils.getAssetItems(context, it) }

        if (listImg != null) {
            for (i in 0 until listImg.size) {
                val imgview = ImageView(context)
                Glide.with(context).load(listImg.get(i)).into(imgview)
                imgview.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                holder.rowSideMenuBinding.viewFlipper.addView(imgview)
            }
        }

        holder.rowSideMenuBinding.viewFlipper.isAutoStart = true
        holder.rowSideMenuBinding.viewFlipper.flipInterval = context.resources.getInteger(R.integer.viewfliper_animation)
        holder.rowSideMenuBinding.viewFlipper.startFlipping()

    }


    override fun getItemCount(): Int {
        return data.size
    }

    inner class MyViewHolder(internal var rowSideMenuBinding: ItemReplaceExerciesBinding) :
        RecyclerView.ViewHolder(rowSideMenuBinding.root)

    interface EventListener {
        fun onItemClick(position: Int, view: View)
    }

    fun setEventListener(eventListener: EventListener) {
        this.mEventListener = eventListener
    }


}
