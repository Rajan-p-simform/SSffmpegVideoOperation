package com.simform.videoimageeditor.videoProcessActivity

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.jaiselrahman.filepicker.model.MediaFile
import com.simform.videoimageeditor.BaseActivity
import com.simform.videoimageeditor.R
import com.simform.videoimageeditor.databinding.ActivityVideoToGifBinding
import com.simform.videooperations.CallBackOfQuery
import com.simform.videooperations.Common
import com.simform.videooperations.FFmpegCallBack
import com.simform.videooperations.FFmpegQueryExtension
import com.simform.videooperations.LogMessage
import java.util.concurrent.CyclicBarrier

class VideoToGifActivity : BaseActivity(R.layout.activity_video_to_gif, R.string.video_to_gif) {
    private var isInputVideoSelected: Boolean = false
    private lateinit var binding: ActivityVideoToGifBinding
    
    override fun initialization() {
        binding = ActivityVideoToGifBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnVideoPath.setOnClickListener(this)
        binding.btnConvertIntoGif.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnVideoPath -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                } else {
                    // Fallback for devices below Android 14
                    Common.selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = false)
                }
            }
            R.id.btnConvertIntoGif -> {
                when {
                    !isInputVideoSelected -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        processStart()
                        convertProcess()
                    }
                }
            }
        }
    }

    private fun convertProcess() {
        val outputPath = Common.getFilePath(this, Common.GIF)
        val query = ffmpegQueryExtension.convertVideoToGIF(binding.tvInputPathVideo.text.toString(), outputPath)

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

    @SuppressLint("NewApi")
    override fun selectedFiles(mediaFiles: List<MediaFile>?, requestCode: Int) {
        when (requestCode) {
            Common.VIDEO_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    binding.tvInputPathVideo.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Common.saveFileToTempAndGetPath(this, mediaFiles[0].uri)
                    } else {
                        mediaFiles[0].path
                    }
                    isInputVideoSelected = true
                } else {
                    Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processStop() {
        binding.apply {
            btnVideoPath.isEnabled = true
            btnConvertIntoGif.isEnabled = true
            mProgressView.root.visibility = View.GONE
        }
    }

    private fun processStart() {
        binding.apply {
            btnVideoPath.isEnabled = false
            btnConvertIntoGif.isEnabled = false
            mProgressView.root.visibility = View.VISIBLE
        }
    }
}