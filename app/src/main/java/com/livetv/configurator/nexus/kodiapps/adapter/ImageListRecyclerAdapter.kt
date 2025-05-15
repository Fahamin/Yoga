package com.livetv.configurator.nexus.kodiapps.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.livetv.configurator.nexus.kodiapps.databinding.ItemCustomGalleryBinding
import com.livetv.configurator.nexus.kodiapps.model.CustomGallery
import java.io.File
import java.util.*

class ImageListRecyclerAdapter(private val mContext: Context) :
    RecyclerView.Adapter<ImageListRecyclerAdapter.VerticalItemHolder>() {

    var data = ArrayList<CustomGallery>()
    var isMultiSelected: Boolean = false
        private set
    var mEventListener: EventListener? = null

    override fun getItemCount(): Int {
        return data.size
    }

    val selected: ArrayList<CustomGallery>
        get() {
            val dataT = ArrayList<CustomGallery>()
            for (i in data.indices) {
                if (data[i].isSeleted) {
                    dataT.add(data[i])
                }
            }
            return dataT
        }

    val totalSelected: Int
        get() {
            var totalSelected = 0
            for (i in data.indices) {
                if (data[i].isSeleted) {
                    totalSelected += 1
                }
            }
            return totalSelected
        }

    interface EventListener {
        fun onItemClickListener(position: Int)
    }

    fun addAll(files: ArrayList<CustomGallery>) {
        try {
            this.data.clear()
            this.data.addAll(files)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        notifyDataSetChanged()
    }

    fun isSelected(position: Int): Boolean {
        return data[position].isSeleted
    }

    fun changeSelection(position: Int) {
        data[position].isSeleted = !data[position].isSeleted
        notifyItemChanged(position)
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    fun setMultiplePick(isMultiplePick: Boolean) {
        this.isMultiSelected = isMultiplePick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalItemHolder {
        val binding = ItemCustomGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VerticalItemHolder(binding)
    }

    override fun onBindViewHolder(holder: VerticalItemHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            mEventListener?.onItemClickListener(position)
        }
    }

    fun getItem(position: Int): CustomGallery {
        return data[position]
    }

    inner class VerticalItemHolder(private val binding: ItemCustomGalleryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CustomGallery) {
            Glide.with(mContext)
                .load(File(item.sdcardPath))
                .into(binding.imgImage)

            binding.imgSelected.visibility = if (item.isSeleted) View.VISIBLE else View.GONE
        }
    }

    fun setEventListener(eventListener: EventListener) {
        mEventListener = eventListener
    }
}
