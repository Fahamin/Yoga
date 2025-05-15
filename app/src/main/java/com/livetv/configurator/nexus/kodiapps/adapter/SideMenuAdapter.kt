package com.livetv.configurator.nexus.kodiapps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.databinding.ItemSideMenuBinding
import com.livetv.configurator.nexus.kodiapps.model.SideMenuItem

class SideMenuAdapter(private val context: Context) :
    RecyclerView.Adapter<SideMenuAdapter.ViewHolder>() {

    private var data: MutableList<SideMenuItem> = ArrayList()
    private lateinit var mEventListener: EventListener

    interface EventListener {
        fun onItemClick(pos: Int)
    }
    internal var utils: Prefs? = null

    init {
        //        this.data.addAll(data);
        utils = Prefs(context)
    }
    fun addAll(mData: List<SideMenuItem>) {
        this.data.clear()
        this.data.addAll(mData)
        notifyDataSetChanged()
    }

    fun add(mData: SideMenuItem) {
        this.data.add(mData)
        notifyDataSetChanged()
    }

    fun getItem(pos: Int): SideMenuItem {
        return data[pos]
    }

    fun changeSelection(position: Int) {
        for (i in data.indices) {
            data[i].selected = position == i
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemSideMenuBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(private val binding: ItemSideMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SideMenuItem) {
            binding.tvMenuName.text = item.name
            binding.imgMenuIcon.setImageResource(item.icon)

            if (item.selected) {
                binding.container.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.side_menu_selected_bg_color
                    )
                )
            } else {
                binding.container.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
            }

            binding.container.setOnClickListener {
                if (::mEventListener.isInitialized) {
                    mEventListener.onItemClick(adapterPosition)
                }
            }
        }
    }

    fun setEventListener(eventListener: EventListener) {
        mEventListener = eventListener
    }
}
