package com.livetv.configurator.nexus.kodiapps.presentation.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.DurationAdapter
import com.livetv.configurator.nexus.kodiapps.adapter.PainReliefPagerAdapter
import com.livetv.configurator.nexus.kodiapps.adapter.PostureCorrectionAdapter
import com.livetv.configurator.nexus.kodiapps.adapter.TrainingGoalAdapter
import com.livetv.configurator.nexus.kodiapps.core.AdUtils
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.AdsCallback
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.databinding.FragmentDiscoverBinding
import com.livetv.configurator.nexus.kodiapps.model.HomePlanTableClass
import com.livetv.configurator.nexus.kodiapps.presentation.activity.DiscoverDetailActivity
import com.livetv.configurator.nexus.kodiapps.presentation.activity.ExercisesListActivity
import com.livetv.configurator.nexus.kodiapps.presentation.activity.MyTrainingActivity
import java.util.Date

class DiscoverFragment : BaseFragment() , CallbackListener {
    lateinit var binding: FragmentDiscoverBinding
    var painReliefPagerAdapter: PainReliefPagerAdapter? = null
    var flexibilityViewPagerAdapter: PainReliefPagerAdapter? = null
    var beginnerViewPagerAdapter: PainReliefPagerAdapter? = null
    var fatBurningViewPagerAdapter: PainReliefPagerAdapter? = null
    var trainingGoalAdapter: TrainingGoalAdapter? = null
    var bodyFocusAdapter: TrainingGoalAdapter? = null
    var durationAdapter: DurationAdapter? = null
    var postureCorrectionAdapter: PostureCorrectionAdapter? = null
    var onClickAd = 1
    lateinit var mContext: Context
    var randomPlan: HomePlanTableClass? = null
    lateinit var pref: Prefs


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_discover,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = Prefs(requireContext())
        initView()
    }

    private fun initView() {
        init()
    }

    private fun init() {
        this.mContext = binding.root.context
        binding.handler = ClickHandler()
        binding.topbar.tvTitleText.text = getString(R.string.menu_discover)
        painReliefPagerAdapter = PainReliefPagerAdapter(mContext, 2)
        binding.painReliefViewPager.offscreenPageLimit = painReliefPagerAdapter!!.count
        binding.painReliefViewPager.adapter = painReliefPagerAdapter
        binding.painReliefViewPager.clipToPadding = false
        binding.painReliefViewPager.measure(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        binding.painReliefViewPager.setPadding(0, 0, 110, 0)

        painReliefPagerAdapter!!.setEventListener(object : PainReliefPagerAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                val item = painReliefPagerAdapter!!.getItem(position)
                if (!item.isPro) {
                    if (onClickAd == Constant.FIRST_CLICK_COUNT && Constant.FIRST_CLICK_COUNT != 0) {
                        if (Constant.AD_TYPE_FB_GOOGLE == Constant.AD_GOOGLE) {
                            AdUtils!!.loadGoogleFullAd(mContext, object : AdsCallback {
                                override fun startNextScreenAfterAd() {
                                    startNextScreenpainRelief(position)
                                }
                            })
                        } else if (Constant.AD_TYPE_FB_GOOGLE == Constant.AD_FACEBOOK) {
                            AdUtils!!.loadFacebookFullAd(mContext, object : AdsCallback {
                                override fun startNextScreenAfterAd() {
                                    startNextScreenpainRelief(position)
                                }
                            })
                        } else {
                            startNextScreenpainRelief(position)
                        }
                        onClickAd = 1
                    } else {
                        startNextScreenpainRelief(position)
                        onClickAd += 1
                    }
                } else {
                    startNextScreenpainRelief(position)
                }
            }

        })

        flexibilityViewPagerAdapter = PainReliefPagerAdapter(mContext, 2)
        binding!!.flexibilityViewPager.offscreenPageLimit = flexibilityViewPagerAdapter!!.count
        binding!!.flexibilityViewPager.adapter = flexibilityViewPagerAdapter
        binding!!.flexibilityViewPager.setClipToPadding(false)
        binding!!.flexibilityViewPager.measure(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        binding!!.flexibilityViewPager.setPadding(0, 0, 110, 0)

        flexibilityViewPagerAdapter!!.setEventListener(object :
            PainReliefPagerAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                val item = flexibilityViewPagerAdapter!!.getItem(position)
                if (!item.isPro) {
                    if (onClickAd == Constant.FIRST_CLICK_COUNT && Constant.FIRST_CLICK_COUNT != 0) {
                        if (Constant.AD_TYPE_FB_GOOGLE == Constant.AD_GOOGLE) {
                            AdUtils!!.loadGoogleFullAd(mContext, object : AdsCallback {
                                override fun startNextScreenAfterAd() {
                                    startNextScreenflexibility(position)
                                }
                            })
                        } else if (Constant.AD_TYPE_FB_GOOGLE == Constant.AD_FACEBOOK) {
                            AdUtils!!.loadFacebookFullAd(mContext, object : AdsCallback {
                                override fun startNextScreenAfterAd() {
                                    startNextScreenflexibility(position)
                                }
                            })
                        } else {
                            startNextScreenflexibility(position)
                        }
                        onClickAd = 1
                    } else {
                        startNextScreenflexibility(position)
                        onClickAd += 1
                    }
                } else {
                    startNextScreenflexibility(position)
                }
            }

        })

        beginnerViewPagerAdapter = PainReliefPagerAdapter(mContext, 2)
        binding!!.forBeginnerViewPager.offscreenPageLimit = beginnerViewPagerAdapter!!.count
        binding!!.forBeginnerViewPager.adapter = beginnerViewPagerAdapter
        binding!!.forBeginnerViewPager.setClipToPadding(false)
        binding!!.forBeginnerViewPager.measure(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        binding!!.forBeginnerViewPager.setPadding(0, 0, 110, 0)

        beginnerViewPagerAdapter!!.setEventListener(object : PainReliefPagerAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {

                val item = beginnerViewPagerAdapter!!.getItem(position)

                if (!item.isPro) {
                    if (onClickAd == Constant.FIRST_CLICK_COUNT && Constant.FIRST_CLICK_COUNT != 0) {
                        if (Constant.AD_TYPE_FB_GOOGLE == Constant.AD_GOOGLE) {
                            AdUtils!!.loadGoogleFullAd(mContext, object : AdsCallback {
                                override fun startNextScreenAfterAd() {
                                    startNextScreembeginner(position)
                                }
                            })
                        } else if (Constant.AD_TYPE_FB_GOOGLE == Constant.AD_FACEBOOK) {
                            AdUtils!!.loadFacebookFullAd(mContext, object : AdsCallback {
                                override fun startNextScreenAfterAd() {
                                    startNextScreembeginner(position)
                                }
                            })
                        } else {
                            startNextScreembeginner(position)
                        }
                        onClickAd = 1
                    } else {
                        startNextScreembeginner(position)
                        onClickAd += 1
                    }
                } else {
                    startNextScreembeginner(position)
                }
            }

        })

        fatBurningViewPagerAdapter = PainReliefPagerAdapter(mContext, 2)
        binding!!.fatBurningViewPager!!.offscreenPageLimit = fatBurningViewPagerAdapter!!.count
        binding!!.fatBurningViewPager!!.adapter = fatBurningViewPagerAdapter
        binding!!.fatBurningViewPager.setClipToPadding(false)
        binding!!.fatBurningViewPager.measure(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        binding!!.fatBurningViewPager.setPadding(0, 0, 110, 0)

        fatBurningViewPagerAdapter!!.setEventListener(object :
            PainReliefPagerAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {

                val item = fatBurningViewPagerAdapter!!.getItem(position)
                if (!item.isPro) {

                    if (onClickAd == Constant.FIRST_CLICK_COUNT && Constant.FIRST_CLICK_COUNT != 0) {
                        if (Constant.AD_TYPE_FB_GOOGLE == Constant.AD_GOOGLE) {
                            AdUtils!!.loadGoogleFullAd(mContext, object : AdsCallback {
                                override fun startNextScreenAfterAd() {
                                    startNextScreenfatBurning(position)
                                }
                            })
                        } else if (Constant.AD_TYPE_FB_GOOGLE == Constant.AD_FACEBOOK) {
                            AdUtils!!.loadFacebookFullAd(mContext, object : AdsCallback {
                                override fun startNextScreenAfterAd() {
                                    startNextScreenfatBurning(position)
                                }
                            })
                        } else {
                            startNextScreenfatBurning(position)
                        }
                        onClickAd = 1
                    } else {
                        startNextScreenfatBurning(position)
                        onClickAd += 1
                    }
                } else {
                    startNextScreenfatBurning(position)
                }
            }

        })

        trainingGoalAdapter = TrainingGoalAdapter(mContext)
        binding!!.rvTrainingGoal.adapter = trainingGoalAdapter
        trainingGoalAdapter!!.setEventListener(object : TrainingGoalAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                val item = trainingGoalAdapter!!.getItem(position)
                val i = Intent(mContext, DiscoverDetailActivity::class.java)
                i.putExtra("workoutPlanData", Gson().toJson(item))
                if (item.isPro) {
                    i.putExtra(Constant.IS_PURCHASE, true)
                } else {
                    i.putExtra(Constant.IS_PURCHASE, false)
                }
                startActivity(i)
            }

        })

        postureCorrectionAdapter = PostureCorrectionAdapter(mContext)
        binding.rvPostureCorrection.adapter = postureCorrectionAdapter
        postureCorrectionAdapter!!.setEventListener(object :
            PostureCorrectionAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                val item = postureCorrectionAdapter!!.getItem(position)
                val i = Intent(mContext, ExercisesListActivity::class.java)
                i.putExtra("workoutPlanData", Gson().toJson(item))
                if (item.isPro) {
                    i.putExtra(Constant.IS_PURCHASE, true)
                } else {
                    i.putExtra(Constant.IS_PURCHASE, false)
                }
                startActivity(i)
            }
        })

        bodyFocusAdapter = TrainingGoalAdapter(mContext)
        binding!!.rvBodyFocus.adapter = bodyFocusAdapter
        bodyFocusAdapter!!.setEventListener(object : TrainingGoalAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                val item = bodyFocusAdapter!!.getItem(position)
                val i = Intent(mContext, DiscoverDetailActivity::class.java)
                i.putExtra("workoutPlanData", Gson().toJson(item))
                if (item.isPro) {
                    i.putExtra(Constant.IS_PURCHASE, true)
                } else {
                    i.putExtra(Constant.IS_PURCHASE, false)
                }
                startActivity(i)
            }
        })

        durationAdapter = DurationAdapter(mContext)
        binding!!.rvDuration.adapter = durationAdapter
        durationAdapter!!.setEventListener(object : DurationAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                val item = durationAdapter!!.getItem(position)
                val i = Intent(mContext, DiscoverDetailActivity::class.java)
                i.putExtra("workoutPlanData", Gson().toJson(item))
                if (item.isPro) {
                    i.putExtra(Constant.IS_PURCHASE, true)
                } else {
                    i.putExtra(Constant.IS_PURCHASE, false)
                }
                startActivity(i)
            }
        })

        fillData()

    }

    private fun startNextScreenfatBurning(position: Int) {
        val item = fatBurningViewPagerAdapter!!.getItem(position)
        val i = Intent(mContext, ExercisesListActivity::class.java)
        i.putExtra("workoutPlanData", Gson().toJson(item))
        if (item.isPro) {
            i.putExtra(Constant.IS_PURCHASE, true)
        } else {
            i.putExtra(Constant.IS_PURCHASE, false)
        }
        startActivity(i)
    }

    private fun startNextScreembeginner(position: Int) {
        val item = beginnerViewPagerAdapter!!.getItem(position)
        val i = Intent(mContext, ExercisesListActivity::class.java)
        i.putExtra("workoutPlanData", Gson().toJson(item))
        if (item.isPro) {
            i.putExtra(Constant.IS_PURCHASE, true)
        } else {
            i.putExtra(Constant.IS_PURCHASE, false)
        }
        startActivity(i)
    }

    private fun startNextScreenflexibility(position: Int) {
        val item = flexibilityViewPagerAdapter!!.getItem(position)
        val i = Intent(mContext, ExercisesListActivity::class.java)
        i.putExtra("workoutPlanData", Gson().toJson(item))
        if (item.isPro) {
            i.putExtra(Constant.IS_PURCHASE, true)
        } else {
            i.putExtra(Constant.IS_PURCHASE, false)
        }
        startActivity(i)
    }

    private fun startNextScreenpainRelief(position: Int) {
        val item = painReliefPagerAdapter!!.getItem(position)
        val i = Intent(mContext, ExercisesListActivity::class.java)
        i.putExtra("workoutPlanData", Gson().toJson(item))
        if (item.isPro) {
            i.putExtra(Constant.IS_PURCHASE, true)
        } else {
            i.putExtra(Constant.IS_PURCHASE, false)
        }
        startActivity(i)
    }

    private fun fillData() {
        try {
            getPainReliefData()
            getTrainingData()
            getFlexibilityData()
            getForBeginnerData()
            getPostureCurractionData()
            getFatBurningData()
            getBodyFocusData()
            getDurationData()

            val lastDate = pref!!.getPref( Constant.PREF_RANDOM_DISCOVER_PLAN_DATE, "")
            val currDate = pref!!.parseTime(Date(), "dd-MM-yyyy")
            val currDateStr = pref!!.parseTime(currDate.time, "dd-MM-yyyy")
            val str = pref!!.getPref( Constant.PREF_RANDOM_DISCOVER_PLAN, "")
            if (lastDate.isNullOrEmpty()
                    .not() && currDateStr.equals(lastDate) && str.isNullOrEmpty().not()
            ) {
                randomPlan =
                    Gson().fromJson(str, object : TypeToken<HomePlanTableClass>() {}.type)!!

            } else {
                randomPlan = dbHelper!!.getRandomDiscoverPlan()
                pref!!.setPref( Constant.PREF_RANDOM_DISCOVER_PLAN_DATE, currDateStr)
                pref!!.setPref(Constant.PREF_RANDOM_DISCOVER_PLAN, Gson().toJson(randomPlan))
            }
            binding!!.imgCover.setImageResource(pref!!.getDrawableId(randomPlan!!.planImage, mContext))
            binding!!.tvTitle.text = randomPlan!!.planName
            if (randomPlan!!.shortDes.isNullOrEmpty().not())
                binding!!.tvDesc.text = randomPlan!!.shortDes
            else {
                binding!!.tvDesc.text = randomPlan!!.introduction
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getBodyFocusData() {
        bodyFocusAdapter!!.addAll(
            dbHelper!!.getDiscoverPlanList(Constant.Discover_BodyFocus) as ArrayList<HomePlanTableClass>
        )
    }

    private fun getDurationData() {
        durationAdapter!!.addAll(dbHelper!!.getDiscoverPlanList(Constant.Discover_Duration) as ArrayList<HomePlanTableClass>)
    }

    private fun getPainReliefData() {
        painReliefPagerAdapter!!.addAll(
            dbHelper!!.getDiscoverPlanList(Constant.Discover_Pain_Relief)
                .reversed() as ArrayList<HomePlanTableClass>
        )
    }

    fun getTrainingData() {
        trainingGoalAdapter!!.addAll(
            dbHelper!!.getDiscoverPlanList(Constant.Discover_Training) as ArrayList<HomePlanTableClass>
        )
    }

    fun getFlexibilityData() {
        flexibilityViewPagerAdapter!!.addAll(
            dbHelper!!.getDiscoverPlanList(Constant.Discover_Flexibility) as ArrayList<HomePlanTableClass>
        )
    }

    fun getForBeginnerData() {
        beginnerViewPagerAdapter!!.addAll(
            dbHelper!!.getDiscoverPlanList(Constant.Discover_ForBeginner) as ArrayList<HomePlanTableClass>
        )
    }

    fun getPostureCurractionData() {
        postureCorrectionAdapter!!.addAll(
            dbHelper!!.getDiscoverPlanList(Constant.Discover_PostureCorrection) as ArrayList<HomePlanTableClass>
        )
    }

    fun getFatBurningData() {
        fatBurningViewPagerAdapter!!.addAll(
            dbHelper!!.getDiscoverPlanList(Constant.Discover_FatBurning) as ArrayList<HomePlanTableClass>
        )
    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
    }


    inner class ClickHandler {

        fun onMyTrainingClick() {
            val i = Intent(mContext, MyTrainingActivity::class.java)
            startActivity(i)
            (mContext as Activity).finish()
        }

        fun onTopPlanClick() {

            val i = Intent(mContext, ExercisesListActivity::class.java)
            i.putExtra("workoutPlanData", Gson().toJson(randomPlan))
            startActivity(i)
        }

    }



    override fun onSuccess() {

    }

    override fun onCancel() {

    }

    override fun onRetry() {

    }
}