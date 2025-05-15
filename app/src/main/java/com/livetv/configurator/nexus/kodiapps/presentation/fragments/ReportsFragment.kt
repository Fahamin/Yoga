package com.livetv.configurator.nexus.kodiapps.presentation.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.livetv.configurator.nexus.kodiapps.R
import com.livetv.configurator.nexus.kodiapps.adapter.ReportWeekGoalAdapter
import com.livetv.configurator.nexus.kodiapps.core.Constant
import com.livetv.configurator.nexus.kodiapps.core.Prefs
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener
import com.livetv.configurator.nexus.kodiapps.core.interfaces.DialogDismissListener
import com.livetv.configurator.nexus.kodiapps.databinding.DialogWeightWithDateBinding
import com.livetv.configurator.nexus.kodiapps.databinding.FragmentReportsBinding
import com.livetv.configurator.nexus.kodiapps.presentation.activity.HistoryActivity
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class ReportsFragment : BaseFragment(), CallbackListener {
    lateinit var binding: FragmentReportsBinding
    var reportWeekGoalAdapter: ReportWeekGoalAdapter? = null
    private var count = 0
    var avgAnnualWight = 0F
    lateinit var mContext: Context
    private lateinit var daysText: java.util.ArrayList<String>
    private lateinit var daysYearText: java.util.ArrayList<String>
    var pref: Prefs? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reports, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = Prefs(requireContext())
        init()
    }

    private fun init() {
        this.mContext = binding.root.context

        binding.handler = ClickHandler()
        binding.topbar.tvTitleText.text = getString(R.string.menu_report)
        reportWeekGoalAdapter = ReportWeekGoalAdapter(mContext)
        binding.rvWeekGoal.layoutManager =
            LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        binding.rvWeekGoal.adapter = reportWeekGoalAdapter

        reportWeekGoalAdapter?.setEventListener(object : ReportWeekGoalAdapter.EventListener {
            override fun onItemClick(position: Int, view: View) {
                val i = Intent(mContext, HistoryActivity::class.java)
                startActivity(i)
            }
        })

        initCommon()
    }

    private fun initCommon() {
        setupGraph()
        setWeightValues()
        setBmiCalculation()

        binding.tvWorkOuts.text = dbHelper?.getHistoryTotalWorkout()?.toString() ?: "0"
        binding.tvCalorie.text = dbHelper?.getHistoryTotalKCal()?.toInt()?.toString() ?: "0"
        binding.tvMinutes.text =
            ((dbHelper?.getHistoryTotalMinutes() ?: 0 / 60).toDouble()).roundToInt().toString()
    }

    private fun setBmiCalculation() {
        try {
            val lastWeight = pref!!.getPref(Constant.PREF_LAST_INPUT_WEIGHT, 0f)
            val lastFoot = pref!!.getPref(Constant.PREF_LAST_INPUT_FOOT, 0)
            val lastInch = pref!!.getPref(Constant.PREF_LAST_INPUT_INCH, 0F)

            val heightUnit = pref!!.getPref(Constant.PREF_HEIGHT_UNIT, Constant.DEF_CM)

            if (heightUnit.equals(Constant.DEF_CM)) {
                val inch = pref!!.ftInToInch(
                    lastFoot,
                    lastInch.toDouble()
                )
                binding.editCurrHeightCM.visibility = View.VISIBLE
                binding.editCurrHeightFT.visibility = View.GONE
                binding.editCurrHeightIn.visibility = View.GONE
                binding.editCurrHeightCM.setText(
                    pref!!.inchToCm(inch).roundToInt().toDouble().toString()
                )
            } else {
                binding.editCurrHeightCM.visibility = View.GONE
                binding.editCurrHeightFT.visibility = View.VISIBLE
                binding.editCurrHeightIn.visibility = View.VISIBLE
                binding.editCurrHeightFT.setText(
                    pref!!.getPref(Constant.PREF_LAST_INPUT_FOOT, 0).toString()
                )
                binding.editCurrHeightIn.setText(
                    pref!!.getPref(Constant.PREF_LAST_INPUT_INCH, 0F).toString()
                )
            }

            if (lastWeight != 0f && lastFoot != 0 && lastInch.toInt() != 0) {
                binding.clBMIGraphView.visibility = View.VISIBLE

                val bmiValue = pref!!.getBmiCalculation(
                    lastWeight,
                    lastFoot,
                    lastInch.toInt()
                )

                val bmiVal = pref!!.calculationForBmiGraph(bmiValue.toFloat())

                val param = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    bmiVal
                )

                binding.txtBmiGrade.text = pref!!.truncateUptoTwoDecimal(bmiValue.toString())
                binding.tvBMI.text = pref!!.truncateUptoTwoDecimal(bmiValue.toString())
                binding.tvWeightString.text = pref!!.bmiWeightString(bmiValue.toFloat())
                binding.tvWeightString.setTextColor(
                    ColorStateList.valueOf(
                        pref!!.bmiWeightTextColor(
                            mContext,
                            bmiValue.toFloat()
                        )
                    )
                )
                binding.blankView1.layoutParams = param
            } else {
                binding.clBMIGraphView.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.clBMIGraphView.visibility = View.GONE
        }
    }

    private fun setWeightValues() {
        try {
            val lastWeight = pref!!.getPref(Constant.PREF_LAST_INPUT_WEIGHT, 0f) ?: 0f
            val weightUnit = pref!!.getPref(Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG)
                ?: Constant.DEF_KG

            val maxWeight = dbHelper?.getMaxWeight()?.toFloat() ?: 0f
            val minWeight = dbHelper?.getMinWeight()?.toFloat() ?: 0f

            if (weightUnit == Constant.DEF_KG && lastWeight != 0f) {
                binding.tvCurrentWeight.text =
                    lastWeight.toString().takeIf { it.isNotBlank() }?.let {
                        pref!!.truncateUptoTwoDecimal(it)
                    } ?: "0.0"
                binding.tvCurretUnit.text = weightUnit
                binding.tvHeaviestWeight.text =
                    "${pref!!.truncateUptoTwoDecimal(maxWeight.toString())} $weightUnit"
                binding.tvLightestWeight.text =
                    "${pref!!.truncateUptoTwoDecimal(minWeight.toString())} $weightUnit"
            } else if (weightUnit == Constant.DEF_LB && lastWeight != 0f) {
                binding.tvCurrentWeight.text =
                    pref!!.kgToLb(lastWeight.toDouble()).toFloat().toString()
                        .takeIf { it.isNotBlank() }?.let {
                            pref!!.truncateUptoTwoDecimal(it)
                        } ?: "0.0"
                binding.tvCurretUnit.text = weightUnit
                binding.tvHeaviestWeight.text =
                    "${
                        pref!!.truncateUptoTwoDecimal(
                            pref!!.kgToLb(maxWeight.toDouble()).toString()
                        )
                    } $weightUnit"
                binding.tvLightestWeight.text =
                    "${
                        pref!!.truncateUptoTwoDecimal(
                            pref!!.kgToLb(minWeight.toDouble()).toString()
                        )
                    } $weightUnit"
            } else {
                // Set default values if no weight data available
                binding.tvCurrentWeight.text = "0.0"
                binding.tvCurretUnit.text = weightUnit
                binding.tvHeaviestWeight.text = "0.0 $weightUnit"
                binding.tvLightestWeight.text = "0.0 $weightUnit"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Set default values in case of error
            binding.tvCurrentWeight.text = "0.0"
            binding.tvCurretUnit.text = Constant.DEF_KG
            binding.tvHeaviestWeight.text = "0.0 ${Constant.DEF_KG}"
            binding.tvLightestWeight.text = "0.0 ${Constant.DEF_KG}"
        }
    }

    private fun setupGraph() {
        try {
            val format = SimpleDateFormat("dd/MM", Locale.getDefault())
            val formatDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)

            calendar.time = formatDate.parse("$year-01-01") ?: Date()

            count = getIsLeapYear(year) + 1
            daysText = ArrayList()
            daysYearText = ArrayList()

            for (i in 0 until count) {
                daysText.add(format.format(calendar.time))
                daysYearText.add(formatDate.format(calendar.time))
                calendar.add(Calendar.DATE, 1)
            }

            binding.chartWeight.drawOrder = arrayOf(CombinedChart.DrawOrder.LINE)
            binding.chartWeight.description.isEnabled = false
            binding.chartWeight.description.text = "Date"
            binding.chartWeight.setNoDataText(resources.getString(R.string.app_name))
            binding.chartWeight.setBackgroundColor(Color.WHITE)
            binding.chartWeight.setDrawGridBackground(false)
            binding.chartWeight.setDrawBarShadow(false)
            binding.chartWeight.isHighlightFullBarEnabled = false

            val l = binding.chartWeight.legend
            l.isEnabled = false
            l.isWordWrapEnabled = true
            l.textSize = 14f
            l.formSize = 15F
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.setDrawInside(false)

            val leftAxis = binding.chartWeight.axisLeft
            leftAxis.setDrawGridLines(true)

            val rightAxis = binding.chartWeight.axisRight
            rightAxis.isEnabled = false

            val xAxis = binding.chartWeight.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = count.toFloat()
            xAxis.granularity = 1f
            xAxis.labelCount = 30

            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value < daysText.size && value > 0) {
                        daysText[value.toInt()]
                    } else ""
                }
            }

            val data = CombinedData()
            data.setData(generateLineData())

            if (avgAnnualWight > 0) {
                binding.chartWeight.axisLeft.removeAllLimitLines()
                val ll = LimitLine(avgAnnualWight, "")
                ll.lineColor = Color.rgb(181, 129, 189)
                ll.lineWidth = 2f
                binding.chartWeight.axisLeft.addLimitLine(ll)

                val legendEntryA = LegendEntry()
                legendEntryA.label = "Annual Average"
                legendEntryA.formColor = Color.rgb(181, 129, 189)
                l.setExtra(listOf(legendEntryA))
            }

            data.setValueTypeface(Typeface.DEFAULT)
            binding.chartWeight.data = data
            binding.chartWeight.setVisibleXRange(5f, 8f)

            val strDate = pref!!.parseTime(Date().time, "yyyy-MM-dd")
            val xPos = daysYearText.indexOf(strDate)
            var yPos = 0f
            val lastWeight = pref!!.getPref(Constant.PREF_LAST_INPUT_WEIGHT, 0f) ?: 0f
            val weightUnit = pref!!.getPref(Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG)
                ?: Constant.DEF_KG

            yPos = if (weightUnit == Constant.DEF_KG) {
                lastWeight
            } else {
                pref!!.kgToLb(lastWeight.toDouble()).toFloat()
            }

            binding.chartWeight.centerViewTo(xPos.toFloat(), yPos, YAxis.AxisDependency.LEFT)
            setGraphTouch()
            binding.chartWeight.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setGraphTouch() {
        binding.chartWeight.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    binding.chartWeight.parent.requestDisallowInterceptTouchEvent(true)
            }
            false
        }
    }

    private fun generateLineData(): LineData {
        val d = LineData()
        val entries = ArrayList<Entry>()

        try {
            val yAxisData = dbHelper?.getUserWeightData() ?: emptyList()
            var totalWeight = 0f

            if (yAxisData.isNotEmpty()) {
                for (index in yAxisData.indices) {
                    val strDate = yAxisData[index]["DT"] ?: continue
                    val kgStr = yAxisData[index]["KG"] ?: continue

                    val position = daysYearText.indexOf(strDate)
                    if (position == -1) continue

                    val weightUnit =
                        pref!!.getPref(Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG)
                            ?: Constant.DEF_KG
                    val kgValue = try {
                        kgStr.toFloat()
                    } catch (e: NumberFormatException) {
                        0f
                    }

                    if (weightUnit == Constant.DEF_KG) {
                        totalWeight += kgValue
                        entries.add(Entry(position.toFloat(), kgValue))
                    } else {
                        val lbValue = pref!!.kgToLb(kgValue.toDouble()).toFloat()
                        totalWeight += lbValue
                        entries.add(Entry(position.toFloat(), lbValue))
                    }
                }

                if (yAxisData.isNotEmpty()) {
                    avgAnnualWight = totalWeight / yAxisData.size
                    val firstWeight = yAxisData.first()["KG"]?.toFloatOrNull() ?: 0f
                    val lastWeight = yAxisData.last()["KG"]?.toFloatOrNull() ?: 0f
                    val weightUnit =
                        pref!!.getPref(Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG)
                            ?: Constant.DEF_KG
                    var difference = lastWeight - firstWeight

                    if (weightUnit == Constant.DEF_LB) {
                        difference = pref!!.kgToLb(difference.toDouble()).toFloat()
                    }

                    binding.tvAvgWeight.text =
                        "${
                            String.format(
                                "%.2f",
                                difference
                            )
                        } ${if (weightUnit == Constant.DEF_KG) Constant.DEF_KG else Constant.DEF_LB}"

                    binding.tvAvgWeight.backgroundTintList = ColorStateList.valueOf(
                        if (difference < 0) Color.rgb(0, 192, 98) else Color.rgb(246, 176, 39)
                    )
                }
            } else {
                // Handle case when no weight data exists
                val lastWeight = pref!!.getPref(Constant.PREF_LAST_INPUT_WEIGHT, 0f) ?: 0f
                val weightUnit = pref!!.getPref(Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG)
                    ?: Constant.DEF_KG

                if (lastWeight > 0) {
                    val strDate = pref!!.parseTime(Date().time, Constant.DATE_FORMAT)
                    val position = daysYearText.indexOf(strDate)

                    if (weightUnit == Constant.DEF_KG) {
                        entries.add(Entry(position.toFloat(), lastWeight))
                    } else {
                        entries.add(
                            Entry(
                                position.toFloat(),
                                pref!!.kgToLb(lastWeight.toDouble()).toFloat()
                            )
                        )
                    }

                    binding.tvAvgWeight.text =
                        "${
                            String.format(
                                "%.2f",
                                lastWeight
                            )
                        } ${if (weightUnit == Constant.DEF_KG) Constant.DEF_KG else Constant.DEF_LB}"
                    binding.tvAvgWeight.backgroundTintList =
                        ColorStateList.valueOf(Color.rgb(246, 176, 39))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (entries.isNotEmpty()) {
            val set = LineDataSet(entries, "Date")
            set.color = Color.rgb(246, 176, 39)
            set.lineWidth = 2f
            set.circleHoleRadius = 0f
            set.circleHoleColor = Color.rgb(246, 176, 39)
            set.setCircleColor(Color.rgb(246, 176, 39))
            set.circleRadius = 5f
            set.mode = LineDataSet.Mode.LINEAR
            set.setDrawValues(false)
            set.valueTextSize = 15f
            set.valueTextColor = Color.rgb(246, 176, 39)
            set.axisDependency = YAxis.AxisDependency.LEFT
            d.addDataSet(set)
        }

        return d
    }

    override fun onResume() {
        openInternetDialog(this)
        super.onResume()
    }

    private fun getIsLeapYear(year: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        return cal.getActualMaximum(Calendar.DAY_OF_YEAR)
    }

    inner class ClickHandler {
        fun onAddWeightClick() {
            showAddWeightByDate()
        }

        fun onEditBMIClick() {
            showHeightWeightDialog(object : DialogDismissListener {
                override fun onDialogDismiss() {
                    setupGraph()
                    setBmiCalculation()
                    setWeightValues()
                }
            })
        }

        fun onRecordsClick() {
            val i = Intent(mContext, HistoryActivity::class.java)
            startActivity(i)
        }
    }

    private fun showAddWeightByDate() {
        try {
            var boolKg = true
            var dateSelect = pref!!.parseTime(Date().time, Constant.WEIGHT_TABLE_DATE_FORMAT)

            val builder = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
            builder.setCancelable(false)
            val v: View = (mContext as Activity).layoutInflater
                .inflate(R.layout.dialog_weight_with_date, null)
            val dialogBinding: DialogWeightWithDateBinding? = DataBindingUtil.bind(v)
            builder.setView(v)

            dialogBinding?.let { db ->
                db.editWeight.requestFocus()
                db.editWeight.setSelectAllOnFocus(true)

                val lastWeight = pref!!.getPref(Constant.PREF_LAST_INPUT_WEIGHT, 0f) ?: 0f
                val weightUnit = pref!!.getPref(Constant.PREF_WEIGHT_UNIT, Constant.DEF_KG)
                    ?: Constant.DEF_KG

                if (weightUnit == Constant.DEF_KG && lastWeight != 0f) {
                    db.tvKG.isSelected = true
                    db.tvLB.isSelected = false
                    db.editWeight.setText(lastWeight.toString())
                    db.tvKG.background = resources.getDrawable(R.color.green_text, null)
                    db.tvLB.background = resources.getDrawable(R.color.gray_light_, null)
                } else if (weightUnit == Constant.DEF_LB && lastWeight != 0f) {
                    db.tvKG.isSelected = false
                    db.tvLB.isSelected = true
                    boolKg = false
                    db.editWeight.setText(pref!!.kgToLb(lastWeight.toDouble()).toFloat().toString())
                    db.tvKG.background = resources.getDrawable(R.color.gray_light_, null)
                    db.tvLB.background = resources.getDrawable(R.color.green_text, null)
                } else {
                    db.tvKG.isSelected = true
                    db.tvLB.isSelected = false
                    db.tvKG.background = resources.getDrawable(R.color.green_text, null)
                    db.tvLB.background = resources.getDrawable(R.color.gray_light_, null)
                }

                db.editWeight.setOnClickListener {
                    if (db.editWeight.text.toString() == "0.0") {
                        db.editWeight.setText("")
                    } else {
                        db.editWeight.setSelectAllOnFocus(false)
                    }
                }

                db.tvKG.setOnClickListener {
                    try {
                        if (!boolKg) {
                            boolKg = true
                            db.tvKG.isSelected = true
                            db.tvLB.isSelected = false
                            db.tvKG.background = resources.getDrawable(R.color.green_text, null)
                            db.tvLB.background = resources.getDrawable(R.color.gray_light_, null)
                            db.editWeight.hint = Constant.DEF_KG

                            db.editWeight.text?.toString()?.takeIf { it.isNotBlank() }
                                ?.let { weightStr ->
                                    db.editWeight.setText(
                                        pref!!.lbToKg(weightStr.toDouble()).toString()
                                    )
                                }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                db.tvLB.setOnClickListener {
                    try {
                        if (boolKg) {
                            boolKg = false
                            db.tvKG.isSelected = false
                            db.tvLB.isSelected = true
                            db.tvLB.background = resources.getDrawable(R.color.green_text, null)
                            db.tvKG.background = resources.getDrawable(R.color.gray_light_, null)
                            db.editWeight.hint = Constant.DEF_LB

                            db.editWeight.text?.toString()?.takeIf { it.isNotBlank() }
                                ?.let { weightStr ->
                                    db.editWeight.setText(
                                        pref!!.kgToLb(weightStr.toDouble()).toString()
                                    )
                                }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                db.dtpWeightSet
                    .setDays(369)
                    .setOffset(365)
                    .setListener { dateSelected ->
                        dateSelect = pref!!.parseTime(
                            dateSelected.toDate().time,
                            Constant.WEIGHT_TABLE_DATE_FORMAT
                        )

                        dbHelper?.let { helper ->
                            if (helper.weightExistOrNot(dateSelect)) {
                                helper.getWeightForDate(dateSelect)?.let { weight ->
                                    if (db.tvKG.isSelected) {
                                        boolKg = true
                                        db.tvKG.isSelected = true
                                        db.tvLB.isSelected = false
                                        db.tvKG.background =
                                            resources.getDrawable(R.color.green_text, null)
                                        db.tvLB.background =
                                            resources.getDrawable(R.color.gray_light_, null)
                                        db.editWeight.setText(pref!!.truncateUptoTwoDecimal(weight))
                                    } else {
                                        boolKg = false
                                        db.tvKG.isSelected = false
                                        db.tvLB.isSelected = true
                                        db.tvLB.background =
                                            resources.getDrawable(R.color.green_text, null)
                                        db.tvKG.background =
                                            resources.getDrawable(R.color.gray_light_, null)
                                        db.editWeight.setText(
                                            pref!!.kgToLb(weight.toDouble()).toString()
                                        )
                                    }
                                }
                            } else {
                                db.editWeight.setText("0.0")
                            }
                        }
                    }
                    .showTodayButton(false)
                    .init()

                db.dtpWeightSet.setDate(DateTime.now())

                builder.setPositiveButton(R.string.save) { dialog, _ ->
                    try {
                        val weightStr = db.editWeight.text?.toString()?.trim()
                        if (weightStr.isNullOrBlank()) {
                            dialog.dismiss()
                            return@setPositiveButton
                        }

                        val strKG: Float
                        val strUnit: String
                        val date = pref!!.parseTime(dateSelect, Constant.WEIGHT_TABLE_DATE_FORMAT)
                        val currDate = pref!!.parseTime(Date(), Constant.WEIGHT_TABLE_DATE_FORMAT)

                        if (boolKg) {
                            strKG = weightStr.toFloatOrNull() ?: 0f
                            strUnit = Constant.DEF_KG
                        } else {
                            strKG = pref!!.lbToKg(weightStr.toDoubleOrNull() ?: 0.0).roundToInt()
                                .toFloat()
                            strUnit = Constant.DEF_LB
                        }

                        if (date >= currDate) {
                            pref!!.setPref(Constant.PREF_WEIGHT_UNIT, strUnit)
                            pref!!.setPref(Constant.PREF_LAST_INPUT_WEIGHT, strKG)
                        }

                        dbHelper?.let { helper ->
                            if (helper.weightExistOrNot(dateSelect)) {
                                helper.updateWeight(dateSelect, strKG.toString(), "")
                            } else {
                                helper.addUserWeight(strKG.toString(), dateSelect, "")
                            }
                        }

                        setupGraph()
                        setWeightValues()
                        setBmiCalculation()
                        dialog.dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        dialog.dismiss()
                    }
                }

                builder.setNegativeButton(R.string.btn_cancel) { dialog, _ -> dialog.dismiss() }
                builder.create().show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSuccess() {}
    override fun onCancel() {}
    override fun onRetry() {}
}