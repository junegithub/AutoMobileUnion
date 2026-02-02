package com.yt.car.union.pages.training

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.checkbox.MaterialCheckBox
import com.yt.car.union.R
import com.yt.car.union.bean.TodoItem
import com.yt.car.union.databinding.FragmentSafetyMainBinding
import com.yt.car.union.util.StatusBarHeightUtil
import com.yt.car.union.viewmodel.SafetyViewModel
import java.util.concurrent.TimeUnit

class TrainingFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentSafetyMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SafetyViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSafetyMainBinding.inflate(inflater, container, false)
        _binding?.swipeRefreshLayout?.setPaddingRelative(0, StatusBarHeightUtil.getStatusBarHeight(requireContext()), 0,0)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[SafetyViewModel::class.java]

        // 初始化下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener(this)

        // 观察数据变化
        observeData()

        // 初始化点击事件
        initClickEvents()
    }

    /**
     * 观察ViewModel数据变化
     */
    private fun observeData() {
        // 培训卡片数据
        viewModel.trainingCard.observe(viewLifecycleOwner) { card ->
            card?.let {
                binding.trainingTitle.text = it.title
                binding.trainingInfo.text = it.info
                binding.tvUnfinishedCount.text = "${it.unfinishedCount}个"
            }
        }

        // 待办列表数据
        viewModel.todoList.observe(viewLifecycleOwner) { todoList ->
            if (todoList.isNotEmpty()) {
                // 绑定待办项数据
                bindTodoList(todoList)
            }
        }

        // 会议数据
        viewModel.meetingItem.observe(viewLifecycleOwner) { meeting ->
            meeting?.let {
                binding.meetingTitle.text = it.title
                binding.meetingContent.text = it.content
                binding.meetingCountdown.text = it.countdown
            }
        }

        // 加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }
    }

    /**
     * 绑定待办列表数据（动态生成待办项，支持标记完成）
     */
    private fun bindTodoList(todoList: List<TodoItem>) {
        binding.todoContainer.removeAllViews()
        todoList.forEach { todo ->
            val todoView = layoutInflater.inflate(R.layout.item_todo, binding.todoContainer, false)

            // 绑定数据
            val cbTodo = todoView.findViewById<MaterialCheckBox>(R.id.cbTodo)
            cbTodo.text = todo.title
            cbTodo.isChecked = todo.isCompleted
            cbTodo.isEnabled = !todo.isCompleted // 已完成则禁用复选框

            val tvTodoDesc = todoView.findViewById<android.widget.TextView>(R.id.tvTodoDesc)
            tvTodoDesc.text = todo.desc

            val tvOverdueTime = todoView.findViewById<android.widget.TextView>(R.id.tvOverdueTime)
            tvOverdueTime.text = todo.overdueTime

            // 标记完成事件
            cbTodo.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.markTodoCompleted(todo.id)
                    Toast.makeText(context, "${todo.title} 已标记为完成", Toast.LENGTH_SHORT).show()
                }
            }

            // 点击待办项跳转详情页
            todoView.setOnClickListener {
                navigateToTodoDetail(todo)
            }

            binding.todoContainer.addView(todoView)
        }
    }

    /**
     * 初始化所有点击事件
     */
    private fun initClickEvents() {
        // 开始学习按钮
        binding.btnStartLearn.setOnClickListener {
//            navigateToFragment(LearnDetailFragment())
            Toast.makeText(context, "进入日常安全培训详情", Toast.LENGTH_SHORT).show()
        }

        // 学习记录
        binding.todoLearnRecord.setOnClickListener {
//            navigateToFragment(LearnRecordFragment())
            Toast.makeText(context, "进入学习记录页面", Toast.LENGTH_SHORT).show()
        }

        // 从业资格考试真题
        binding.btnExamTrue.setOnClickListener {
//            navigateToFragment(ExamTrueFragment())
            Toast.makeText(context, "进入从业资格考试真题", Toast.LENGTH_SHORT).show()
        }

        // 两类人员真题
        binding.btnTwoPerson.setOnClickListener {
//            navigateToFragment(TwoPersonFragment())
            Toast.makeText(context, "进入两类人员真题", Toast.LENGTH_SHORT).show()
        }

        // 继续教育
        binding.btnContinueEdu.setOnClickListener {
//            navigateToFragment(ContinueEduFragment())
            Toast.makeText(context, "进入继续教育页面", Toast.LENGTH_SHORT).show()
        }

        // 安全会议点击
        binding.meetingCard.setOnClickListener {
            val meeting = viewModel.meetingItem.value
            meeting?.let {
//                navigateToFragment(MeetingDetailFragment.newInstance(meeting))
                Toast.makeText(context, "进入安全会议详情", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 跳转到待办详情页
     */
    private fun navigateToTodoDetail(todo: TodoItem) {
//        val fragment = TodoDetailFragment.newInstance(todo)
//        navigateToFragment(fragment)
    }

    /**
     * 通用Fragment跳转方法
     */
    private fun navigateToFragment(fragment: Fragment) {
//        parentFragmentManager.commit {
//            replace(R.id.fragment_container, fragment)
//            addToBackStack(null) // 添加到返回栈，支持返回
//        }
    }

    /**
     * 下拉刷新回调
     */
    override fun onRefresh() {
        viewModel.refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TrainingFragment()
    }
}