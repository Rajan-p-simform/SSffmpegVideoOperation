package com.simform.videoimageeditor.videoProcessActivity

import android.annotation.SuppressLint
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.jaiselrahman.filepicker.model.MediaFile
import com.simform.videoimageeditor.BaseActivity
import com.simform.videoimageeditor.R
import com.simform.videoimageeditor.databinding.ActivityCutVideoUsingTimeBinding
import com.simform.videoimageeditor.ikovac.timepickerwithseconds.MyTimePickerDialog
import com.simform.videoimageeditor.utils.enableEdgeToEdge
import com.simform.videooperations.CallBackOfQuery
import com.simform.videooperations.Common
import com.simform.videooperations.Common.TIME_FORMAT
import com.simform.videooperations.Common.VIDEO_FILE_REQUEST_CODE
import com.simform.videooperations.Common.stringForTime
import com.simform.videooperations.FFmpegCallBack
import com.simform.videooperations.LogMessage
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CutVideoUsingTimeActivity : BaseActivity(R.layout.activity_cut_video_using_time, R.string.cut_video_using_time) {
    private var startTimeString: String? = null
    private var endTimeString: String? = null
    private var maxTimeString: String? = null
    private lateinit var binding: ActivityCutVideoUsingTimeBinding

    override fun initialization() {
        binding = ActivityCutVideoUsingTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge(binding.toolbar.root)
        binding.toolbar.textTitle.text = getString(R.string.cut_video_using_time)

        binding.apply {
            btnVideoPath.setOnClickListener(this@CutVideoUsingTimeActivity)
            btnSelectStartTime.setOnClickListener(this@CutVideoUsingTimeActivity)
            btnSelectEndTime.setOnClickListener(this@CutVideoUsingTimeActivity)
            btnConvert.setOnClickListener(this@CutVideoUsingTimeActivity)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnVideoPath -> {
                // check if device is 14 or plus
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                } else {
                    // Fallback for devices below Android 14
                    Common.selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = false)
                }
            }
            R.id.btnSelectStartTime -> {
                if (!TextUtils.isEmpty(maxTimeString) && !TextUtils.equals(maxTimeString, getString(R.string.zero_time))) {
                    selectTime(binding.edtStartTime, true)
                } else {
                    Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                }
            }
            R.id.btnSelectEndTime -> {
                if (!TextUtils.isEmpty(maxTimeString) && !TextUtils.equals(maxTimeString, getString(R.string.zero_time))) {
                    selectTime(binding.edtEndTime, false)
                } else {
                    Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                }
            }
            R.id.btnConvert -> {
                when {
                    TextUtils.isEmpty(maxTimeString) -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(startTimeString) -> {
                        Toast.makeText(this, getString(R.string.start_time_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(endTimeString) -> {
                        Toast.makeText(this, getString(R.string.end_time_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        if (isValidation()) {
                            processStart()
                            cutProcess()
                        } else {
                            Toast.makeText(this, getString(R.string.start_time_end_time_validation_message), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun selectedFiles(mediaFiles: List<MediaFile>?, requestCode: Int) {
        if (requestCode == VIDEO_FILE_REQUEST_CODE) {
            if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                binding.tvInputPath.text =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Common.saveFileToTempAndGetPath(this, mediaFiles[0].uri)
                } else {
                    mediaFiles[0].path
                }
                maxTimeString =
                    stringForTime(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Common.getDurationFromFile(File(binding.tvInputPath.text.toString()))
                    } else {
                            mediaFiles[0].duration
                        }
                    )
                binding.tvMaxTime.text = "Selected video max time : $maxTimeString"
            } else {
                Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun selectTime(tvTime: TextView, isStartTime: Boolean) {
        MyTimePickerDialog(this, { _, hourOfDay, minute, seconds ->
            val selectedTime = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", seconds)
            if (isSelectedTimeValid(selectedTime)) {
                tvTime.text = selectedTime
                if (isStartTime) {
                    startTimeString = selectedTime
                } else {
                    endTimeString = selectedTime
                }
            } else {
                Toast.makeText(this, getString(R.string.time_range_validate_message), Toast.LENGTH_SHORT).show()
            }
        }, 0, 0, 0, true).show()
    }

    private fun isSelectedTimeValid(selectedTime: String?): Boolean {
        var isBetween = false
        try {
            val time1: Date = SimpleDateFormat(TIME_FORMAT, Locale.ENGLISH).parse(getString(R.string.zero_time))
            val time2: Date = SimpleDateFormat(TIME_FORMAT, Locale.ENGLISH).parse(maxTimeString)
            val sTime: Date = SimpleDateFormat(TIME_FORMAT, Locale.ENGLISH).parse(selectedTime)
            if (time1.before(sTime) && time2.after(sTime)) {
                isBetween = true
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return isBetween
    }

    private fun isValidation(): Boolean {
        var isBetween = false
        try {
            val time1: Date = SimpleDateFormat(TIME_FORMAT, Locale.ENGLISH).parse(startTimeString)
            val time2: Date = SimpleDateFormat(TIME_FORMAT, Locale.ENGLISH).parse(endTimeString)
            if (time1.before(time2)) {
                isBetween = true
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return isBetween
    }

    @SuppressLint("SetTextI18n")
    private fun cutProcess() {
        val outputPath = Common.getFilePath(this, Common.VIDEO)
        val query = ffmpegQueryExtension.cutVideo(binding.tvInputPath.text.toString(), startTimeString, endTimeString, outputPath)
        CallBackOfQuery().callQuery(query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                binding.tvOutputPath.text = logMessage.text
            }

            override fun success() {
                binding.tvOutputPath.text = String.format(getString(R.string.output_path), outputPath)
                processStop()
            }

            override fun cancel() {
                processStop()
            }

            override fun failed() {
                processStop()
            }
        })
    }

    private fun processStop() {
        binding.apply {
            btnVideoPath.isEnabled = true
            btnSelectStartTime.isEnabled = true
            btnSelectEndTime.isEnabled = true
            btnConvert.isEnabled = true
            mProgressView.root.visibility = View.GONE
        }
    }

    private fun processStart() {
        binding.apply {
            btnVideoPath.isEnabled = false
            btnSelectStartTime.isEnabled = false
            btnSelectEndTime.isEnabled = false
            btnConvert.isEnabled = false
            mProgressView.root.visibility = View.VISIBLE
        }
    }
}