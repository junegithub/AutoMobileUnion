package com.yt.car.union.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.GridView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yt.car.union.R
import com.yt.car.union.util.DateUtil
import com.yt.car.union.util.PressEffectUtils
import java.util.Calendar

class CalendarDialog : BottomSheetDialogFragment() {
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)

    interface OnDateSelectedListener {
        fun onSelected(start: String, end: String)
    }
    private var listener: OnDateSelectedListener? = null

    fun setOnDateSelectedListener(listener: OnDateSelectedListener) {
        this.listener = listener
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_calendar_picker, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        // 绑定控件（新增上一年/下一年/关闭Icon）
        val ivClose = view.findViewById<ImageView>(R.id.iv_close)
        val ivPrevYear = view.findViewById<ImageView>(R.id.iv_prev_year)
        val ivPrevMonth = view.findViewById<ImageView>(R.id.iv_prev_month)
        val ivNextMonth = view.findViewById<ImageView>(R.id.iv_next_month)
        val ivNextYear = view.findViewById<ImageView>(R.id.iv_next_year)
        val tvMonth = view.findViewById<TextView>(R.id.tv_month)
        val gvCalendar = view.findViewById<GridView>(R.id.gv_calendar)
        val btnConfirm = view.findViewById<View>(R.id.btn_confirm)

        PressEffectUtils.setCommonPressEffect(ivClose)
        PressEffectUtils.setCommonPressEffect(btnConfirm)

        // 更新年月显示
        fun updateMonthText() {
            tvMonth.text = "${currentYear}年${currentMonth + 1}月"
        }

        // 初始化日历数据
        fun initCalendar() {
            val days = DateUtil.getDaysInMonth(currentYear, currentMonth)
            val firstDay = DateUtil.getFirstDayOfMonth(currentYear, currentMonth)

            val dataList = mutableListOf<CalendarItem>()
            repeat(firstDay) { dataList.add(CalendarItem(isEmpty = true)) }
            repeat(days) { day ->
                val cal = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, day + 1)
                }
                val isStart = startDate?.let { DateUtil.isSameDay(it, cal) } ?: false
                val isEnd = endDate?.let { DateUtil.isSameDay(it, cal) } ?: false
                val isMiddle = DateUtil.isInMiddleRange(cal, startDate, endDate)

                dataList.add(CalendarItem(
                    isEmpty = false,
                    day = day + 1,
                    date = cal,
                    isStart = isStart,
                    isEnd = isEnd,
                    isMiddle = isMiddle
                ))
            }
            gvCalendar.adapter = CalendarAdapter(dataList)
        }

        // 点击事件：关闭弹窗（取消）
        ivClose.setOnClickListener { dismiss() }

        // 点击事件：上一年（仅切换一年）
        ivPrevYear.setOnClickListener {
            currentYear--
            updateMonthText()
            initCalendar()
        }

        // 点击事件：上月（仅切换一个月）
        ivPrevMonth.setOnClickListener {
            currentMonth--
            if (currentMonth < 0) {
                currentMonth = 11
                currentYear--
            }
            updateMonthText()
            initCalendar()
        }

        // 点击事件：下月（仅切换一个月）
        ivNextMonth.setOnClickListener {
            currentMonth++
            if (currentMonth > 11) {
                currentMonth = 0
                currentYear++
            }
            updateMonthText()
            initCalendar()
        }

        // 点击事件：下一年（仅切换一年）
        ivNextYear.setOnClickListener {
            currentYear++
            updateMonthText()
            initCalendar()
        }

        // 日历单元格点击
        gvCalendar.onItemClickListener = onItemClickListener@{ _, _, position, _ ->
            val adapter = gvCalendar.adapter as CalendarAdapter
            val item = adapter.getItem(position) as CalendarItem
            if (item.isEmpty) return@onItemClickListener

            when {
                startDate == null -> {
                    startDate = item.date
                    endDate = null
                }

                endDate == null -> {
                    if (item.date.timeInMillis >= startDate!!.timeInMillis) {
                        endDate = item.date
                    } else {
                        startDate = item.date
                        endDate = null
                    }
                }

                else -> {
                    startDate = item.date
                    endDate = null
                }
            }
            initCalendar()
        }

        // 底部蓝色长条确认按钮
        btnConfirm.setOnClickListener {
            listener?.onSelected(startDate?.time.toString(), endDate?.time.toString())
            dismiss()
        }

        // 初始化
        updateMonthText()
        initCalendar()
    }

    fun isFutureTime(time: CalendarItem): Boolean {
        return time.date.timeInMillis > System.currentTimeMillis()
    }

    data class CalendarItem(
        val isEmpty: Boolean,
        val day: Int = 0,
        val date: Calendar = Calendar.getInstance(),
        val isStart: Boolean = false,
        val isEnd: Boolean = false,
        val isMiddle: Boolean = false
    )

    inner class CalendarAdapter(private val dataList: List<CalendarItem>) : BaseAdapter() {
        override fun getCount() = dataList.size
        override fun getItem(position: Int) = dataList[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_calendar_cell, parent, false)

            val vRangeBg = view.findViewById<View>(R.id.v_range_bg)
            val vSelectedBg = view.findViewById<View>(R.id.v_selected_bg)
            val tvDate = view.findViewById<TextView>(R.id.tv_date)
            val tvLabel = view.findViewById<TextView>(R.id.tv_label)

            val item = dataList[position]

            if (item.isEmpty) {
                tvDate.text = ""
                vSelectedBg.visibility = View.GONE
                vRangeBg.visibility = View.GONE
                tvLabel.visibility = View.GONE
                return view
            }

            tvDate.text = item.day.toString()

            if (item.isStart) {
                vSelectedBg.visibility = View.VISIBLE
                vRangeBg.visibility = View.GONE
                tvDate.setTextColor(resources.getColor(R.color.calendar_bg_white))
                tvLabel.text = getString(R.string.start)
                tvLabel.visibility = View.VISIBLE
            } else if (item.isEnd) {
                vSelectedBg.visibility = View.VISIBLE
                vRangeBg.visibility = View.GONE
                tvDate.setTextColor(resources.getColor(R.color.calendar_bg_white))
                tvLabel.text = getString(R.string.end)
                tvLabel.visibility = View.VISIBLE
            } else if (item.isMiddle) {
                vSelectedBg.visibility = View.GONE
                vRangeBg.visibility = View.VISIBLE
                tvDate.setTextColor(resources.getColor(R.color.calendar_text_black))
                tvLabel.visibility = View.GONE
            } else {
                vSelectedBg.visibility = View.GONE
                vRangeBg.visibility = View.GONE
                if (isFutureTime(item)) {
                    tvDate.setTextColor(resources.getColor(R.color.calendar_text_gray))
                } else {
                    tvDate.setTextColor(resources.getColor(R.color.calendar_text_black))
                }
                tvLabel.visibility = View.GONE
            }

            return view
        }

        override fun isEnabled(position: Int): Boolean {
            return !isFutureTime(dataList[position])
        }
    }

    companion object {
        fun newInstance(): CalendarDialog {
            return CalendarDialog()
        }
    }
}