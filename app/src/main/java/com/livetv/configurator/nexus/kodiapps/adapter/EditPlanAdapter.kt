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
import com.livetv.configurator.nexus.kodiapps.databinding.ItemEditPlanBinding
import com.livetv.configurator.nexus.kodiapps.model.HomeExTableClass


class EditPlanAdapter(internal var context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val data = arrayListOf<HomeExTableClass>()
    internal var mEventListener: EventListener? = null

    fun getItem(pos: Int): HomeExTableClass {
        return data[pos]
    }
    internal var utils: Prefs? = null
    init {
        utils = Prefs(context)

    }
    fun addAll(mData: ArrayList<HomeExTableClass>) {
        try {
            data.clear()
            data.addAll(mData)

        } catch (e: Exception) {
            
        }
        notifyDataSetChanged()
    }

    fun add(item: HomeExTableClass) {

        try {
            this.data.add(item)

        } catch (e: Exception) {
            
        }

        notifyDataSetChanged()
    }

    fun onChangePosition(fromPos: Int, toPos: Int) {
        //Collections.swap(data, fromPos, toPos)
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val rowSideMenuBinding = DataBindingUtil.inflate<ItemEditPlanBinding>(
            inflater,
            R.layout.item_edit_plan, parent, false
        )

        return MyViewHolder(rowSideMenuBinding)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        val holder = viewHolder as MyViewHolder
        holder.rowSideMenuBinding.container.setOnClickListener {
            if (mEventListener != null) {
                mEventListener!!.onItemClick(position, holder.rowSideMenuBinding.root)
            }
        }
        holder.rowSideMenuBinding.imgReplace.setOnClickListener {
            if (mEventListener != null) {
                mEventListener!!.onReplaceClick(position, holder.rowSideMenuBinding.root)
            }
        }

        val item = getItem(position)

        holder.rowSideMenuBinding.tvName.text = item.exName

        if (item.replaceExId.isNullOrEmpty().not()) {
            holder.rowSideMenuBinding.imgReplaceMark.visibility = View.VISIBLE
        } else {
            holder.rowSideMenuBinding.imgReplaceMark.visibility = View.GONE
        }

        if (item.exUnit.equals(Constant.workout_type_step)) {
            holder.rowSideMenuBinding.tvTime.text = "X ${item.exTime}"
        } else {
            holder.rowSideMenuBinding.tvTime.text =
                utils!!.secToString(item.exTime!!.toInt(), Constant.WORKOUT_TIME_FORMAT)
        }

        holder.rowSideMenuBinding.container.setOnClickListener {
            if (mEventListener != null) {
                mEventListener!!.onItemClick(position, holder.rowSideMenuBinding.root)
            }
        }

        holder.rowSideMenuBinding.viewFlipper.removeAllViews()
        val listImg: ArrayList<String>? =
            utils!!.ReplaceSpacialCharacters(item.exPath!!)?.let { utils!!.getAssetItems(context, it) }

        if (listImg != null) {
            for (i in 0 until listImg.size) {
                val imgview = ImageView(context)
                //            Glide.with(mContext).load("//android_asset/burpee/".plus(i.toString()).plus(".png")).into(imgview)
                Glide.with(context).load(listImg.get(i)).into(imgview)
                imgview.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                holder.rowSideMenuBinding.viewFlipper.addView(imgview)
            }
        }

        holder.rowSideMenuBinding.viewFlipper.isAutoStart = true
        holder.rowSideMenuBinding.viewFlipper.setFlipInterval(context.resources.getInteger(R.integer.viewfliper_animation))
        holder.rowSideMenuBinding.viewFlipper.startFlipping()

    }


    override fun getItemCount(): Int {
        return data.size
    }

    inner class MyViewHolder(internal var rowSideMenuBinding: ItemEditPlanBinding) :
        RecyclerView.ViewHolder(rowSideMenuBinding.root)

    interface EventListener {
        fun onItemClick(position: Int, view: View)
        fun onReplaceClick(position: Int, view: View)
    }

    fun setEventListener(eventListener: EventListener) {
        this.mEventListener = eventListener
    }


}
