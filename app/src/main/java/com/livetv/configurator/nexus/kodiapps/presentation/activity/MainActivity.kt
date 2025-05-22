package com.livetv.configurator.nexus.kodiapps.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.ViewPagerAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Fun
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityMainBinding
import com.livetv.configurator.nexus.kodiapps.presentation.fragments.DiscoverFragment
import com.livetv.configurator.nexus.kodiapps.presentation.fragments.MyTrainingFragment
import com.livetv.configurator.nexus.kodiapps.presentation.fragments.ReportsFragment
import com.livetv.configurator.nexus.kodiapps.presentation.fragments.SettingFragment

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var vPagerAdapter: ViewPagerAdapter
    private var selectedPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Fun(this)
        setupViewPager()

        binding.vPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                binding.bottomBarNav.selectedItemId = when(position) {
                    0 -> R.id.menu_my_training
                    1 -> R.id.menu_discover
                    else -> R.id.menu_setting
                }
            }
        })

        binding.bottomBarNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.menu_my_training -> binding.vPager.currentItem = 0
                R.id.menu_discover -> binding.vPager.currentItem = 1
                R.id.menu_setting -> binding.vPager.currentItem = 3
            }
            true
        }

        initParam()
    }

    fun initParam() {
        try {
            if (intent.extras != null && intent.extras!!.containsKey(Constant.FROMMYTRAININGFRAGMENT)) {
                selectedPage = intent.getIntExtra(Constant.FROMMYTRAININGFRAGMENT, 0)
                binding.vPager.currentItem = selectedPage
                binding.bottomBarNav.selectedItemId = when(selectedPage) {
                    0 -> R.id.menu_my_training
                    1 -> R.id.menu_discover
                    else -> R.id.menu_setting
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupViewPager() {
        vPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        vPagerAdapter.addFrag(MyTrainingFragment(), getString(R.string.menu_my_training))
        vPagerAdapter.addFrag(DiscoverFragment(), getString(R.string.menu_discover))
        vPagerAdapter.addFrag(SettingFragment(), getString(R.string.menu_setting))

        binding.vPager.adapter = vPagerAdapter
        binding.vPager.offscreenPageLimit = vPagerAdapter.count
        binding.vPager.currentItem = selectedPage
    }
}