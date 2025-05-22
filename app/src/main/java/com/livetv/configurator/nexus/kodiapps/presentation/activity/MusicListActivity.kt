package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.MusicListAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Fun
import com.livetv.configurator.nexus.kodiapps.core.MyApplication
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityMusicListBinding
import com.livetv.configurator.nexus.kodiapps.model.Music

class MusicListActivity : BaseActivity(), CallbackListener {

    var binding: ActivityMusicListBinding? = null
    var adapter: MusicListAdapter? = null
    var musicList: ArrayList<Music>? = null
    var currMusic: Music? = null
    lateinit var pref: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_music_list)

        pref = Prefs(this)
         Fun(this)
        val adContainerView = findViewById<FrameLayout>(R.id.ad_view_container)
        Fun.showBanner(this, adContainerView)
        initIntentParam()
        init()
    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
    }
    private fun initIntentParam() {
        try {
            /*if (intent.extras != null) {
                if (intent.extras!!.containsKey("workoutPlanData")) {
                    val str = intent.getStringExtra("workoutPlanData")
                    workoutPlanData = Gson().fromJson(str, object :
                        TypeToken<HomeTrainingPlans.Plan>() {}.type)!!
                }
            }*/

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        binding!!.handler = ClickHandler()
        binding!!.rvMusic.layoutManager = LinearLayoutManager(this)
        adapter = MusicListAdapter(this)
        binding!!.rvMusic.adapter = adapter
        musicList = dbHelper.getMusicList()
        adapter!!.addAll(musicList!!)

        adapter!!.setEventListener(object : MusicListAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                val item = adapter!!.getItem(position)

             
                    currMusic = item
                    binding!!.tvMusicName.text = item.name
                    binding!!.imgPlayMusic.setImageResource(R.drawable.ic_play_bar_btn_pause)
                    MyApplication.playMusic(item, this@MusicListActivity)
                    pref!!.setPref( Constant.PREF_IS_MUSIC_SELECTED, true)
                    pref!!.setPref( Constant.PREF_MUSIC, Gson().toJson(item))
                
            }

        })

        if (pref!!.getPref( Constant.PREF_IS_MUSIC_SELECTED, false)) {
            val str = pref!!.getPref( Constant.PREF_MUSIC, "")
            if (str.isNullOrEmpty().not()) {
                currMusic = Gson().fromJson<Music>(str, object : TypeToken<Music>() {}.type)
                binding!!.tvMusicName.text = currMusic!!.name

            } else {
                setDefaultMusic()
            }
        } else {
            setDefaultMusic()
        }

        if (MyApplication.musicUtil != null && MyApplication.musicUtil!!.isPlaying) {
            binding!!.imgPlayMusic.setImageResource(R.drawable.ic_play_bar_btn_pause)
        }

        val isShuffle = pref!!.getPref( Constant.PREF_IS_MUSIC_SHUFFLE, false)
        val isRepeat = pref!!.getPref( Constant.PREF_IS_MUSIC_REPEAT, false)

        if (isRepeat) {
            binding!!.imgRepeat.setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            binding!!.imgRepeat.setColorFilter(
                ContextCompat.getColor(this, R.color.gray_light_),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        if (isShuffle) {
            binding!!.imgSuffle.setColorFilter(
                ContextCompat.getColor(this, R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            binding!!.imgSuffle.setColorFilter(
                ContextCompat.getColor(this, R.color.gray_light_),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }

    fun setDefaultMusic() {
        for (item in musicList!!) {

                currMusic = item
                binding!!.tvMusicName.text = item.name
                return

        }
    }

    inner class ClickHandler {

        fun onCloseClick() {
            finish()
        }

        fun onPauseClick() {
            if (MyApplication.musicUtil == null || MyApplication.musicUtil!!.isPlaying.not()) {
                binding!!.imgPlayMusic.setImageResource(R.drawable.ic_play_bar_btn_pause)
                MyApplication.playMusic(currMusic!!, this@MusicListActivity)
                pref!!.setPref( Constant.PREF_IS_MUSIC_MUTE, false)
                pref!!.setPref( Constant.PREF_IS_MUSIC_SELECTED, true)
                pref!!.setPref( Constant.PREF_MUSIC, Gson().toJson(currMusic))
            } else {
                binding!!.imgPlayMusic.setImageResource(R.drawable.ic_music_play)
                pref!!.setPref( Constant.PREF_IS_MUSIC_MUTE, true)
                MyApplication.stopMusic()
            }
        }

        fun onSuffleClick() {

                val isShuffle =
                    pref!!.getPref( Constant.PREF_IS_MUSIC_SHUFFLE, false)
                if (isShuffle) {
                    pref!!.setPref( Constant.PREF_IS_MUSIC_SHUFFLE, false)
                    binding!!.imgSuffle.setColorFilter(
                        ContextCompat.getColor(
                            this@MusicListActivity,
                            R.color.gray_light_
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                } else {
                    binding!!.imgSuffle.setColorFilter(
                        ContextCompat.getColor(
                            this@MusicListActivity,
                            R.color.white
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    pref!!.setPref( Constant.PREF_IS_MUSIC_SHUFFLE, true)
                }

        }

        fun onRepeatClick() {

                val isRepeat =
                    pref!!.getPref( Constant.PREF_IS_MUSIC_REPEAT, false)
                if (isRepeat) {
                    pref!!.setPref( Constant.PREF_IS_MUSIC_REPEAT, false)
                    binding!!.imgRepeat.setColorFilter(
                        ContextCompat.getColor(
                            this@MusicListActivity,
                            R.color.gray_light_
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                } else {
                    binding!!.imgRepeat.setColorFilter(
                        ContextCompat.getColor(
                            this@MusicListActivity,
                            R.color.white
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    pref!!.setPref( Constant.PREF_IS_MUSIC_REPEAT, true)

            }
        }

        fun onNextClick() {

                for (i in musicList!!.indices) {
                    Log.e("TAG", "onNextClick::::: " + musicList!![i].name + "  " + musicList!![i].fileName + "  " + musicList!!.size)
                }
                MyApplication.nextMusic()

        }

        fun onPrevClick() {

            MyApplication.prevMusic()

        }
    }

    override fun onSuccess() {

    }

    override fun onCancel() {

    }

    override fun onRetry() {

    }

}
