package com.yt.car.union

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.yt.car.union.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    // 声明ViewBinding对象
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        // 通过Binding对象调用控件（替换kotlinx.android.synthetic）
        binding.btnLogin.setOnClickListener {
            if (!binding.cbAgreement.isChecked) {
                Toast.makeText(this, R.string.toast_agreement, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val account = binding.etAccount.text.toString().trim()
            val pwd = binding.etPwd.text.toString().trim()
            if (account.isNotEmpty() && pwd.isNotEmpty()) {
                finish()
            } else {
                Toast.makeText(this, R.string.toast_empty_account, Toast.LENGTH_SHORT).show()
            }
        }
    }
}