package com.simform.videoimageeditor.videoProcessActivity

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.jaiselrahman.filepicker.model.MediaFile
import com.simform.videoimageeditor.BaseActivity
import com.simform.videoimageeditor.R
import com.simform.videoimageeditor.databinding.ActivityVideoFadeInFadeOutBinding
import com.simform.videoimageeditor.utils.enableEdgeToEdge
import com.simform.videooperations.CallBackOfQuery
import com.simform.videooperations.Common
import com.simform.videooperations.FFmpegCallBack
import com.simform.videooperations.FFmpegQueryExtension
import com.simform.videooperations.LogMessage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class VideoFadeInFadeOutActivity : BaseActivity(R.layout.activity_video_fade_in_fade_out, R.string.video_fade_in_and_fade_out) {
    private lateinit var binding: ActivityVideoFadeInFadeOutBinding
    private var isInputVideoSelected: Boolean = false
    private var selectedVideoDurationInSecond = 0L
    
    override fun initialization() {
        binding = ActivityVideoFadeInFadeOutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge(binding.toolbar.root)
        binding.toolbar.textTitle.text = getString(R.string.video_fade_in_and_fade_out)
        binding.btnVideoPath.setOnClickListener(this)
        binding.btnApplyFadeInFadeOut.setOnClickListener(this)
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
            R.id.btnApplyFadeInFadeOut -> {
                when {
                    !isInputVideoSelected -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        processStart()
                        fadeInFadeOutProcess()
                    }
                }
            }
        }
    }

    private fun fadeInFadeOutProcess() {
        val outputPath = Common.getFilePath(this, Common.VIDEO)
        val query = ffmpegQueryExtension.videoFadeInFadeOut(binding.tvInputPathVideo.text.toString(), selectedVideoDurationInSecond, fadeInEndSeconds = 3, fadeOutStartSeconds = 3, output = outputPath)

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
                    CompletableFuture.runAsync {
                        retriever = MediaMetadataRetriever()
                        retriever?.setDataSource(binding.tvInputPathVideo.text.toString())
                        val time = retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        time?.toLong()?.let {
                            selectedVideoDurationInSecond = TimeUnit.MILLISECONDS.toSeconds(it)
                        }
                    }
                } else {
                    Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processStop() {
        binding.apply {
            btnVideoPath.isEnabled = true
            btnApplyFadeInFadeOut.isEnabled = true
            mProgressView.root.visibility = View.GONE
        }
    }

    private fun processStart() {
        binding.apply {
            btnVideoPath.isEnabled = false
            btnApplyFadeInFadeOut.isEnabled = false
            mProgressView.root.visibility = View.VISIBLE
        }
    }
}