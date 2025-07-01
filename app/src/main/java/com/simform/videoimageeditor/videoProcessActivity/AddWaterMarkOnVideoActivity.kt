package com.simform.videoimageeditor.videoProcessActivity

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.jaiselrahman.filepicker.model.MediaFile
import com.simform.videoimageeditor.BaseActivity
import com.simform.videoimageeditor.R
import com.simform.videoimageeditor.databinding.ActivityAddWaterMarkOnVideoBinding
import com.simform.videoimageeditor.utils.enableEdgeToEdge
import com.simform.videooperations.CallBackOfQuery
import com.simform.videooperations.Common
import com.simform.videooperations.Common.VIDEO
import com.simform.videooperations.Common.getFilePath
import com.simform.videooperations.FFmpegCallBack
import com.simform.videooperations.LogMessage
import java.util.concurrent.CompletableFuture.runAsync

class AddWaterMarkOnVideoActivity : BaseActivity(R.layout.activity_add_water_mark_on_video, R.string.add_water_mark_on_video) {
    private lateinit var binding: ActivityAddWaterMarkOnVideoBinding
    private var isInputVideoSelected = false
    private var isWaterMarkImageSelected = false
    
    override fun initialization() {
        binding = ActivityAddWaterMarkOnVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge(binding.toolbar.root)
        binding.toolbar.textTitle.text = getString(R.string.add_water_mark_on_video)
        binding.btnVideoPath.setOnClickListener(this)
        binding.btnImagePath.setOnClickListener(this)
        binding.btnAdd.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnVideoPath -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                } else {
                    // Fallback for devices below Android 13
                    Common.selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = false)
                }
            }
            R.id.btnImagePath -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickSingleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    // Fallback for devices below Android 13
                    Common.selectFile(this, maxSelection = 1, isImageSelection = true, isAudioSelection = false)
                }
            }
            R.id.btnAdd -> {
                when {
                    !isInputVideoSelected -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    !isWaterMarkImageSelected -> {
                        Toast.makeText(this, getString(R.string.input_image_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(binding.edtXPos.text.toString()) -> {
                        Toast.makeText(this, getString(R.string.x_position_validation), Toast.LENGTH_SHORT).show()
                    }
                    binding.edtXPos.text.toString().toFloat() > 100 || binding.edtXPos.text.toString().toFloat() <= 0 -> {
                        Toast.makeText(this, getString(R.string.x_validation_invalid), Toast.LENGTH_SHORT).show()
                    }
                    TextUtils.isEmpty(binding.edtYPos.text.toString()) -> {
                        Toast.makeText(this, getString(R.string.y_position_validation), Toast.LENGTH_SHORT).show()
                    }
                    binding.edtYPos.text.toString().toFloat() > 100 || binding.edtYPos.text.toString().toFloat() <= 0 -> {
                        Toast.makeText(this, getString(R.string.y_validation_invalid), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        processStart()
                        addWaterMarkProcess()
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun selectedFiles(mediaFiles: List<MediaFile>?, fileRequestCode: Int) {
        when (fileRequestCode) {
            Common.VIDEO_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    binding.tvInputPathVideo.text =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Common.saveFileToTempAndGetPath(this, mediaFiles[0].uri)
                        } else {
                            mediaFiles[0].path
                        }
                    isInputVideoSelected = true
                    runAsync {
                        retriever = MediaMetadataRetriever()
                        retriever?.setDataSource(binding.tvInputPathVideo.text.toString())
                        val bit = retriever?.frameAtTime
                        width = bit?.width
                        height = bit?.height
                    }
                } else {
                    Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
            Common.IMAGE_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    binding.tvInputPathImage.text =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Common.saveFileToTempAndGetPath(this, mediaFiles[0].uri)
                        } else {
                            mediaFiles[0].path
                        }
                    isWaterMarkImageSelected = true
                } else {
                    Toast.makeText(this, getString(R.string.image_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addWaterMarkProcess() {
        val outputPath = getFilePath(this, VIDEO)
        val xPos = width?.let {
            (binding.edtXPos.text.toString().toFloat().times(it)).div(100)
        }
        val yPos = height?.let {
            (binding.edtYPos.text.toString().toFloat().times(it)).div(100)
        }
        val query = ffmpegQueryExtension.addVideoWaterMark(binding.tvInputPathVideo.text.toString(), binding.tvInputPathImage.text.toString(), xPos, yPos, outputPath)
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
            btnImagePath.isEnabled = true
            btnAdd.isEnabled = true
            mProgressView.root.visibility = View.GONE
        }
    }

    private fun processStart() {
        binding.apply {
            btnVideoPath.isEnabled = false
            btnImagePath.isEnabled = false
            btnAdd.isEnabled = false
            mProgressView.root.visibility = View.VISIBLE
        }
    }
}