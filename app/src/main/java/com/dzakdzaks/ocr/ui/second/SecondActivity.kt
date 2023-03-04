package com.dzakdzaks.ocr.ui.second

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.dzakdzaks.ocr.core.util.isEmptyOrBlank
import com.dzakdzaks.ocr.core.util.toKiloMeter
import com.dzakdzaks.ocr.core.util.toReadableHour
import com.dzakdzaks.ocr.databinding.ActivitySecondBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    private val viewModel by viewModels<SecondViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        observe()
    }

    private fun initView() {
        with(binding) {
            fieldDistance.editText?.addTextChangedListener {
                fieldDistance.helperText = if (!it.toString().isEmptyOrBlank()) {
                    it.toString().toLong().toKiloMeter()
                } else {
                    ""
                }
            }
            fieldDuration.editText?.addTextChangedListener {
                fieldDuration.helperText = if (!it.toString().isEmptyOrBlank()) {
                    it.toString().toLong().toReadableHour()
                } else {
                    ""
                }
            }
        }
    }

    private fun observe() {
        with(binding) {
            with(viewModel) {
                resultText.observe(this@SecondActivity) {
                    fieldResultText.editText?.setText(it)
                }
                distance.observe(this@SecondActivity) {
                    fieldDistance.editText?.setText(it.toString())
                }
                duration.observe(this@SecondActivity) {
                    fieldDuration.editText?.setText(it.toString())
                }
            }
        }
    }

    companion object {
        const val EXTRA_RESULT_TEXT = "resultText"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_DISTANCE = "distance"

        fun start(context: Context, resultText: String, duration: Long, distance: Long) {
            context.startActivity(Intent(context, SecondActivity::class.java).apply {
                putExtra(EXTRA_RESULT_TEXT, resultText)
                putExtra(EXTRA_DURATION, duration)
                putExtra(EXTRA_DISTANCE, distance)
            })
        }
    }
}
