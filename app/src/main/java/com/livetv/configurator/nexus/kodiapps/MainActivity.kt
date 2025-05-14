package com.livetv.configurator.nexus.kodiapps

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.livetv.configurator.nexus.kodiapps.adapter.ViewPagerAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.databinding.ActivityMainBinding
import com.livetv.configurator.nexus.kodiapps.presentation.fragments.DiscoverFragment
import com.livetv.configurator.nexus.kodiapps.presentation.fragments.MyTrainingFragment
import com.livetv.configurator.nexus.kodiapps.presentation.fragments.ReportsFragment
import com.livetv.configurator.nexus.kodiapps.presentation.fragments.SettingFragment
import com.livetv.configurator.nexus.kodiapps.presentation.fragments.TrainingAddFragment


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var vPagerAdapter: ViewPagerAdapter
    private var selectedPage = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()

        binding.vPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                binding.vPager.currentItem = position
                binding.bottomBarNav.show(position + 1, true)
            }
        })
        initBottomBarNavigationMain(binding.bottomBarNav)
        initParam()
    }

    fun initParam() {
        try {
            if (intent.extras != null) {
                if (intent.extras!!.containsKey(Constant.FROMMYTRAININGFRAGMENT)) {
                    selectedPage = intent.getIntExtra(Constant.FROMMYTRAININGFRAGMENT, 0)
                    binding.vPager.currentItem = selectedPage
                    binding.bottomBarNav.show(selectedPage + 1, true)
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
        vPagerAdapter.addFrag(TrainingAddFragment(), getString(R.string.menu_my_training))
        vPagerAdapter.addFrag(ReportsFragment(), getString(R.string.menu_report))
        vPagerAdapter.addFrag(SettingFragment(), getString(R.string.menu_setting))

        binding.vPager.adapter = vPagerAdapter
        binding.vPager.offscreenPageLimit = vPagerAdapter.count
        binding.vPager.currentItem = selectedPage

    }

    private fun initBottomBarNavigationMain(bottomBarNavigationBinding: MeowBottomNavigation) {
        bottomBarNavigationBinding.add(
            MeowBottomNavigation.Model(
                1,
                R.drawable.ic_menu_training_plan
            )
        )
        bottomBarNavigationBinding.add(
            MeowBottomNavigation.Model(
                2,
                R.drawable.ic_menu_library
            )
        )
        bottomBarNavigationBinding.add(
            MeowBottomNavigation.Model(
                3,
                R.drawable.ic_baseline_add_24
            )
        )
        bottomBarNavigationBinding.add(
            MeowBottomNavigation.Model(
                4,
                R.drawable.ic_menu_report
            )
        )
        bottomBarNavigationBinding.add(
            MeowBottomNavigation.Model(
                5,
                R.drawable.ic_menu_setting
            )
        )
        bottomBarNavigationBinding.show(1, true)
        bottomBarNavigationBinding.setOnClickMenuListener {
            Log.e("TAG", "initBottomBarNavigation:::: ${it.id}")
            binding.vPager.currentItem = it.id - 1

        }



    }

}